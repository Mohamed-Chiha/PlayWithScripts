package com.example.pws.jobs;

import com.example.pws.domain.Session;
import com.example.pws.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.time.Instant;


@Component
@EnableScheduling
@RequiredArgsConstructor
public class CleanupScheduler {
    private final SessionService sessions;


    @Scheduled(fixedDelay = 30000)
    public void sweep() {
        var now = Instant.now();
        sessions.list().forEach(s -> {
            long age = now.getEpochSecond() - s.getLastActiveAt().getEpochSecond();
            if (age > s.getTtlSeconds()) {
                sessions.terminate(s.getId());
            }
        });
    }
}