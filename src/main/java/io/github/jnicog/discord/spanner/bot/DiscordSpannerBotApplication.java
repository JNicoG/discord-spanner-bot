package io.github.jnicog.discord.spanner.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@Import({DiscordSpannerBotConfig.class})
@EnableJpaRepositories("io.github.jnicog.discord.spanner.bot.repository")
@EntityScan("io.github.jnicog.discord.spanner.bot.model")
public class DiscordSpannerBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscordSpannerBotApplication.class, args);
	}

}
