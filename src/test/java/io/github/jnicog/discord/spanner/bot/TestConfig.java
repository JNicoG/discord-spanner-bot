package io.github.jnicog.discord.spanner.bot;

import net.dv8tion.jda.api.JDA;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;

@TestConfiguration
public class TestConfig {

    @Bean
    public JDA TestJdaConfig() {
        JDA mock = Mockito.mock(JDA.class);
        // Set up mock behaviour here if needed
        // Mockito.when(mock.someMethod()).thenReturn(someValue);
        return mock;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return Mockito.mock(TaskScheduler.class);
    }

}
