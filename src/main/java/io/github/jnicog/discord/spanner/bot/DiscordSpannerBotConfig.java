package io.github.jnicog.discord.spanner.bot;

import com.google.common.base.Strings;
import io.github.jnicog.discord.spanner.bot.command.registry.SlashCommandRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
@Profile("!test")
public class DiscordSpannerBotConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordSpannerBotConfig.class);

    private static final String SPANNER_BOT_TOKEN_ENV = "SPANNER_BOT_TOKEN";

    private final List<ListenerAdapter> eventListeners;

    public DiscordSpannerBotConfig(List<ListenerAdapter> eventListeners) {
        this.eventListeners = eventListeners;
    }

    @Bean
    public JDA jda(Environment env,
                   SlashCommandRegistry commandRegistry) throws InterruptedException {
        String botToken = env.getProperty(SPANNER_BOT_TOKEN_ENV);

        if (Strings.isNullOrEmpty(botToken)) {
            throw new IllegalStateException(
                    String.format("Discord bot token not found. Ensure the environment variable %s is set.",
                            SPANNER_BOT_TOKEN_ENV));
        }

        JDA jda = JDABuilder.createDefault(botToken)
                .setActivity(Activity.playing("Looking for Spanners"))
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS
                )
                .addEventListeners(eventListeners.toArray())
                .build()
                .awaitReady();

        jda.updateCommands()
                .addCommands(commandRegistry.getCommands())
                .queue(success -> {
                    LOGGER.info("Registered {} slash commands successfully", success.stream().toList());
                }, error -> {
                    LOGGER.error("Failed to register slash commands: {}", error.getMessage());
                });

        return jda;
    }

}
