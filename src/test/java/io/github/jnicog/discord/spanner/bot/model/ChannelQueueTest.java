package io.github.jnicog.discord.spanner.bot.model;

import io.github.jnicog.discord.spanner.bot.config.QueueProperties;
import io.github.jnicog.discord.spanner.bot.service.QueueEventPublisher;
import io.github.jnicog.discord.spanner.bot.service.SpannerService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class ChannelQueueTest {

    @Mock
    private MessageChannel messageChannel;
    
    @Mock
    private QueueProperties queueProperties;
    
    @Mock
    private SpannerService spannerService;
    
    @Mock
    private QueueEventPublisher eventPublisher;
    
    @Mock
    private User user1;

    private ChannelQueue channelQueue;

    @BeforeEach
    void setUp() {
        // Setup mock properties with lenient stubbing to avoid unnecessary stubbing warnings
        lenient().when(queueProperties.getMaxQueueSize()).thenReturn(5);
        lenient().when(queueProperties.getUserTimeoutLength()).thenReturn(10); // 10 seconds for test
        lenient().when(queueProperties.getUserTimeoutUnit()).thenReturn(TimeUnit.SECONDS);
        lenient().when(queueProperties.getCheckInTimeoutLength()).thenReturn(30);
        lenient().when(queueProperties.getCheckInTimeoutUnit()).thenReturn(TimeUnit.SECONDS);

        lenient().when(messageChannel.getIdLong()).thenReturn(12345L);
        lenient().when(user1.getIdLong()).thenReturn(100L);
        lenient().when(user1.getName()).thenReturn("TestUser1");

        channelQueue = new ChannelQueue(messageChannel, queueProperties, spannerService, eventPublisher);
    }

    @Test
    void testRefreshPlayerSuccess() {
        // First add the player
        boolean added = channelQueue.addPlayer(user1, messageChannel);
        assertTrue(added, "Player should be added successfully");
        assertTrue(channelQueue.getPlayers().contains(user1), "Queue should contain the user");

        // Now refresh the player
        boolean refreshed = channelQueue.refreshPlayer(user1);
        assertTrue(refreshed, "Player should be refreshed successfully");
        assertTrue(channelQueue.getPlayers().contains(user1), "Queue should still contain the user after refresh");
    }

    @Test
    void testRefreshPlayerNotInQueue() {
        // Try to refresh a player that's not in the queue
        boolean refreshed = channelQueue.refreshPlayer(user1);
        assertFalse(refreshed, "Should not be able to refresh a player not in queue");
    }

    @Test
    void testRefreshPlayerKeepsThemInQueue() throws InterruptedException {
        // Add player
        boolean added = channelQueue.addPlayer(user1, messageChannel);
        assertTrue(added, "Player should be added successfully");

        // Refresh the player (this should reset their timeout)
        boolean refreshed = channelQueue.refreshPlayer(user1);
        assertTrue(refreshed, "Player should be refreshed successfully");

        // Player should still be in queue after refresh
        assertTrue(channelQueue.getPlayers().contains(user1), 
                "Player should still be in queue after refresh");
    }
}
