package io.github.jnicog.discord.spanner.bot.service;

import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeUtilsTest {

    @Test
    void testFormatTimeUnit_Singular() {
        assertEquals("minute", TimeUtils.formatTimeUnit(1, TimeUnit.MINUTES));
        assertEquals("second", TimeUtils.formatTimeUnit(1, TimeUnit.SECONDS));
        assertEquals("hour", TimeUtils.formatTimeUnit(1, TimeUnit.HOURS));
        assertEquals("day", TimeUtils.formatTimeUnit(1, TimeUnit.DAYS));
    }

    @Test
    void testFormatTimeUnit_Plural() {
        assertEquals("minutes", TimeUtils.formatTimeUnit(2, TimeUnit.MINUTES));
        assertEquals("minutes", TimeUtils.formatTimeUnit(5, TimeUnit.MINUTES));
        assertEquals("seconds", TimeUtils.formatTimeUnit(30, TimeUnit.SECONDS));
        assertEquals("hours", TimeUtils.formatTimeUnit(24, TimeUnit.HOURS));
        assertEquals("days", TimeUtils.formatTimeUnit(7, TimeUnit.DAYS));
    }

    @Test
    void testFormatTimeUnit_Zero() {
        // Zero should be plural
        assertEquals("minutes", TimeUtils.formatTimeUnit(0, TimeUnit.MINUTES));
        assertEquals("seconds", TimeUtils.formatTimeUnit(0, TimeUnit.SECONDS));
    }

    @Test
    void testFormatTimeUnit_EdgeCases() {
        // Test with different time units
        assertEquals("microsecond", TimeUtils.formatTimeUnit(1, TimeUnit.MICROSECONDS));
        assertEquals("microseconds", TimeUtils.formatTimeUnit(2, TimeUnit.MICROSECONDS));
        assertEquals("millisecond", TimeUtils.formatTimeUnit(1, TimeUnit.MILLISECONDS));
        assertEquals("milliseconds", TimeUtils.formatTimeUnit(2, TimeUnit.MILLISECONDS));
        assertEquals("nanosecond", TimeUtils.formatTimeUnit(1, TimeUnit.NANOSECONDS));
        assertEquals("nanoseconds", TimeUtils.formatTimeUnit(2, TimeUnit.NANOSECONDS));
    }
}