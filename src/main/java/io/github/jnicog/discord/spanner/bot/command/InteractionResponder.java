package io.github.jnicog.discord.spanner.bot.command;

/**
 * V1 responder interface that couples response capability to the domain layer.
 *
 * @deprecated Use {@link InteractionResponse} with {@link io.github.jnicog.discord.spanner.bot.command.dispatcher.JdaResponseTranslator}
 * instead. The V2 architecture keeps JDA coupling in the dispatcher layer.
 */
@Deprecated
public interface InteractionResponder {
    void replyEphemeral(String content);
    void replyPublic(String content);
    void updateMessageContent(String content, long messageId);
}
