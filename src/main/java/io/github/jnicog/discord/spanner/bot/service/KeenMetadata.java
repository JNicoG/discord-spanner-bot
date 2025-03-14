package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class KeenMetadata {

    private final long keenTime;
    private final SlashCommandInteractionEvent keenInteractionEvent;

    public KeenMetadata(long keenTime, SlashCommandInteractionEvent keenInteractionEvent) {
        this.keenTime = keenTime;
        this.keenInteractionEvent = keenInteractionEvent;
    }

    public long getKeenTime() {
        return this.keenTime;
    }

    public SlashCommandInteractionEvent getKeenInteractionEvent() {
        return this.keenInteractionEvent;
    }

}
