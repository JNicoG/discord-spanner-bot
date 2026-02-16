package io.github.jnicog.discord.spanner.bot.notification.resolver;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.command.ResponseResolver;
import io.github.jnicog.discord.spanner.bot.event.leaderboard.LeaderboardQueryEvent;
import org.springframework.stereotype.Component;

/**
 * ResponseResolver for LeaderboardQueryEvent.
 * Returns a LeaderboardEmbed response which the dispatcher handles specially.
 */
@Component
public class LeaderboardQueryResponseResolver implements ResponseResolver<LeaderboardQueryEvent> {

    @Override
    public InteractionResponse resolve(LeaderboardQueryEvent event) {
        return new InteractionResponse.LeaderboardEmbed(
                event.getLeaderboardPage(),
                event.getContext().userId()
        );
    }
}

