package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.model.Spanner;
import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.github.jnicog.discord.spanner.bot.service.AcceptState.ACCEPTED;
import static io.github.jnicog.discord.spanner.bot.service.AcceptState.AWAITING;

@Service
public class AcceptServiceImpl implements AcceptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptServiceImpl.class);

    private final NotifyService notifyService;
    private final QueueService queueService;
    private final SpannerRepository spannerRepository;

    private long activeQueueMessageId = 0;

    ConcurrentHashMap<User, AcceptState> userAcceptStateMap = new ConcurrentHashMap<>(5);

    private static final int ACCEPT_TIMEOUT_MINUTES = 3;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> timeoutTask;

    @Autowired
    public AcceptServiceImpl(QueueService queueService,
                             NotifyService notifyService,
                             SpannerRepository spannerRepository) {
        this.queueService = queueService;
        this.notifyService = notifyService;
        this.spannerRepository = spannerRepository;
    }

    @Override
    public void initialiseQueue(long queuePopMessageId) {
        userAcceptStateMap.clear();

        Set<User> activeQueue = queueService.showQueue();
        for (User user : activeQueue) {
            userAcceptStateMap.put(user, AWAITING);
        }

        this.activeQueueMessageId = queuePopMessageId;

        LOGGER.info("Initialised accept state tracking with message ID {} for current queue: {}",
                queuePopMessageId, activeQueue);

        setupTimeoutTask(messageChannel);
    }

    @Override
    public void playerAccept(ButtonInteractionEvent buttonInteractionEvent) {
        User user = buttonInteractionEvent.getUser();

        if (!userAcceptStateMap.containsKey(user)) {
            LOGGER.info("User {} is not a member of the queue.", buttonInteractionEvent.getUser().getName());
            notifyService.sendReply(buttonInteractionEvent,
                    "You are not a member of the queue!",
                    true);
            return;
        }
        userAcceptStateMap.put(buttonInteractionEvent.getUser(), ACCEPTED);
        LOGGER.info("User {} accepted the queue", user.getName());

        String updatedMessage = formatQueueStatusMessage();

        notifyService.notifyPoppedQueuePlayerAccept(buttonInteractionEvent, updatedMessage);

        checkAllAccepted(buttonInteractionEvent);
    }

    private String formatQueueStatusMessage() {
        String userStatuses = userAcceptStateMap.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    AcceptState acceptState = entry.getValue();
                    return user.getAsMention()
                            + (acceptState == AcceptState.ACCEPTED ?
                            String.format(" %s", Emoji.fromUnicode("U+2705"))
                            : String.format(" %s", Emoji.fromUnicode("U+1F527")));
                })
                .collect(Collectors.joining(" | "));

        return String.format("The queue has been filled! Click the %s button within 3 minutes to accept. %s",
                Emoji.fromUnicode("U+2705"),
                userStatuses);
    }

    private void checkAllAccepted(ButtonInteractionEvent buttonInteractionEvent) {
        boolean allAccepted = userAcceptStateMap.values().stream()
                .noneMatch(state -> state == AWAITING);

        if (allAccepted) {
            LOGGER.info("All users have accepted the queue");

            buttonInteractionEvent.deferEdit().queue();
            buttonInteractionEvent.getMessage().editMessage("All players have accepted!")
                    .setComponents(Collections.emptyList())
                    .queue();

            queueService.resetPlayerQueue();
            userAcceptStateMap.clear();
            activeQueueMessageId = 0;
        }
    }

    @Override
    public boolean isActiveQueueMessage(long messageId) {
        boolean isActive = messageId == activeQueueMessageId;

        if (!isActive && messageId != 0 && activeQueueMessageId != 0) {
            LOGGER.debug("Message ID validation failed: provided={}, active={}", messageId, activeQueueMessageId);
        }

        return isActive;
    }

    private void setupTimeoutTask(MessageChannel messageChannel) {
        if (timeoutTask != null && !timeoutTask.isDone()) {
            timeoutTask.cancel(false);
        }

        timeoutTask = scheduler.schedule(
                () -> handleQueueTimeout(messageChannel),
                ACCEPT_TIMEOUT_MINUTES,
                TimeUnit.MINUTES
        );

        LOGGER.info("Set up queue accept handler timeout for {} {}", ACCEPT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    private void handleQueueTimeout(MessageChannel messageChannel) {
        LOGGER.info("Queue accept handler timeout limit reached");

        Set<User> pendingUsers = userAcceptStateMap.entrySet().stream()
                .filter(entry -> entry.getValue() == AWAITING)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        for (User user : pendingUsers) {
            LOGGER.info("User {} did not accept in time and received a spanner", user.getName());
            spannerRepository.incrementSpannerCount(user.getIdLong());
        }

        String nonResponders = pendingUsers.stream()
                .map(User::getAsMention)
                .collect(Collectors.joining(", "));

        if (messageChannel != null) {
            messageChannel.editMessageById(activeQueueMessageId,
                            String.format("Queue timed out after %d minutes. " +
                                            "The following players did not respond and received a spanner: %s",
                                    ACCEPT_TIMEOUT_MINUTES, nonResponders))
                    .queue(
                            success -> LOGGER.info("Updated queue timeout message"),
                            error -> LOGGER.error("Failed to update queue timeout message: {}", error.getMessage())
                    );

            messageChannel.sendMessage("Queue has timed out. Use /k to join a new queue.")
                    .queue();
        }

        queueService.resetPlayerQueue();
        userAcceptStateMap.clear();
        activeQueueMessageId = 0;
    }

}
