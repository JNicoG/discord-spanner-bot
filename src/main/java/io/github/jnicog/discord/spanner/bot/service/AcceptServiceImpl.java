package io.github.jnicog.discord.spanner.bot.service;

import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.jnicog.discord.spanner.bot.service.AcceptState.AWAITING;

@Service
public class AcceptServiceImpl implements AcceptService {

    ConcurrentHashMap<User, AcceptState> userAcceptStateMap = new ConcurrentHashMap<>(5);

    public AcceptServiceImpl(Set<User> queue) {
        for (User user : queue) {
            userAcceptStateMap.put(user, AWAITING);
        }
    }

    @Override
    public void playerAccept(User user) {

    }
}
