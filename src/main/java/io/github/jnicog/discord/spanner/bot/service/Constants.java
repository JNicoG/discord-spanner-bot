package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Constants {
    public static final Emoji checkMarkEmoji = Emoji.fromUnicode("U+2714");
    public static final Emoji spannerEmoji = Emoji.fromUnicode("U+1F527");
    public static final Emoji yesEmoji = Emoji.fromUnicode("U+1F44D");
    public static final Emoji noEmoji = Emoji.fromUnicode("U+1F44E");
    public static final Button acceptButton = Button.success("acceptButton", checkMarkEmoji);
    public static final Button spannerButton = Button.danger("spannerButton", spannerEmoji);
    public static final Button yesVoteButton = Button.success("yesVoteButton", yesEmoji);
    public static final Button noVoteButton = Button.danger("noVoteButton", noEmoji);
    public static final String awaitingButton = "X";
}
