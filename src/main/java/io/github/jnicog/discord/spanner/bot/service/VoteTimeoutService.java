package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.SpannerVote;
import io.github.jnicog.discord.spanner.bot.model.VoteStatus;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoteTimeoutService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoteTimeoutService.class);

    private final SpannerVoteService spannerVoteService;
    private final NotificationService notificationService;
    private final JDA jda;

    public VoteTimeoutService(SpannerVoteService spannerVoteService, 
                             NotificationService notificationService, 
                             @Autowired(required = false) JDA jda) {
        this.spannerVoteService = spannerVoteService;
        this.notificationService = notificationService;
        this.jda = jda;
    }

    @Scheduled(fixedDelay = 60000) // Run every minute
    public void checkForExpiredVotes() {
        try {
            // First check for expired votes and process them
            List<SpannerVote> expiredVotes = getExpiredVotes();
            for (SpannerVote vote : expiredVotes) {
                processExpiredVote(vote);
            }
            
            // Then expire any remaining overdue votes
            spannerVoteService.expireOverdueVotes();
        } catch (Exception e) {
            LOGGER.error("Error checking for expired votes", e);
        }
    }

    private List<SpannerVote> getExpiredVotes() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(10); // 10 minutes ago
        return spannerVoteService.findExpiredVotes(cutoffTime);
    }

    private void processExpiredVote(SpannerVote vote) {
        try {
            // Complete the vote (this will assign spanner if vote passed)
            spannerVoteService.completeVote(vote);
            
            // Skip Discord interactions if JDA is not available (e.g., in test mode)
            if (jda == null) {
                LOGGER.debug("JDA not available, skipping Discord notification for vote {}", vote.getId());
                return;
            }
            
            // Get the target user
            User targetUser = jda.getUserById(vote.getTargetUserId());
            if (targetUser == null) {
                LOGGER.warn("Could not find user {} for vote completion", vote.getTargetUserId());
                return;
            }
            
            // Get the channel
            TextChannel channel = jda.getTextChannelById(vote.getChannelId());
            if (channel == null) {
                LOGGER.warn("Could not find channel {} for vote completion", vote.getChannelId());
                return;
            }
            
            // Send completion message
            notificationService.sendVoteCompletedMessage(channel, vote, targetUser, vote.hasVotePassed());
            
            LOGGER.info("Processed expired vote {} - result: {}", 
                    vote.getId(), vote.hasVotePassed() ? "PASSED" : "FAILED");
                    
        } catch (Exception e) {
            LOGGER.error("Error processing expired vote {}", vote.getId(), e);
        }
    }
}