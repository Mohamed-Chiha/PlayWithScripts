package com.example.pws.web;

import com.example.pws.domain.Session;
import com.example.pws.infra.DockerAdapter;
import com.example.pws.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessions;
    private final DockerAdapter docker;

    @PostMapping("/start")
    public Session start(@RequestParam(defaultValue = "alpine") String image) {
        // 1. Create a Docker container
        String containerId = docker.createInteractiveContainer(image);

        // 2. Save the session in memory
        Session session = sessions.create(containerId);

        // 3. Return the session info to the client
        return session;
    }

    @DeleteMapping("/{id}")
    public void stop(@PathVariable String id) {
        sessions.get(id).ifPresent(s -> {
            docker.killAndRemove(s.getContainerId());
            sessions.remove(id);
        });
    }
}
