package com.example.pws.domain;


import lombok.*;
import java.time.Instant;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Session {
    private String id; // UUID
    private String containerId; // Docker container id
    private String scenarioId; // optional
    private Instant startedAt;
    private Instant lastActiveAt;
    private long ttlSeconds; // e.g., 900 (15 min)
    private Status status;


    public enum Status { STARTING, RUNNING, STOPPING, TERMINATED }
}