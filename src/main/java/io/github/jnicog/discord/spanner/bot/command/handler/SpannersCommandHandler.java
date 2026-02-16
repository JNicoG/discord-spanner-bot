package io.github.jnicog.discord.spanner.bot.command.handler;

import io.github.jnicog.discord.spanner.bot.command.SlashCommandContext;
import io.github.jnicog.discord.spanner.bot.event.AbstractCommandResult;
import io.github.jnicog.discord.spanner.bot.event.spanner.SpannerQueryEvent;
import io.github.jnicog.discord.spanner.bot.spanner.SpannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for the /spanners command.
 * Queries the spanner count for a user in the current channel.
 * Creates a record if one doesn't exist.
 */
@Component
public class SpannersCommandHandler implements SlashCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpannersCommandHandler.class);
    private static final String USER_OPTION = "user";

    private final SpannerService spannerService;

    public SpannersCommandHandler(SpannerService spannerService) {
        this.spannerService = spannerService;
    }

    @Override
    public String getCommandName() {
        return "spanners";
    }

    @Override
    public AbstractCommandResult<?> handleCommand(SlashCommandContext context) {
        long channelId = context.channelId();
        long requestingUserId = context.userId();

        // Check if a target user was specified
        String targetUserIdString = context.options().get(USER_OPTION);

        if (targetUserIdString != null && !targetUserIdString.isBlank()) {
            return handleUserQuery(context, channelId, requestingUserId, targetUserIdString);
        }

        return handleSelfQuery(context, channelId, requestingUserId);
    }

    private AbstractCommandResult<?> handleSelfQuery(SlashCommandContext context,
                                                      long channelId,
                                                      long userId) {
        LOGGER.debug("User {} querying their own spanner count in channel {}", userId, channelId);

        int spannerCount = spannerService.getOrCreateSpannerCount(userId, channelId);

        return new SpannerQueryEvent(context, userId, spannerCount, true);
    }

    private AbstractCommandResult<?> handleUserQuery(SlashCommandContext context,
                                                      long channelId,
                                                      long requestingUserId,
                                                      String targetUserIdString) {
        long targetUserId;
        try {
            targetUserId = Long.parseLong(targetUserIdString);
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid user ID format: {}", targetUserIdString);
            // Fallback to self query if invalid user ID
            return handleSelfQuery(context, channelId, requestingUserId);
        }

        LOGGER.debug("User {} querying spanner count for user {} in channel {}",
                requestingUserId, targetUserId, channelId);

        // Ensure both users have records created
        spannerService.getOrCreateSpannerCount(requestingUserId, channelId);
        int targetSpannerCount = spannerService.getOrCreateSpannerCount(targetUserId, channelId);

        boolean isSelfQuery = requestingUserId == targetUserId;

        return new SpannerQueryEvent(context, targetUserId, targetSpannerCount, isSelfQuery);
    }
}

