package io.github.jnicog.discord.spanner.bot.tenman;

import io.github.jnicog.discord.spanner.bot.repository.TenManDateOptionRepository;
import io.github.jnicog.discord.spanner.bot.repository.TenManPollRepository;
import io.github.jnicog.discord.spanner.bot.repository.TenManSignupRepository;
import io.github.jnicog.discord.spanner.bot.repository.entity.TenManDateOptionEntity;
import io.github.jnicog.discord.spanner.bot.repository.entity.TenManPollEntity;
import io.github.jnicog.discord.spanner.bot.repository.entity.TenManPollStatus;
import io.github.jnicog.discord.spanner.bot.repository.entity.TenManSignupEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class TenManServiceImpl implements TenManService {

    private static final int CAPACITY = 10;
    private static final int TEST_CAPACITY = 1;

    private final TenManPollRepository pollRepository;
    private final TenManDateOptionRepository dateOptionRepository;
    private final TenManSignupRepository signupRepository;

    public TenManServiceImpl(TenManPollRepository pollRepository,
                              TenManDateOptionRepository dateOptionRepository,
                              TenManSignupRepository signupRepository) {
        this.pollRepository = pollRepository;
        this.dateOptionRepository = dateOptionRepository;
        this.signupRepository = signupRepository;
    }

    @Override
    public Optional<TenManPollCreatedResult> createPoll(long channelId, long createdByUserId, List<LocalDate> dates, String timeDisplay, boolean testMode, OffsetDateTime closesAt) {
        boolean alreadyActive = pollRepository.findByChannelIdAndStatus(channelId, TenManPollStatus.ACTIVE).isPresent()
                || pollRepository.findByChannelIdAndStatus(channelId, TenManPollStatus.LOCKED).isPresent();
        if (alreadyActive) {
            return Optional.empty();
        }

        TenManPollEntity poll = new TenManPollEntity(channelId, createdByUserId, timeDisplay, testMode, closesAt);
        pollRepository.save(poll);

        List<TenManDateOptionEntity> options = dates.stream()
                .map(date -> new TenManDateOptionEntity(poll, date))
                .toList();
        dateOptionRepository.saveAll(options);

        List<TenManDateOptionSnapshot> snapshots = options.stream()
                .map(o -> new TenManDateOptionSnapshot(o.getId(), o.getOptionDate(), List.of()))
                .toList();

        return Optional.of(new TenManPollCreatedResult(poll.getId(), timeDisplay, testMode, closesAt, snapshots));
    }

    @Override
    public TenManSignupToggleResult toggleSignup(long dateOptionId, long userId) {
        TenManDateOptionEntity dateOption = dateOptionRepository.findById(dateOptionId)
                .orElseThrow(() -> new IllegalArgumentException("Date option not found: " + dateOptionId));

        TenManPollEntity poll = dateOption.getPoll();
        TenManPollStatus pollStatus = poll.getStatus();
        int currentCount = signupRepository.countByDateOptionId(dateOptionId);

        if (pollStatus == TenManPollStatus.CLOSED) {
            return new TenManSignupToggleResult(TenManSignupToggleResult.ToggleStatus.POLL_CLOSED, poll.getId(), dateOptionId, currentCount);
        }

        if (pollStatus == TenManPollStatus.LOCKED) {
            Long lockedId = poll.getLockedDateOptionId();
            if (lockedId == null || !Objects.equals(lockedId, dateOptionId)) {
                return new TenManSignupToggleResult(TenManSignupToggleResult.ToggleStatus.WRONG_DATE_FOR_LOCKED_POLL, poll.getId(), dateOptionId, currentCount);
            }
            boolean isOnRoster = signupRepository.existsByDateOptionIdAndUserId(dateOptionId, userId);
            if (!isOnRoster) {
                return new TenManSignupToggleResult(TenManSignupToggleResult.ToggleStatus.NOT_ON_LOCKED_ROSTER, poll.getId(), dateOptionId, currentCount);
            }
            signupRepository.findByDateOptionIdAndUserId(dateOptionId, userId).ifPresent(signupRepository::delete);
            signupRepository.flush();
            int newCount = signupRepository.countByDateOptionId(dateOptionId);
            return new TenManSignupToggleResult(TenManSignupToggleResult.ToggleStatus.RESIGNED_FROM_LOCKED, poll.getId(), dateOptionId, newCount);
        }

        // ACTIVE poll
        int capacity = poll.isTestMode() ? TEST_CAPACITY : CAPACITY;
        boolean exists = signupRepository.existsByDateOptionIdAndUserId(dateOptionId, userId);
        if (exists) {
            signupRepository.findByDateOptionIdAndUserId(dateOptionId, userId).ifPresent(signupRepository::delete);
            signupRepository.flush();
            int newCount = signupRepository.countByDateOptionId(dateOptionId);
            return new TenManSignupToggleResult(TenManSignupToggleResult.ToggleStatus.LEFT, poll.getId(), dateOptionId, newCount);
        } else {
            TenManSignupEntity signup = new TenManSignupEntity(dateOption, userId);
            signupRepository.save(signup);
            signupRepository.flush();
            int newCount = signupRepository.countByDateOptionId(dateOptionId);
            if (newCount >= capacity) {
                return new TenManSignupToggleResult(TenManSignupToggleResult.ToggleStatus.DATE_FULL, poll.getId(), dateOptionId, newCount);
            }
            return new TenManSignupToggleResult(TenManSignupToggleResult.ToggleStatus.JOINED, poll.getId(), dateOptionId, newCount);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getLockedDateOptionId(long channelId) {
        return pollRepository.findByChannelIdAndStatus(channelId, TenManPollStatus.LOCKED)
                .map(TenManPollEntity::getLockedDateOptionId);
    }

    @Override
    public void lockPoll(long pollId, long lockedDateOptionId) {
        pollRepository.findById(pollId).ifPresent(poll -> {
            poll.setStatus(TenManPollStatus.LOCKED);
            poll.setLockedDateOptionId(lockedDateOptionId);
            pollRepository.save(poll);
        });
    }

    @Override
    public TenManResignSlashResult resignFromLockedRoster(long channelId, long userId) {
        Optional<TenManPollEntity> pollOpt = pollRepository.findByChannelIdAndStatus(channelId, TenManPollStatus.LOCKED);
        if (pollOpt.isEmpty()) {
            return new TenManResignSlashResult.NoPoll();
        }
        TenManPollEntity poll = pollOpt.get();
        Long lockedDateOptionId = poll.getLockedDateOptionId();
        if (lockedDateOptionId == null) {
            return new TenManResignSlashResult.NoPoll();
        }
        boolean isOnRoster = signupRepository.existsByDateOptionIdAndUserId(lockedDateOptionId, userId);
        if (!isOnRoster) {
            return new TenManResignSlashResult.NotOnRoster();
        }
        signupRepository.findByDateOptionIdAndUserId(lockedDateOptionId, userId).ifPresent(signupRepository::delete);
        signupRepository.flush();
        int newCount = signupRepository.countByDateOptionId(lockedDateOptionId);
        int capacity = poll.isTestMode() ? TEST_CAPACITY : CAPACITY;
        int slotsNeeded = capacity - newCount;
        TenManDateOptionEntity dateOption = dateOptionRepository.findById(lockedDateOptionId).orElseThrow();
        return new TenManResignSlashResult.Resigned(poll.getId(), channelId, lockedDateOptionId, dateOption.getOptionDate(), poll.getTimeDisplay(), slotsNeeded);
    }

    @Override
    public TenManFillSlashResult fillRosterSlot(long channelId, long userId) {
        Optional<TenManPollEntity> pollOpt = pollRepository.findByChannelIdAndStatus(channelId, TenManPollStatus.LOCKED);
        if (pollOpt.isEmpty()) {
            return new TenManFillSlashResult.NoPoll();
        }
        TenManPollEntity poll = pollOpt.get();
        Long lockedDateOptionId = poll.getLockedDateOptionId();
        if (lockedDateOptionId == null) {
            return new TenManFillSlashResult.NoPoll();
        }
        int capacity = poll.isTestMode() ? TEST_CAPACITY : CAPACITY;
        int currentCount = signupRepository.countByDateOptionId(lockedDateOptionId);
        if (currentCount >= capacity) {
            return new TenManFillSlashResult.NoSlots();
        }
        if (signupRepository.existsByDateOptionIdAndUserId(lockedDateOptionId, userId)) {
            return new TenManFillSlashResult.AlreadyOnRoster();
        }
        TenManDateOptionEntity dateOption = dateOptionRepository.findById(lockedDateOptionId).orElseThrow();
        signupRepository.save(new TenManSignupEntity(dateOption, userId));
        signupRepository.flush();
        int newCount = signupRepository.countByDateOptionId(lockedDateOptionId);
        boolean rosterRestored = newCount >= capacity;
        List<Long> rosterUserIds = signupRepository.findByDateOptionId(lockedDateOptionId).stream()
                .map(TenManSignupEntity::getUserId).toList();
        return new TenManFillSlashResult.Filled(rosterRestored, poll.getId(), channelId, lockedDateOptionId, dateOption.getOptionDate(), poll.getTimeDisplay(), newCount, capacity, capacity - newCount, rosterUserIds);
    }

    @Override
    public void registerMessageId(long pollId, long discordMessageId) {
        pollRepository.findById(pollId).ifPresent(poll -> {
            poll.setDiscordMessageId(discordMessageId);
            pollRepository.save(poll);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenManPollSnapshot> getActivePollByMessageId(long discordMessageId) {
        return pollRepository.findByDiscordMessageId(discordMessageId)
                .map(this::buildSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public TenManPollSnapshot getPollSnapshot(long pollId) {
        TenManPollEntity poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("Poll not found: " + pollId));
        return buildSnapshot(poll);
    }

    @Override
    public Optional<Long> cancelPoll(long channelId) {
        return pollRepository.findByChannelIdAndStatus(channelId, TenManPollStatus.ACTIVE)
                .or(() -> pollRepository.findByChannelIdAndStatus(channelId, TenManPollStatus.LOCKED))
                .map(poll -> {
                    poll.setStatus(TenManPollStatus.CLOSED);
                    pollRepository.save(poll);
                    return poll.getDiscordMessageId();
                });
    }

    @Override
    public List<TenManPollSnapshot> processExpiredPolls() {
        OffsetDateTime now = OffsetDateTime.now();
        List<TenManPollEntity> expiredActive = pollRepository.findByStatusAndClosesAtBefore(TenManPollStatus.ACTIVE, now);
        List<TenManPollEntity> expiredLocked = pollRepository.findByStatusAndClosesAtBefore(TenManPollStatus.LOCKED, now);
        return java.util.stream.Stream.concat(expiredActive.stream(), expiredLocked.stream())
                .map(poll -> {
                    TenManPollSnapshot snapshot = buildSnapshot(poll);
                    poll.setStatus(TenManPollStatus.CLOSED);
                    pollRepository.save(poll);
                    return snapshot;
                }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenManPollSnapshot> getPollsClosingSoon(Duration window) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime cutoff = now.plus(window);
        return pollRepository.findByStatusAndClosesAtBetween(TenManPollStatus.ACTIVE, now, cutoff).stream()
                .map(this::buildSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenManPollSnapshot> getActivePollSnapshot(long channelId) {
        return pollRepository.findByChannelIdAndStatus(channelId, TenManPollStatus.ACTIVE)
                .or(() -> pollRepository.findByChannelIdAndStatus(channelId, TenManPollStatus.LOCKED))
                .map(this::buildSnapshot);
    }

    private TenManPollSnapshot buildSnapshot(TenManPollEntity poll) {
        List<TenManDateOptionEntity> options = dateOptionRepository.findByPollIdOrderByOptionDateAsc(poll.getId());
        List<TenManDateOptionSnapshot> dateSnapshots = options.stream()
                .map(opt -> {
                    List<Long> userIds = signupRepository.findByDateOptionId(opt.getId()).stream()
                            .map(TenManSignupEntity::getUserId)
                            .toList();
                    return new TenManDateOptionSnapshot(opt.getId(), opt.getOptionDate(), userIds);
                })
                .toList();
        long messageId = poll.getDiscordMessageId() != null ? poll.getDiscordMessageId() : 0L;
        return new TenManPollSnapshot(poll.getId(), poll.getChannelId(), messageId, poll.getTimeDisplay(), poll.isTestMode(), poll.getClosesAt(), poll.getLockedDateOptionId(), dateSnapshots);
    }
}
