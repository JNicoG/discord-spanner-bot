package io.github.jnicog.discord.spanner.bot.command.dispatcher;

import io.github.jnicog.discord.spanner.bot.command.PendingInteraction;
import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
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

/**
 *  dispatcher that keeps JDA event local and publishes a PendingInteraction
 * rather than passing a responder through the context.
 *
 * <ul>
 *   <li>Handlers receive a JDA-free context</li>
 *   <li>Handlers return domain events without response capability</li>
 *   <li>The dispatcher wraps the result with a response callback</li>
 *   <li>Notification handlers resolve responses using pure functions</li>
 * </ul>
 */
@Component
public class SlashCommandDispatcher extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandDispatcher.class);

    private final ApplicationEventPublisher eventPublisher;
    private final JdaResponseTranslator responseTranslator;
    private final Map<String, SlashCommandHandler> handlers = new HashMap<>();

    public SlashCommandDispatcher(
            ApplicationEventPublisher eventPublisher,
            JdaResponseTranslator responseTranslator,
            List<SlashCommandHandler> handlerList) {
        this.eventPublisher = eventPublisher;
        this.responseTranslator = responseTranslator;
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
            // Extract options as a simple map
            Map<String, String> options = event.getOptions().stream()
                    .collect(HashMap::new, (map, option)
                            -> map.put(option.getName(), option.getAsString()), HashMap::putAll);

            // Create JDA-free context - no responder passed!
            SlashCommandContext context = new SlashCommandContext(
                    event.getTimeCreated(),
                    handler.getCommandName(),
                    event.getUser().getIdLong(),
                    event.getChannelIdLong(),
                    options
            );

            // Handler returns pure domain result
            AbstractCommandResult<?> commandResult = handler.handleCommand(context);

            // Wrap result with response callback that closes over the JDA event
            PendingInteraction<?> pending = new PendingInteraction<>(
                    commandResult,
                    response -> responseTranslator.send(event, response)
            );

            LOGGER.debug("Publishing pending interaction for: {}", commandResult.getClass());
            eventPublisher.publishEvent(pending);

            // Also publish the raw event for other listeners (e.g., StartCheckInTrigger)
            LOGGER.debug("Publishing raw command result: {}", commandResult.getClass());
            eventPublisher.publishEvent(commandResult);

        } catch (Exception e) {
            LOGGER.error("Error handling command: {}", event.getName(), e);
            event.reply("An error occurred while processing your command. Please try again later.")
                    .setEphemeral(true).queue();
        }
    }
}

