package io.github.jnicog.discord.spanner.bot.command;

public interface InteractionResponder {
    void replyEphemeral(String content);
    void replyPublic(String content);
//    void editReply(String content);
}
