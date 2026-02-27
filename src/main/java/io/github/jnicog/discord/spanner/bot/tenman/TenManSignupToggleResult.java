package io.github.jnicog.discord.spanner.bot.tenman;

public record TenManSignupToggleResult(ToggleStatus status, long pollId, long dateOptionId, int newCount) {

    public enum ToggleStatus {
        JOINED,                      // signed up on an active poll
        LEFT,                        // signed off from an active poll
        DATE_FULL,                   // signing up caused the date to reach capacity
        RESIGNED_FROM_LOCKED,        // signed off from the locked roster (via button)
        NOT_ON_LOCKED_ROSTER,        // tried to join a locked date via button but isn't on the roster
        WRONG_DATE_FOR_LOCKED_POLL,  // clicked a button for a date that wasn't locked in
        POLL_CLOSED                  // poll is fully closed; no interactions allowed
    }
}
