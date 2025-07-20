package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.SpannerVote;

import java.time.LocalDateTime;
import java.util.List;

public interface SpannerVoteService {

    SpannerVote createVote(long channelId, long targetUserId, long initiatorUserId, String reason);

    void updateVoteMessage(SpannerVote vote, long messageId);

    void updateVoteCounts(long messageId, int yesVotes, int noVotes);

    SpannerVote findActiveVoteByMessageId(long messageId);

    void completeVote(SpannerVote vote);

    void expireOverdueVotes();

    List<SpannerVote> findExpiredVotes(LocalDateTime cutoffTime);
}