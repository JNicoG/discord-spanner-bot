package io.github.jnicog.discord.spanner.bot.controller;

import io.github.jnicog.discord.spanner.bot.service.ChannelQueueManager;
import io.github.jnicog.discord.spanner.bot.service.NotificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@ExtendWith(MockitoExtension.class)
public class QueueControllerTest {

    @Mock
    private static ChannelQueueManager queueManager;

    @Mock
    private static NotificationService notificationService;

    private static final QueueController queueController = new QueueController(queueManager, notificationService);

    public QueueControllerTest() {
        // Empty constructor
    }

    @BeforeAll
    public static void setup() {
        Assertions.assertNotNull(queueManager);
        Assertions.assertNotNull(notificationService);
        Assertions.assertNotNull(queueController);
    }

}
