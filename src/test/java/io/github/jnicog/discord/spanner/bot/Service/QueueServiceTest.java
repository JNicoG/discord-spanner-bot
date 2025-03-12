package io.github.jnicog.discord.spanner.bot.Service;

import io.github.jnicog.discord.spanner.bot.service.NotifyService;
import io.github.jnicog.discord.spanner.bot.service.NotifyServiceImpl;
import io.github.jnicog.discord.spanner.bot.service.QueueServiceImpl;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@ExtendWith(MockitoExtension.class)
public class QueueServiceTest {

    private final NotifyService notifyService = Mockito.mock(NotifyServiceImpl.class);

    @Mock
    private SlashCommandInteractionEvent slashCommandInteractionEvent;

    private final QueueServiceImpl queueService = new QueueServiceImpl(notifyService);

    private static final int MAX_QUEUE_SIZE = 5;

    @Autowired
    public QueueServiceTest() {
        // Empty constructor
    }

    @BeforeAll()
    public static void setup() {

    }

    @Test
    public void checkNotNull() {
        Assertions.assertNotNull(queueService);
        Assertions.assertNotNull(notifyService);
    }

    @Test
    public void testJoinPlayerQueue_emptyQueue_notInQueue_expectAddToQueue() {
        // Given
        User user = Mockito.mock(User.class);
        Mockito.when(slashCommandInteractionEvent.getUser()).thenReturn(user);

        // When
        queueService.joinPlayerQueue(slashCommandInteractionEvent);

        // Then
        Assertions.assertEquals(1, queueService.getPlayerQueue().size());
        Assertions.assertTrue(queueService.getPlayerQueue().containsKey(user));
        Assertions.assertNotNull(queueService.getPlayerQueue().get(user));
        Assertions.assertFalse(queueService.isPlayerQueueFull());
        Assertions.assertFalse(queueService.getQueuePoppedState());
    }

    @Test
    public void testJoinPlayerQueue_oneInQueue_expectTwoInQueue() {
        // Given
        User user1 = Mockito.mock(User.class);
        User user2 = Mockito.mock(User.class);
        queueService.getPlayerQueue().put(user1, System.currentTimeMillis());

        Mockito.when(slashCommandInteractionEvent.getUser()).thenReturn(user2);

        // When
        queueService.joinPlayerQueue(slashCommandInteractionEvent);

        // Then
        Assertions.assertEquals(2, queueService.getPlayerQueue().size());
        Assertions.assertTrue(queueService.getPlayerQueue().containsKey(user1));
        Assertions.assertNotNull(queueService.getPlayerQueue().get(user1));
        Assertions.assertTrue(queueService.getPlayerQueue().containsKey(user2));
        Assertions.assertNotNull(queueService.getPlayerQueue().get(user2));
        Assertions.assertFalse(queueService.isPlayerQueueFull());
        Assertions.assertFalse(queueService.getQueuePoppedState());
    }

    @Test
    public void testJoinPlayerQueue_maxQueueSizeMinusOne_expectAddPlayerAndQueuePop() {
        // Given
        User user1 = Mockito.mock(User.class);
        User user2 = Mockito.mock(User.class);
        User user3 = Mockito.mock(User.class);
        User user4 = Mockito.mock(User.class);
        queueService.getPlayerQueue().put(user1, System.currentTimeMillis());
        queueService.getPlayerQueue().put(user2, System.currentTimeMillis());
        queueService.getPlayerQueue().put(user3, System.currentTimeMillis());
        queueService.getPlayerQueue().put(user4, System.currentTimeMillis());

        User user5 = Mockito.mock(User.class);
        Mockito.when(slashCommandInteractionEvent.getUser()).thenReturn(user5);

        // When
        queueService.joinPlayerQueue(slashCommandInteractionEvent);

        // Then
        Assertions.assertEquals(5, queueService.getPlayerQueue().size());
        Assertions.assertTrue(queueService.getPlayerQueue().containsKey(user5));
        Assertions.assertNotNull(queueService.getPlayerQueue().get(user5));
        Assertions.assertTrue(queueService.isPlayerQueueFull());
        Assertions.assertTrue(queueService.getQueuePoppedState());
    }

    @Test
    public void testJoinPlayerQueue_userAlreadyInQueue_expectNoChange() {
        // Given
        User user1 = Mockito.mock(User.class);
        queueService.getPlayerQueue().put(user1, System.currentTimeMillis());

        Mockito.when(slashCommandInteractionEvent.getUser()).thenReturn(user1);

        // When
        queueService.joinPlayerQueue(slashCommandInteractionEvent);

        // Then
        Assertions.assertEquals(5, queueService.getPlayerQueue().size());
        Assertions.assertTrue(queueService.getPlayerQueue().containsKey(user1));
        Assertions.assertNotNull(queueService.getPlayerQueue().get(user1));
        Assertions.assertFalse(queueService.isPlayerQueueFull());
        Assertions.assertFalse(queueService.getQueuePoppedState());
    }

    @Test
    public void testJoinPlayerQueue_queueAlreadyFull_expectNoChange() {
        // Given
        User user1 = Mockito.mock(User.class);
        User user2 = Mockito.mock(User.class);
        User user3 = Mockito.mock(User.class);
        User user4 = Mockito.mock(User.class);
        User user5 = Mockito.mock(User.class);
        queueService.getPlayerQueue().put(user1, System.currentTimeMillis());
        queueService.getPlayerQueue().put(user2, System.currentTimeMillis());
        queueService.getPlayerQueue().put(user3, System.currentTimeMillis());
        queueService.getPlayerQueue().put(user4, System.currentTimeMillis());
        queueService.getPlayerQueue().put(user5, System.currentTimeMillis());
        queueService.setQueuePoppedState();

        User user6 = Mockito.mock(User.class);
        Mockito.when(slashCommandInteractionEvent.getUser()).thenReturn(user6);

        // When
        queueService.joinPlayerQueue(slashCommandInteractionEvent);

        // Then
        Assertions.assertEquals(5, queueService.getPlayerQueue().size());
        Assertions.assertFalse(queueService.getPlayerQueue().containsKey(user6));
        Assertions.assertNull(queueService.getPlayerQueue().get(user6));
        Assertions.assertTrue(queueService.isPlayerQueueFull());
        Assertions.assertTrue(queueService.getQueuePoppedState());
    }

    @Test
    public void testJoinPlayerQueue_timeoutTaskCreated() {
        // Given
        User user1 = Mockito.mock(User.class);
        Mockito.when(slashCommandInteractionEvent.getUser()).thenReturn(user1);

        // When
        queueService.joinPlayerQueue(slashCommandInteractionEvent);

        // Then
        Assertions.assertEquals(1, queueService.getPlayerQueue().size());
        Assertions.assertFalse(queueService.getPlayerQueue().containsKey(user1));
        Assertions.assertNull(queueService.getPlayerQueue().get(user1));
        Assertions.assertFalse(queueService.isPlayerQueueFull());
        Assertions.assertFalse(queueService.getQueuePoppedState());
        Assertions.assertTrue(queueService.getTimeoutTasksMap().containsKey(user1));
        Assertions.assertNotNull(queueService.getTimeoutTasksMap().get(user1));
    }

    /**
     * TODO:
     * Test remaining methods
     */

}
