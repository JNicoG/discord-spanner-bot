package io.github.jnicog.discord.spanner.bot.controller;

import io.github.jnicog.discord.spanner.bot.model.ChannelQueue;
import io.github.jnicog.discord.spanner.bot.service.ChannelQueueManager;
import io.github.jnicog.discord.spanner.bot.service.NotificationService;
import io.github.jnicog.discord.spanner.bot.service.QueuePenaltyService;
import io.github.jnicog.discord.spanner.bot.service.SpannerService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueueControllerPenaltyIntegrationTest {

    @Mock
    private ChannelQueueManager queueManager;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SpannerService spannerService;

    @Mock
    private QueuePenaltyService queuePenaltyService;

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private MessageChannelUnion messageChannel;

    @Mock
    private InteractionHook interactionHook;

    @SuppressWarnings("rawtypes") 
    @Mock
    private WebhookMessageCreateAction messageCreateAction;

    @Mock
    private User user;

    @Mock
    private ChannelQueue channelQueue;

    private QueueController queueController;

    @BeforeEach
    void setUp() {
        queueController = new QueueController(queueManager, notificationService, spannerService, queuePenaltyService);
        
        // Common mock setup
        when(event.getName()).thenReturn("unkeen");
        when(event.getUser()).thenReturn(user);
        when(event.getChannel()).thenReturn(messageChannel);
        when(user.getIdLong()).thenReturn(123456789L);
        when(queueManager.getOrCreateQueue(messageChannel)).thenReturn(channelQueue);
        when(channelQueue.getChannelId()).thenReturn(987654321L);
    }

    @Test
    void shouldPreventUnkeenWhenUserUnderCooldown() {
        // Given user is under cooldown
        when(queuePenaltyService.isUserUnderCooldown(123456789L, 987654321L)).thenReturn(true);
        when(queuePenaltyService.getRemainingCooldown(123456789L, 987654321L))
                .thenReturn(Duration.ofMinutes(5));

        // When user tries to use /unkeen
        queueController.onSlashCommandInteraction(event);

        // Then cooldown message should be sent and removePlayer should not be called
        verify(notificationService).sendReply(eq(event), contains("cooldown"), eq(true), eq(true), any());
        verify(channelQueue, never()).removePlayer(any(), anyBoolean());
        verify(queuePenaltyService, never()).applyPenalty(anyLong(), anyLong());
    }

    @Test
    void shouldApplyPenaltyWhenUserSuccessfullyUnkeens() {
        // Given user is not under cooldown
        when(queuePenaltyService.isUserUnderCooldown(123456789L, 987654321L)).thenReturn(false);
        when(channelQueue.removePlayer(user, true)).thenReturn(true);
        when(queuePenaltyService.applyPenalty(123456789L, 987654321L)).thenReturn(2); // Tier 2 penalty
        when(event.getHook()).thenReturn(interactionHook);
        when(interactionHook.sendMessage(anyString())).thenReturn(messageCreateAction);
        when(messageCreateAction.setEphemeral(true)).thenReturn(messageCreateAction);

        // When user uses /unkeen
        queueController.onSlashCommandInteraction(event);

        // Then penalty should be applied
        verify(channelQueue).removePlayer(user, true);
        verify(queuePenaltyService).applyPenalty(123456789L, 987654321L);
        verify(notificationService).sendQueueStatusUpdate(event, channelQueue);
        verify(event.getHook()).sendMessage(contains("Tier 2"));
        verify(messageCreateAction).setEphemeral(true);
    }

    @Test
    void shouldHandleUserNotInQueue() {
        // Given user is not under cooldown but not in queue
        when(queuePenaltyService.isUserUnderCooldown(123456789L, 987654321L)).thenReturn(false);
        when(channelQueue.removePlayer(user, true)).thenReturn(false);

        // When user uses /unkeen
        queueController.onSlashCommandInteraction(event);

        // Then error message should be sent and no penalty applied
        verify(notificationService).sendReply(eq(event), contains("not currently in the queue"), eq(true), eq(true), any());
        verify(queuePenaltyService, never()).applyPenalty(anyLong(), anyLong());
    }

}