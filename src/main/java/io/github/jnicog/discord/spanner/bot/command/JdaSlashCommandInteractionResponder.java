package io.github.jnicog.discord.spanner.bot.command;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class JdaSlashCommandInteractionResponder implements InteractionResponder {

    private final IReplyCallback interaction;

    public JdaSlashCommandInteractionResponder(SlashCommandInteraction event) {
        this.interaction = event;
    }

    @Override
    public void replyEphemeral(String content) {
        interaction.reply(content).setEphemeral(true).queue();
    }

    @Override
    public void replyPublic(String content) {
        interaction.reply(content).setEphemeral(false).queue();
    }

    @Override
    public void editReply(String content) {
        interaction.getHook().editOriginal(content).queue();
    }

}
