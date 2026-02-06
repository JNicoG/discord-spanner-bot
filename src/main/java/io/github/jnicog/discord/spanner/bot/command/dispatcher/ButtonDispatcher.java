package io.github.jnicog.discord.spanner.bot.command.dispatcher;

import io.github.jnicog.discord.spanner.bot.command.ButtonContext;
import io.github.jnicog.discord.spanner.bot.command.InteractionResponder;
import io.github.jnicog.discord.spanner.bot.command.JdaInteractionResponder;
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
    private final Map<String, ButtonCommandHandler> handlers = new HashMap<>();

    public ButtonDispatcher(ApplicationEventPublisher eventPublisher, List<ButtonCommandHandler> handlerList) {
        this.eventPublisher = eventPublisher;
        handlerList.forEach(handler -> {
            this.handlers.put(handler.getCommandName(), handler);
            handler.getAliases().forEach(alias -> this.handlers.put(alias, handler));
        });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        LOGGER.debug("Received button interaction: {} by {} at {}",
                event.getComponentId(), event.getUser().getAsTag(), event.getChannelId());

        ButtonCommandHandler handler = handlers.get(event.getComponentId());
        if (handler == null) {
            LOGGER.error("No handler found for button: {}", event.getButton().getLabel());
            event.reply("Unknown button: " + event.getButton().getLabel()).setEphemeral(true).queue();
            return;
        }

        try {
            // Acknowledge
            //event.deferReply(handler.isEphemeral()).queue();

            // Unpack and map to domain context
            InteractionResponder interactionResponder = new JdaInteractionResponder(event);

            ButtonContext context = new ButtonContext(
                    event.getTimeCreated(),
                    handler.getCommandName(),
                    event.getUser().getIdLong(),
                    event.getChannelIdLong(),
                    interactionResponder,
                    event.getMessageIdLong()
            );

            AbstractCommandResult<?> commandResult = handler.handleCommand(context);

            LOGGER.debug("Publishing result: {}", commandResult.getClass());
            eventPublisher.publishEvent(commandResult);

        } catch (Exception e) {
            LOGGER.error("Error handling button: {}", event.getButton().getLabel(), e);
            event.reply("An error occurred while processing your command. Please try again later.")
                    .setEphemeral(true).queue();
        }
    }

}
