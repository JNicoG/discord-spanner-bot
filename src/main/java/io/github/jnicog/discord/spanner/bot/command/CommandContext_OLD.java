//package io.github.jnicog.discord.spanner.bot.command;
//
//import java.time.OffsetDateTime;
//
//public abstract class CommandContext {
//
//    private final OffsetDateTime eventTime;
//    private final String commandName;
//    private final long userId;
//    private final long channelId;
//    private final InteractionResponder interactionResponder;
//
//    public CommandContext(OffsetDateTime eventTime,
//                          String commandName,
//                          long userId,
//                          long channelId,
//                          InteractionResponder interactionResponder) {
//        this.eventTime = eventTime;
//        this.commandName = commandName;
//        this.userId = userId;
//        this.channelId = channelId;
//        this.interactionResponder = interactionResponder;
//    }
//
//    public OffsetDateTime getEventTime() {
//        return eventTime;
//    }
//
//    public String getCommandName() {
//        return commandName;
//    }
//
//    public long getUserId() {
//        return userId;
//    }
//
//    public long getChannelId() {
//        return channelId;
//    }
//
//    public InteractionResponder getInteractionResponder() {
//        return interactionResponder;
//    }
//}
