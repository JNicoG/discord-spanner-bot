package io.github.jnicog.discord.spanner.bot.command.dispatcher;

import io.github.jnicog.discord.spanner.bot.command.ButtonInteractionContext;
import io.github.jnicog.discord.spanner.bot.command.PendingInteractionV2;
import io.github.jnicog.discord.spanner.bot.command.handler.ButtonCommandHandlerV2;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResultV2;
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
public class ButtonDispatcherV2 extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ButtonDispatcherV2.class);

    private final ApplicationEventPublisher eventPublisher;
    private final JdaResponseTranslator responseTranslator;
    private final Map<String, ButtonCommandHandlerV2> handlers = new HashMap<>();

    public ButtonDispatcherV2(
            ApplicationEventPublisher eventPublisher,
            JdaResponseTranslator responseTranslator,
            List<ButtonCommandHandlerV2> handlerList) {
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

        ButtonCommandHandlerV2 handler = handlers.get(event.getComponentId());
        if (handler == null) {
            LOGGER.error("No handler found for button: {}", event.getButton().getLabel());
            event.reply("Unknown button: " + event.getButton().getLabel()).setEphemeral(true).queue();
            return;
        }

        try {
            ButtonInteractionContext context = new ButtonInteractionContext(
                    event.getTimeCreated(),
                    handler.getCommandName(),
                    event.getUser().getIdLong(),
                    event.getChannelIdLong(),
                    event.getMessageIdLong()
            );

            AbstractCommandResultV2<?> commandResult = handler.handleCommand(context);

            PendingInteractionV2<?> pending = new PendingInteractionV2<>(
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
