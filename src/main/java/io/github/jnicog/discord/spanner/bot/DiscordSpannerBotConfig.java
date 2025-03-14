package io.github.jnicog.discord.spanner.bot;

import com.google.common.base.Strings;
import io.github.jnicog.discord.spanner.bot.controller.QueueCommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("!test")
public class DiscordSpannerBotConfig {

    private static final String SPANNER_BOT_TOKEN_ENV = "SPANNER_BOT_TOKEN";

    @Bean
    public JDA jda(Environment env, QueueCommandHandler queueCommandHandler) throws InterruptedException {
        String botToken = env.getProperty(SPANNER_BOT_TOKEN_ENV);

        if (Strings.isNullOrEmpty(botToken)) {
            throw new IllegalStateException(
                    String.format("Discord bot token not found. Ensure the environment variable %s is set.",
                            SPANNER_BOT_TOKEN_ENV));
        }

        return JDABuilder.createDefault(botToken)
                .setActivity(Activity.playing("Looking for Spanners"))
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(queueCommandHandler)
                .build()
                .awaitReady();
    }

}
