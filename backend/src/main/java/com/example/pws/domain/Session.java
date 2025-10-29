package com.example.pws.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    private String id;
    private String containerId;

    public Session(String id, String containerId) {
        this.id = id;
        this.containerId = containerId;
        this.lastActiveAt = Instant.now();
        this.ttlSeconds = 600;
    }

    // ✅ add these fields
    private Instant lastActiveAt = Instant.now();
    private long ttlSeconds = 600; // default 10 minutes

    public boolean isExpired() {
        return Instant.now().isAfter(lastActiveAt.plusSeconds(ttlSeconds));
    }

    public void touch() {
        this.lastActiveAt = Instant.now();
    }
}
