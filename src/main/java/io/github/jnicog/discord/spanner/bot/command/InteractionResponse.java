package io.github.jnicog.discord.spanner.bot.command;

/**
 * The dispatcher translates this into the appropriate JDA call.
 */
public sealed interface InteractionResponse permits
        InteractionResponse.EphemeralReply,
        InteractionResponse.PublicReply,
        InteractionResponse.EditMessage,
        InteractionResponse.UpdateOriginalMessage,
        InteractionResponse.DeferReply,
        InteractionResponse.NoReply {

    record EphemeralReply(String content) implements InteractionResponse {}
    record PublicReply(String content) implements InteractionResponse {}
    record EditMessage(long messageId, String content) implements InteractionResponse {}
    /** For button interactions - acknowledges the interaction and updates the message the button is on */
    record UpdateOriginalMessage(String content) implements InteractionResponse {}
    record DeferReply(boolean ephemeral) implements InteractionResponse {}
    record NoReply() implements InteractionResponse {}
}
