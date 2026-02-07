package io.github.jnicog.discord.spanner.bot.command.dispatcher;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Translates domain-agnostic InteractionResponse objects into JDA API calls.
 * This is the single point where JDA coupling exists for responses.
 */
@Component
public class JdaResponseTranslator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdaResponseTranslator.class);

    /**
     * Handles responses for ButtonInteractionEvent specifically.
     * Supports button-specific response types like EditButtonMessageAndClearComponents.
     */
    public void send(ButtonInteractionEvent event, InteractionResponse response) {
        switch (response) {
            case InteractionResponse.EditButtonMessage(String content) ->
                event.editMessage(content)
                    .queue(
                        __ -> {},
                        error -> LOGGER.error("Failed to edit button message: {}", error.getMessage())
                    );

            case InteractionResponse.EditButtonMessageAndClearComponents(String content) ->
                event.editMessage(content)
                    .setComponents()  // Clear all components (buttons, etc.)
                    .queue(
                        __ -> {},
                        error -> LOGGER.error("Failed to edit button message and clear components: {}", error.getMessage())
                    );

            // Delegate other response types to the generic handler
            default -> send((IReplyCallback) event, response);
        }
    }

    public void send(IReplyCallback interaction, InteractionResponse response) {
        switch (response) {
            case InteractionResponse.EphemeralReply(String content) ->
                interaction.reply(content).setEphemeral(true).queue(
                    __ -> {},
                    error -> LOGGER.error("Failed to send ephemeral reply: {}", error.getMessage())
                );

            case InteractionResponse.PublicReply(String content) ->
                interaction.reply(content).setEphemeral(false).queue(
                    __ -> {},
                    error -> LOGGER.error("Failed to send public reply: {}", error.getMessage())
                );

            case InteractionResponse.EditMessage(long messageId, String content) ->
                interaction.getHook().editMessageById(messageId, content).queue(
                    __ -> {},
                    error -> LOGGER.error("Failed to edit message {}: {}", messageId, error.getMessage())
                );

            case InteractionResponse.UpdateOriginalMessage(String content) ->
                interaction.getHook().editOriginal(content).queue(
                    __ -> {},
                    error -> LOGGER.error("Failed to update original message: {}", error.getMessage())
                );

            case InteractionResponse.UpdateOriginalMessageAndClearComponents(String content) ->
                interaction.getHook().editOriginal(content)
                    .setComponents()  // Clear all components (buttons, etc.)
                    .queue(
                        __ -> {},
                        error -> LOGGER.error("Failed to update original message and clear components: {}", error.getMessage())
                    );

            case InteractionResponse.EditButtonMessage(String content) ->
                // This should only be used with ButtonInteractionEvent
                LOGGER.error("EditButtonMessage used with non-button interaction");

            case InteractionResponse.EditButtonMessageAndClearComponents(String content) ->
                // This should only be used with ButtonInteractionEvent
                LOGGER.error("EditButtonMessageAndClearComponents used with non-button interaction");

            case InteractionResponse.DeferReply(boolean ephemeral) ->
                interaction.deferReply(ephemeral).queue(
                    __ -> {},
                    error -> LOGGER.error("Failed to defer reply: {}", error.getMessage())
                );

            case InteractionResponse.NoReply() -> {
                // Intentionally do nothing
            }
        }
    }
}
