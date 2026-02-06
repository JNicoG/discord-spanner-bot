//package io.github.jnicog.discord.spanner.bot.command;
//
//import java.time.OffsetDateTime;
//import java.util.Map;
//
//public class SlashCommandContext extends CommandContext {
//
//    private final Map<String, String> options;
//
//    public SlashCommandContext(OffsetDateTime eventTime,
//                               String commandName,
//                               long userId,
//                               long channelId,
//                               Map<String, String> options,
//                               InteractionResponder interactionResponder) {
//        super(eventTime, commandName, userId, channelId, interactionResponder);
//        this.options = options;
//    }
//
//    public Map<String, String> getOptions() {
//        return options;
//    }
//
//}
