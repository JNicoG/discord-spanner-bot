package io.github.jnicog.discord.spanner.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration properties for queue-related settings.
 * Values can be overridden in application.properties or application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "discord.queue")
public class QueueProperties {
    /**
     * Maximum number of players allowed in a queue.
     */
    private int maxQueueSize = 5;

    /**
     * Number of units to define how long a player can stay in the queue before being automatically removed.
     */
    private int userTimeoutLength = 1;

    /**
     * Time unit for how long a player can stay in the queue before being automatically removed.
     */
    private TimeUnit userTimeoutUnit = TimeUnit.HOURS;

    /**
     * Number of units to define how long players have to check in when the queue is filled.
     */
    private int checkInTimeoutLength = 5;

    /**
     * Time unit for how long players have to check in when the queue is filled.
     */
    private TimeUnit checkInTimeoutUnit = TimeUnit.MINUTES;

    /**
     * Number of units of inactivity before a queue is considered inactive and eligible for cleanup.
     */
    private int queueInactivityTimeoutLength = 120;

    /**
     * Time unit for queue inactivity
     */
    private TimeUnit queueInactivityTimeoutUnit = TimeUnit.MINUTES;


    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public int getUserTimeoutLength() {
        return userTimeoutLength;
    }

    public TimeUnit getUserTimeoutUnit() {
        return userTimeoutUnit;
    }

    public void setUserTimeoutUnit(TimeUnit timeUnit) {
        this.userTimeoutUnit = timeUnit;
    }

    public void setUserTimeoutLength(int userTimeoutLength) {
        this.userTimeoutLength = userTimeoutLength;
    }

    public int getCheckInTimeoutLength() {
        return checkInTimeoutLength;
    }

    public void setCheckInTimeoutLength(int checkInTimeoutLength) {
        this.checkInTimeoutLength = checkInTimeoutLength;
    }

    public TimeUnit getCheckInTimeoutUnit() {
        return checkInTimeoutUnit;
    }

    public void setCheckInTimeoutUnit(TimeUnit timeUnit) {
        this.checkInTimeoutUnit = timeUnit;
    }

    public int getQueueInactivityTimeoutLength() {
        return queueInactivityTimeoutLength;
    }

    public void setQueueInactivityTimeoutLength(int queueInactivityTimeoutLength) {
        this.queueInactivityTimeoutLength = queueInactivityTimeoutLength;
    }

    public TimeUnit getQueueInactivityTimeoutUnit() {
        return queueInactivityTimeoutUnit;
    }

    public void setQueueInactivityTimeoutUnit(TimeUnit timeUnit) {
        this.queueInactivityTimeoutUnit = timeUnit;
    }

}