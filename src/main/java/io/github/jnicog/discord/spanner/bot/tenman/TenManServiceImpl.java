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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
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
        boolean alreadyActive = pollRepository.findByChannelIdAndStatus(channelId, TenManPollStatus.ACTIVE).isPresent();
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

        int capacity = dateOption.getPoll().isTestMode() ? TEST_CAPACITY : CAPACITY;

        boolean exists = signupRepository.existsByDateOptionIdAndUserId(dateOptionId, userId);
        if (exists) {
            signupRepository.findByDateOptionIdAndUserId(dateOptionId, userId)
                    .ifPresent(signupRepository::delete);
            signupRepository.flush();
            int newCount = signupRepository.countByDateOptionId(dateOptionId);
            return new TenManSignupToggleResult(false, dateOption.getPoll().getId(), dateOptionId, newCount, false);
        } else {
            TenManSignupEntity signup = new TenManSignupEntity(dateOption, userId);
            signupRepository.save(signup);
            signupRepository.flush();
            int newCount = signupRepository.countByDateOptionId(dateOptionId);
            boolean dateFull = newCount >= capacity;
            return new TenManSignupToggleResult(true, dateOption.getPoll().getId(), dateOptionId, newCount, dateFull);
        }
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
                .map(poll -> {
                    poll.setStatus(TenManPollStatus.CLOSED);
                    pollRepository.save(poll);
                    return poll.getDiscordMessageId(); // may be null if message send failed
                });
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
        return new TenManPollSnapshot(poll.getId(), poll.getChannelId(), messageId, poll.getTimeDisplay(), poll.isTestMode(), poll.getClosesAt(), dateSnapshots);
    }

    @Override
    public List<TenManPollSnapshot> processExpiredPolls() {
        List<TenManPollEntity> expired = pollRepository.findByStatusAndClosesAtBefore(TenManPollStatus.ACTIVE, OffsetDateTime.now());
        return expired.stream().map(poll -> {
            poll.setStatus(TenManPollStatus.CLOSED);
            pollRepository.save(poll);
            return buildSnapshot(poll);
        }).toList();
    }
}
