package io.github.jnicog.discord.spanner.bot.checkin;

/**
 * Unified interface for check-in service operations.
 *
 * <p>This interface extends the segregated interfaces for different concerns:</p>
 * <ul>
 *   <li>{@link CheckInSessionReader} - Read-only session state queries</li>
 *   <li>{@link CheckInOperations} - User check-in/cancel operations</li>
 *   <li>{@link CheckInSessionManager} - Session lifecycle management</li>
 * </ul>
 *
 * <p>Consumers should depend on the most specific interface they need
 * rather than this unified interface when possible.</p>
 */
public interface CheckInService extends CheckInSessionReader, CheckInOperations, CheckInSessionManager {
    // All methods are inherited from the extended interfaces
}
