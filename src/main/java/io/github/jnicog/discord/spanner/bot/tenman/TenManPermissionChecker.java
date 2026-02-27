package io.github.jnicog.discord.spanner.bot.tenman;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TenManPermissionChecker {

    private final Set<String> allowedUsernames;

    public TenManPermissionChecker(@Value("${tenman.allowed-usernames:}") String allowedUsernamesCsv) {
        this.allowedUsernames = Arrays.stream(allowedUsernamesCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public boolean isAllowed(String username) {
        return allowedUsernames.contains(username.toLowerCase());
    }
}
