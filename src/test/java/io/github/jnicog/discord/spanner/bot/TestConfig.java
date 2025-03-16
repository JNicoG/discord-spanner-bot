package io.github.jnicog.discord.spanner.bot;

import io.github.jnicog.discord.spanner.bot.repository.SpannerRepository;
import io.github.jnicog.discord.spanner.bot.service.NotificationService;
import net.dv8tion.jda.api.JDA;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestConfiguration
public class TestConfig {

    @MockitoBean
    private JDA jda;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private SpannerRepository spannerRepository;

}
