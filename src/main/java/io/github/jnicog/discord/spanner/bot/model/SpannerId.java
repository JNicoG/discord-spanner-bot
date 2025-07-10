package io.github.jnicog.discord.spanner.bot.model;

import java.io.Serializable;
import java.util.Objects;

public class SpannerId implements Serializable {

    private long userId;
    private long channelId;

    public SpannerId() {
        // Empty default constructor
    }

    public SpannerId(long userId, long channelId) {
        this.userId = userId;
        this.channelId = channelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpannerId spannerId = (SpannerId) o;
        return userId == spannerId.userId && channelId == spannerId.channelId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, channelId);
    }

}
