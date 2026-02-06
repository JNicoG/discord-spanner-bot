package io.github.jnicog.discord.spanner.bot.command;

public interface InteractionResponder {
    void replyEphemeral(String content);
    void replyPublic(String content);
    void updateMessageContent(String content, long messageId);
}
