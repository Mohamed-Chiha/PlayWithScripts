package com.example.pws.web;

import com.example.pws.domain.Session;
import com.example.pws.dto.CreateSessionRequest;
import com.example.pws.dto.SessionView;
import com.example.pws.service.SessionService;
import com.example.pws.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionsController {
    private final SessionService sessions;
    private final ScenarioService scenarios;


    @PostMapping
    public SessionView create(@RequestBody(required = false) CreateSessionRequest req) {
        String image = req != null && req.image() != null ? req.image() : "alpine:3";
        long ttl = req != null && req.ttlSeconds() != null ? req.ttlSeconds() : 900L; // 15 min
        Session s = sessions.create(image, req != null ? req.scenarioId() : null, ttl);
        return new SessionView(s.getId(), s.getStatus().name(), s.getStartedAt(), s.getTtlSeconds());
    }


    @GetMapping
    public List<SessionView> list() {
        return sessions.list().stream()
                .map(s -> new SessionView(s.getId(), s.getStatus().name(), s.getStartedAt(), s.getTtlSeconds()))
                .toList();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        sessions.terminate(id);
        return ResponseEntity.noContent().build();
    }
}