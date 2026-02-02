package io.github.jnicog.discord.spanner.bot.command;

public interface InteractionResponder {
    void editReply(String content);
    void replyEphemeral(String content);
    void replyPublic(String content);
}
