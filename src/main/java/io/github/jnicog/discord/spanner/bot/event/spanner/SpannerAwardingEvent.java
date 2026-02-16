package io.github.jnicog.discord.spanner.bot.event.spanner;

public interface SpannerAwardingEvent {
    long getTargetUserId();
    long getTargetChannelId();
}
