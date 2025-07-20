package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.SpannerVote;
import io.github.jnicog.discord.spanner.bot.model.VoteStatus;
import io.github.jnicog.discord.spanner.bot.repository.SpannerVoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SpannerVoteServiceImpl implements SpannerVoteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpannerVoteServiceImpl.class);
    
    // 10 minutes default vote duration
    private static final int VOTE_DURATION_MINUTES = 10;

    private final SpannerVoteRepository voteRepository;
    private final SpannerService spannerService;

    public SpannerVoteServiceImpl(SpannerVoteRepository voteRepository, SpannerService spannerService) {
        this.voteRepository = voteRepository;
        this.spannerService = spannerService;
    }

    @Override
    public SpannerVote createVote(long channelId, long targetUserId, long initiatorUserId, String reason) {
        SpannerVote vote = new SpannerVote(channelId, 0L, targetUserId, initiatorUserId, reason);
        SpannerVote savedVote = voteRepository.save(vote);
        LOGGER.info("Created spanner vote {} for target user {} in channel {}", 
                savedVote.getId(), targetUserId, channelId);
        return savedVote;
    }

    @Override
    public void updateVoteMessage(SpannerVote vote, long messageId) {
        vote.setMessageId(messageId);
        voteRepository.save(vote);
        LOGGER.debug("Updated vote {} with message ID {}", vote.getId(), messageId);
    }

    @Override
    public void updateVoteCounts(long messageId, int yesVotes, int noVotes) {
        SpannerVote vote = findActiveVoteByMessageId(messageId);
        if (vote != null) {
            vote.setYesVotes(yesVotes);
            vote.setNoVotes(noVotes);
            voteRepository.save(vote);
            LOGGER.debug("Updated vote counts for message {}: Yes={}, No={}", messageId, yesVotes, noVotes);
        }
    }

    @Override
    public SpannerVote findActiveVoteByMessageId(long messageId) {
        return voteRepository.findByMessageIdAndStatus(messageId, VoteStatus.ACTIVE).orElse(null);
    }

    @Override
    public void completeVote(SpannerVote vote) {
        vote.setStatus(VoteStatus.COMPLETED);
        vote.setCompletedAt(LocalDateTime.now());
        voteRepository.save(vote);
        
        if (vote.hasVotePassed()) {
            spannerService.incrementSpannerCount(vote.getTargetUserId(), vote.getChannelId());
            LOGGER.info("Vote {} passed - assigned spanner to user {} in channel {}", 
                    vote.getId(), vote.getTargetUserId(), vote.getChannelId());
        } else {
            LOGGER.info("Vote {} failed - no spanner assigned", vote.getId());
        }
    }

    @Override
    public void expireOverdueVotes() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(VOTE_DURATION_MINUTES);
        List<SpannerVote> overdueVotes = voteRepository.findByStatusAndCreatedAtBefore(VoteStatus.ACTIVE, cutoffTime);
        
        for (SpannerVote vote : overdueVotes) {
            vote.setStatus(VoteStatus.EXPIRED);
            vote.setCompletedAt(LocalDateTime.now());
            voteRepository.save(vote);
            LOGGER.info("Expired vote {} due to timeout", vote.getId());
        }
    }

    @Override
    public List<SpannerVote> findExpiredVotes(LocalDateTime cutoffTime) {
        return voteRepository.findByStatusAndCreatedAtBefore(VoteStatus.ACTIVE, cutoffTime);
    }
}