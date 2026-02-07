package io.github.jnicog.discord.spanner.bot;

import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import({TestContainersConfig.class, TestConfig.class})
class DiscordSpannerBotApplicationTests {

	@Autowired
	private JDA jda;

	@Test
	void contextLoads() {
		Assertions.assertNotNull(jda);
	}

}
