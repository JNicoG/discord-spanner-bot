package io.github.jnicog.discord.spanner.bot;

import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DiscordSpannerBotApplicationTests extends AbstractIntegrationTest {

	@Autowired
	private JDA jda;

	@Test
	void contextLoads() {
	}

}
