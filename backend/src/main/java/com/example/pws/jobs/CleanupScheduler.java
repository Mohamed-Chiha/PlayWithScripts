package com.example.pws.jobs;

import com.example.pws.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private final SessionService sessions;

    @Scheduled(fixedRate = 60000) // every minute
    public void cleanupExpiredSessions() {
        for (var s : sessions.list()) {
            if (s.isExpired()) {
                log.info("🧹 Cleaning up expired session {}", s.getId());
                sessions.terminate(s.getId());
            }
        }
    }
}
