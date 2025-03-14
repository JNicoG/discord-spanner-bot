package io.github.jnicog.discord.spanner.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({DiscordSpannerBotConfig.class})
public class DiscordSpannerBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscordSpannerBotApplication.class, args);
	}

}
