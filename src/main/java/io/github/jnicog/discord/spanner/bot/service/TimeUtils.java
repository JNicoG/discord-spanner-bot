package io.github.jnicog.discord.spanner.bot.service;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for handling time-related operations.
 */
public class TimeUtils {

    /**
     * Formats a time duration with proper pluralization.
     * 
     * @param length the duration length
     * @param unit the time unit
     * @return the formatted time duration (e.g., "1 minute", "2 minutes")
     */
    public static String formatTimeUnit(int length, TimeUnit unit) {
        String unitName = unit.toString().toLowerCase();
        
        if (length == 1) {
            // Remove the 's' at the end for singular form
            if (unitName.endsWith("s")) {
                unitName = unitName.substring(0, unitName.length() - 1);
            }
        }
        
        return unitName;
    }
}