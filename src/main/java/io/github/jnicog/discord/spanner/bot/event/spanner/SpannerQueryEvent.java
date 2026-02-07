package io.github.jnicog.discord.spanner.bot.event.spanner;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;

/**
 * Event for querying a user's spanner count.
 */
public class SpannerQueryEvent extends AbstractCommandResult<SlashCommandContext> {

    private final long targetUserId;
    private final int spannerCount;
    private final boolean isSelfQuery;

    public SpannerQueryEvent(SlashCommandContext context,
                             long targetUserId,
                             int spannerCount,
                             boolean isSelfQuery) {
        super(context);
        this.targetUserId = targetUserId;
        this.spannerCount = spannerCount;
        this.isSelfQuery = isSelfQuery;
    }

    /**
     * The user whose spanner count was queried.
     */
    public long getTargetUserId() {
        return targetUserId;
    }

    /**
     * The spanner count for the target user.
     */
    public int getSpannerCount() {
        return spannerCount;
    }

    /**
     * Whether the query was for the user who issued the command.
     */
    public boolean isSelfQuery() {
        return isSelfQuery;
    }
}

