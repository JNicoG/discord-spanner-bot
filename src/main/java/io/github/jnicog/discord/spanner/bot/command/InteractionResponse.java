package io.github.jnicog.discord.spanner.bot.command;

/**
 * Represents an interaction response that is completely decoupled from JDA.
 * The dispatcher translates this into the appropriate JDA call.
 */
public sealed interface InteractionResponse permits
        InteractionResponse.EphemeralReply,
        InteractionResponse.PublicReply,
        InteractionResponse.EditMessage,
        InteractionResponse.UpdateOriginalMessage,
        InteractionResponse.UpdateOriginalMessageAndClearComponents,
        InteractionResponse.EditButtonMessage,
        InteractionResponse.EditButtonMessageAndClearComponents,
        InteractionResponse.DeferReply,
        InteractionResponse.NoReply {

    record EphemeralReply(String content) implements InteractionResponse {}
    record PublicReply(String content) implements InteractionResponse {}
    record EditMessage(long messageId, String content) implements InteractionResponse {}
    record UpdateOriginalMessage(String content) implements InteractionResponse {}
    record UpdateOriginalMessageAndClearComponents(String content) implements InteractionResponse {}
    /**
     * Specifically for button interactions - edits the message the button is attached to.
     * Uses editMessage() instead of hook.editOriginal(). Keeps components (buttons) intact.
     */
    record EditButtonMessage(String content) implements InteractionResponse {}
    /**
     * Specifically for button interactions - edits the message the button is attached to
     * and clears all components (buttons). Uses editMessage() instead of hook.editOriginal().
     */
    record EditButtonMessageAndClearComponents(String content) implements InteractionResponse {}
    record DeferReply(boolean ephemeral) implements InteractionResponse {}
    record NoReply() implements InteractionResponse {}
}
