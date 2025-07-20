package io.github.jnicog.discord.spanner.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import({DiscordSpannerBotConfig.class})
@EnableJpaRepositories("io.github.jnicog.discord.spanner.bot.repository")
@EntityScan("io.github.jnicog.discord.spanner.bot.model")
@EnableScheduling
public class DiscordSpannerBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscordSpannerBotApplication.class, args);
	}

}
