package io.github.jnicog.discord.spanner.bot.service;

import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import static io.github.jnicog.discord.spanner.bot.service.Constants.awaitingButton;
import static io.github.jnicog.discord.spanner.bot.service.Constants.checkMarkEmoji;

@Service
public class AcceptServiceImpl implements AcceptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptServiceImpl.class);

    private final NotifyService notifyService;
    private final QueueService queueService;
    private final SpannerRepository spannerRepository;

    private long activeQueueMessageId = -1;

    private final ConcurrentHashMap<User, AcceptState> userAcceptStateMap = new ConcurrentHashMap<>(5);

    // Consider moving to properties / config
    public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    public static final int ACCEPT_TIMEOUT_LENGTH = 5;

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
    public void initialiseAcceptQueue(long queuePopMessageId, MessageChannel messageChannel) {
        userAcceptStateMap.clear();

        Set<User> activeQueue = queueService.showQueue();
        for (User user : activeQueue) {
            userAcceptStateMap.put(user, AWAITING);
        }

        this.activeQueueMessageId = queuePopMessageId;

        LOGGER.info("Initialised accept state tracking with message ID {} for current queue: {}",
                queuePopMessageId, activeQueue);

        if (messageChannel != null) {
            setupTimeoutTask(messageChannel);
        } else {
            LOGGER.error("Failed to set up accept queue timeout task: Message channel is null");
        }
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

        checkAllAccepted(buttonInteractionEvent);
    }

    private void checkAllAccepted(ButtonInteractionEvent buttonInteractionEvent) {
        boolean allAccepted = userAcceptStateMap.values().stream()
                .noneMatch(state -> state == AWAITING);

        if (!allAccepted) {
            notifyService.notifyPoppedQueuePlayerAccept(buttonInteractionEvent, formatQueueStatusMessage(AWAITING));
        } else {
            LOGGER.info("All users have accepted the queue");

            notifyService.notifyPoppedQueueAccepted(buttonInteractionEvent, formatQueueStatusMessage(ACCEPTED));

            reset();
        }
    }

    private String formatQueueStatusMessage(AcceptState messageFormat) {
        String userStatuses = userAcceptStateMap.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    AcceptState acceptState = entry.getValue();
                    return user.getAsMention()
                            + (acceptState == AcceptState.ACCEPTED ?
                            String.format(" [%s]", checkMarkEmoji)
                            : String.format(" [%s]", awaitingButton));
                })
                .collect(Collectors.joining(" | "));

        return messageFormat.equals(AWAITING) ? String.format("The queue has been filled!" +
                "\nClick the %s button within %d %s to accept. " +
                "\nWaiting for all players to accept... %s",
                checkMarkEmoji, ACCEPT_TIMEOUT_LENGTH, TIME_UNIT.toString().toLowerCase(), userStatuses)
                : String.format("All players have accepted." +
                "\n%s", userStatuses);
    }

    @Override
    public boolean isActiveQueueMessage(long messageId) {
        boolean isActive = messageId == getActiveQueueMessageId();

        if (!isActive && messageId != -1 && getActiveQueueMessageId() != -1) {
            LOGGER.debug("Message ID validation failed: provided={}, active={}", messageId, getActiveQueueMessageId());
        }

        return isActive;
    }

    private void setupTimeoutTask(MessageChannel messageChannel) {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }

        timeoutTask = scheduler.schedule(
                () -> handleQueueTimeout(messageChannel),
                ACCEPT_TIMEOUT_LENGTH,
                TIME_UNIT
        );

        LOGGER.info("Set up queue accept service timeout for {} {}", ACCEPT_TIMEOUT_LENGTH, TIME_UNIT);
    }

    private void handleQueueTimeout(MessageChannel messageChannel) {
        LOGGER.info("Queue accept service timeout limit reached. Message channel: {}, messageID: {}",
                messageChannel, getActiveQueueMessageId());

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
            notifyService.editPoppedQueueMessage(messageChannel, getActiveQueueMessageId(),
                    String.format("One or more keeners did not check-in." +
                            "\nThe following players did not respond and received a spanner:" +
                            "\n%s" +
                            "\nThe current queue will be cleared. Use /k to join a new queue.",
                            nonResponders));
        }

        reset();
    }

    public long getActiveQueueMessageId() {
        return this.activeQueueMessageId;
    }

    private void setActiveQueueMessageId(long messageId) {
        this.activeQueueMessageId = messageId;
    }

    private Map<User, AcceptState> getAcceptStateMap() {
        return this.userAcceptStateMap;
    }

    @Override
    public void cancelActiveQueue() {
        timeoutTask.cancel(false);
        setActiveQueueMessageId(-1);
    }

    private synchronized void reset() {
        queueService.resetPlayerQueue();
        timeoutTask.cancel(false);
        getAcceptStateMap().clear();
        setActiveQueueMessageId(-1);
    }

}
