package io.github.jnicog.discord.spanner.bot.event;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponder;

import java.time.OffsetDateTime;
import java.util.Map;

public abstract class AbstractCommandResult {
    public OffsetDateTime eventTime;
    public String commandName;
    public long userId;
    public long channelId;
    public String details;
    public Map<String, Object> metadata;
    public InteractionResponder interactionResponder;

    protected AbstractCommandResult(OffsetDateTime eventTime,
                                    String commandName,
                                    long userId,
                                    long channelId,
                                    String details,
                                    Map<String, Object> metadata,
                                    InteractionResponder interactionResponder) {
        this.eventTime = eventTime;
        this.commandName = commandName;
        this.userId = userId;
        this.channelId = channelId;
        this.details = details;
        this.metadata = metadata;
        this.interactionResponder = interactionResponder;
    }

    public AbstractCommandResult() {

    }

    public OffsetDateTime getEventTime() {
        return eventTime;
    }

    public String getCommandName() {
        return commandName;
    }

    public long getUserId() {
        return userId;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getDetails() {
        return details;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public InteractionResponder getInteractionResponder() {
        return interactionResponder;
    }
}
