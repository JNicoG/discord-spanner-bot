package io.github.jnicog.discord.spanner.bot;

import io.github.jnicog.discord.spanner.bot.controller.QueueController;
import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import io.github.jnicog.discord.spanner.bot.service.ChannelQueueManager;
import io.github.jnicog.discord.spanner.bot.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/*@Import({TestContainersConfiguration.class})*/
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TestConfig.class})
@ActiveProfiles("test")
class DiscordSpannerBotApplicationTests {

	@Autowired
	private QueueController queueController;

	@Autowired
	private ChannelQueueManager channelQueueManager;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private SpannerRepository spannerRepository;

	@Test
	void contextLoads() {
		assertNotNull(queueController);
		assertNotNull(channelQueueManager);
		assertNotNull(notificationService);
		assertNotNull(spannerRepository);
	}

}
