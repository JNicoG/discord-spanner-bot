package io.github.jnicog.discord.spanner.bot.command.dispatcher;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
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
                interaction.deferReply().queue(
                    __ -> interaction.getHook().editMessageById(messageId, content).queue(
                        ___ -> {},
                        error -> LOGGER.error("Failed to edit message {}: {}", messageId, error.getMessage())
                    ),
                    error -> LOGGER.error("Failed to defer reply before editing: {}", error.getMessage())
                );

            case InteractionResponse.UpdateOriginalMessage(String content) -> {
                if (interaction instanceof IMessageEditCallback editCallback) {
                    // For button interactions - acknowledge and edit the message the button is on
                    editCallback.editMessage(content).queue(
                        __ -> {},
                        error -> LOGGER.error("Failed to update original message: {}", error.getMessage())
                    );
                } else {
                    LOGGER.error("UpdateOriginalMessage used on non-button interaction");
                    interaction.reply("An error occurred.").setEphemeral(true).queue();
                }
            }

            case InteractionResponse.UpdateOriginalMessageAndClearComponents(String content) -> {
                if (interaction instanceof IMessageEditCallback editCallback) {
                    // For button interactions - acknowledge, update message, and remove all components
                    editCallback.editMessage(content)
                        .setComponents()  // Empty components list removes all buttons
                        .queue(
                            __ -> {},
                            error -> LOGGER.error("Failed to update original message and clear components: {}", error.getMessage())
                        );
                } else {
                    LOGGER.error("UpdateOriginalMessageAndClearComponents used on non-button interaction");
                    interaction.reply("An error occurred.").setEphemeral(true).queue();
                }
            }

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
