package io.github.jnicog.discord.spanner.bot.command.dispatcher;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.command.PendingInteraction;
import io.github.jnicog.discord.spanner.bot.command.handler.ButtonCommandHandler;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ButtonDispatcher extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ButtonDispatcher.class);

    private final ApplicationEventPublisher eventPublisher;
    private final JdaResponseTranslator responseTranslator;
    private final Map<String, ButtonCommandHandler> handlers = new HashMap<>();

    public ButtonDispatcher(
            ApplicationEventPublisher eventPublisher,
            JdaResponseTranslator responseTranslator,
            List<ButtonCommandHandler> handlerList) {
        this.eventPublisher = eventPublisher;
        this.responseTranslator = responseTranslator;
        handlerList.forEach(handler -> {
            this.handlers.put(handler.getCommandName(), handler);
            handler.getAliases().forEach(alias -> this.handlers.put(alias, handler));
        });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        LOGGER.debug("Received button interaction: {} by {} at {}",
                event.getComponentId(), event.getUser().getAsTag(), event.getChannelId());

        String componentId = event.getComponentId();
        ButtonCommandHandler handler = handlers.get(componentId);
        if (handler == null) {
            // Fallback: prefix-matching for handlers like ten-man buttons (tenman_<id>)
            handler = handlers.values().stream()
                    .filter(h -> h.matchesComponentId(componentId))
                    .findFirst()
                    .orElse(null);
        }
        if (handler == null) {
            LOGGER.error("No handler found for button: {}", event.getButton().getLabel());
            event.reply("Unknown button: " + event.getButton().getLabel()).setEphemeral(true).queue();
            return;
        }

        try {
            ButtonInteractionContext context = new ButtonInteractionContext(
                    event.getTimeCreated(),
                    componentId,
                    event.getUser().getIdLong(),
                    event.getChannelIdLong(),
                    event.getMessageIdLong()
            );

            AbstractCommandResult<?> commandResult = handler.handleCommand(context);

            PendingInteraction<?> pending = new PendingInteraction<>(
                    commandResult,
                    response -> responseTranslator.send(event, response)
            );

            LOGGER.debug("Publishing pending interaction for: {}", commandResult.getClass());
            eventPublisher.publishEvent(pending);

            // Also publish the raw event for other listeners
            LOGGER.debug("Publishing raw command result: {}", commandResult.getClass());
            eventPublisher.publishEvent(commandResult);

        } catch (Exception e) {
            LOGGER.error("Error handling button: {}", event.getButton().getLabel(), e);
            event.reply("An error occurred while processing your command. Please try again later.")
                    .setEphemeral(true).queue();
        }
    }
}
