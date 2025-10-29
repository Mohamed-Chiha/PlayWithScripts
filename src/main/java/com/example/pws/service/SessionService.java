package com.example.pws.service;

import com.example.pws.domain.Session;
import com.example.pws.infra.DockerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
    private final DockerAdapter docker;


    private final Map<String, Session> sessions = new HashMap<>();


    public Session create(String image, String scenarioId, long ttlSeconds) {
        String containerId = docker.createInteractiveContainer(image);
        String id = UUID.randomUUID().toString();
        var s = Session.builder()
                .id(id)
                .containerId(containerId)
                .scenarioId(scenarioId)
                .startedAt(Instant.now())
                .lastActiveAt(Instant.now())
                .ttlSeconds(ttlSeconds)
                .status(Session.Status.RUNNING)
                .build();
        sessions.put(id, s);
        return s;
    }


    public Optional<Session> get(String id) { return Optional.ofNullable(sessions.get(id)); }


    public List<Session> list() { return sessions.values().stream().toList(); }


    public void touch(String id) { get(id).ifPresent(s -> s.setLastActiveAt(Instant.now())); }


    public void terminate(String id) {
        var s = sessions.remove(id);
        if (s != null) {
            s.setStatus(Session.Status.STOPPING);
            docker.killAndRemove(s.getContainerId());
            s.setStatus(Session.Status.TERMINATED);
        }
    }
}
