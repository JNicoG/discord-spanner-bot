package io.github.jnicog.discord.spanner.bot.controller;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import io.github.jnicog.discord.spanner.bot.service.ChannelQueueManager;
import io.github.jnicog.discord.spanner.bot.service.NotificationService;
import io.github.jnicog.discord.spanner.bot.service.SpannerService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.TestComponent;

import java.util.EnumSet;

import static org.mockito.Mockito.*;

@TestComponent
@ExtendWith(MockitoExtension.class)
public class QueueControllerTest {

    @Mock
    private ChannelQueueManager queueManager;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SpannerService spannerService;

    @Mock
    private SlashCommandInteractionEvent slashEvent;

    @Mock
    private MessageChannelUnion messageChannel;

    @Mock
    private ChannelQueue channelQueue;

    @Mock
    private User user;

    private QueueController queueController;

    @BeforeEach
    void setUp() {
        queueController = new QueueController(queueManager, notificationService, spannerService);
        
        when(slashEvent.getName()).thenReturn("keen");
        when(slashEvent.getUser()).thenReturn(user);
        when(slashEvent.getChannel()).thenReturn(messageChannel);
        when(slashEvent.getMessageChannel()).thenReturn(messageChannel);
        when(queueManager.getOrCreateQueue(messageChannel)).thenReturn(channelQueue);
    }

    @Test
    void testKeenCommandRefreshesExistingPlayer() {
        // Setup: Player already in queue, not full
        when(channelQueue.addPlayer(user, messageChannel)).thenReturn(false);
        when(channelQueue.isFull()).thenReturn(false);
        when(channelQueue.refreshPlayer(user)).thenReturn(true);

        // Execute
        queueController.onSlashCommandInteraction(slashEvent);

        // Verify refresh message is sent
        verify(notificationService).sendReply(
                eq(slashEvent),
                eq("You are already in this queue! Your keen has been refreshed."),
                eq(true),
                eq(true),
                eq(EnumSet.of(Message.MentionType.USER))
        );
        
        verify(channelQueue).refreshPlayer(user);
    }

    @Test
    void testKeenCommandPlayerAlreadyInQueueButRefreshFails() {
        // Setup: Player already in queue, not full, but refresh fails
        when(channelQueue.addPlayer(user, messageChannel)).thenReturn(false);
        when(channelQueue.isFull()).thenReturn(false);
        when(channelQueue.refreshPlayer(user)).thenReturn(false);

        // Execute
        queueController.onSlashCommandInteraction(slashEvent);

        // Verify normal "already in queue" message is sent
        verify(notificationService).sendReply(
                eq(slashEvent),
                eq("You are already in this queue!"),
                eq(true),
                eq(true),
                eq(EnumSet.of(Message.MentionType.USER))
        );
        
        verify(channelQueue).refreshPlayer(user);
    }

}
