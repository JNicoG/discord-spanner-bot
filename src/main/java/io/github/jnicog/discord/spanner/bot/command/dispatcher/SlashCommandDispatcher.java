package io.github.jnicog.discord.spanner.bot.command.dispatcher;

import io.github.jnicog.discord.spanner.bot.command.CommandContext;
import io.github.jnicog.discord.spanner.bot.command.InteractionResponder;
import io.github.jnicog.discord.spanner.bot.command.JdaSlashCommandInteractionResponder;
import io.github.jnicog.discord.spanner.bot.command.handler.SlashCommandHandler;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlashCommandDispatcher extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandDispatcher.class);

    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, SlashCommandHandler> handlers = new HashMap<>();

    public SlashCommandDispatcher(ApplicationEventPublisher eventPublisher, List<SlashCommandHandler> handlerList) {
        this.eventPublisher = eventPublisher;
        handlerList.forEach(handler -> {
            this.handlers.put(handler.getCommandName(), handler);
            handler.getAliases().forEach(alias -> this.handlers.put(alias, handler));
        });
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        LOGGER.debug("Received slash command: {} by {} at {}",
                event.getName(), event.getUser().getAsTag(), event.getChannelId());

        SlashCommandHandler handler = handlers.get(event.getName());
        if (handler == null) {
            LOGGER.error("No handler found for command: {}", event.getName());
            event.reply("Unknown command: " + event.getName()).setEphemeral(true).queue();
            return;
        }

        try {
            // Acknowledge
            //event.deferReply(handler.isEphemeral()).queue();

            // Unpack and map to domain context
            InteractionResponder interactionResponder = new JdaSlashCommandInteractionResponder(event);

            CommandContext context = new CommandContext(
                    event.getTimeCreated(),
                    handler.getCommandName(),
                    event.getUser().getIdLong(),
                    event.getChannelIdLong(),
                    interactionResponder
            );

            AbstractCommandResult commandResult = handler.handleCommand(context);

            LOGGER.debug("Publishing result: {}", commandResult.getClass());
            eventPublisher.publishEvent(commandResult);

        } catch (Exception e) {
            LOGGER.error("Error handling command: {}", event.getName(), e);
            event.reply("An error occurred while processing your command. Please try again later.")
                    .setEphemeral(true).queue();
        }
    }

}
