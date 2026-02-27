package io.github.jnicog.discord.spanner.bot.command;

import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardPage;

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
        InteractionResponse.EditButtonMessageWithComponents,
        InteractionResponse.LeaderboardEmbed,
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
    /**
     * Specifically for button interactions - edits the message and replaces buttons with the given specs.
     * Allows individual buttons to be enabled or disabled.
     */
    record EditButtonMessageWithComponents(String content, java.util.List<ButtonSpec> buttonSpecs) implements InteractionResponse {}
    /**
     * JDA-free representation of a single button to be rendered in EditButtonMessageWithComponents.
     */
    record ButtonSpec(String componentId, String label, boolean disabled) {}
    /**
     * Leaderboard embed with pagination reactions.
     * The dispatcher will send an embed and add reaction emojis.
     */
    record LeaderboardEmbed(LeaderboardPage page, long ownerId) implements InteractionResponse {}
    record DeferReply(boolean ephemeral) implements InteractionResponse {}
    record NoReply() implements InteractionResponse {}
}
