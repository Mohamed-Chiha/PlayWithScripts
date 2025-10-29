package com.example.pws.service;

import com.example.pws.domain.Session;
import com.example.pws.infra.DockerAdapter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    private final DockerAdapter docker;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public SessionService(DockerAdapter docker) {
        this.docker = docker;
    }

    public Session create(String containerId) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Session s = new Session(id, containerId);
        sessions.put(id, s);
        return s;
    }

    public Optional<Session> get(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    public void remove(String id) {
        sessions.remove(id);
    }

    public Collection<Session> list() {
        return sessions.values();
    }

    // ✅ for CleanupScheduler
    public void terminate(String id) {
        get(id).ifPresent(s -> {
            docker.killAndRemove(s.getContainerId());
            sessions.remove(id);
        });
    }
}
