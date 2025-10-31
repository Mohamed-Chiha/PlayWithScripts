package com.example.pws.web;

import com.example.pws.domain.Session;
import com.example.pws.infra.DockerAdapter;
import com.example.pws.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalWebSocketHandler extends TextWebSocketHandler {

    private final SessionService sessions;
    private final DockerAdapter docker;

    @Override
    public void afterConnectionEstablished(WebSocketSession ws) throws Exception {
        // On extrait l'ID de session WebSocket
        String tmp = (String) ws.getAttributes().get("sessionId");
        if (tmp == null) {
            String path = ws.getUri().getPath();
            tmp = path.substring(path.lastIndexOf('/') + 1);
        }
        final String sessionId = tmp; // <- ici la variable devient finale

        // ✅ Fixed block (replaces the old 404 CloseStatus)
        Optional<Session> opt = sessions.get(sessionId);
        if (opt.isEmpty()) {
            ws.close(CloseStatus.NORMAL.withReason("Session not found"));
            return;
        }

        var s = opt.get();

        // Créer un exec interactif dans le conteneur
        var exec = docker.createExec(s.getContainerId());

        // Créer un pipe pour la communication
        PipedOutputStream stdinToContainer = new PipedOutputStream();
        PipedInputStream stdinFromWs = new PipedInputStream(stdinToContainer);
        ws.getAttributes().put("stdin", stdinToContainer);

        // Attacher la sortie du conteneur vers le WebSocket
        docker.getClient().execStartCmd(exec.getId())
                .withDetach(false)
                .withTty(true)
                .withStdIn(stdinFromWs)
                .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<com.github.dockerjava.api.model.Frame>() {
                    @Override
                    public void onNext(com.github.dockerjava.api.model.Frame frame) {
                        try {
                            String output = new String(frame.getPayload());
                            ws.sendMessage(new TextMessage(output));
                        } catch (IOException e) {
                            log.error("Error sending frame", e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.error("Docker exec error", throwable);
                    }

                    @Override
                    public void onComplete() {
                        log.info("Container stream closed for session {}", sessionId);
                    }
                });

        log.info("WebSocket connected to container session {}", sessionId);
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (payload == null || payload.isEmpty()) return;

        // 🧠 Ignore heartbeats from the client
        if ("__ping__".equals(payload)) {
            return;
        }

        PipedOutputStream stdin = (PipedOutputStream) session.getAttributes().get("stdin");
        if (stdin == null) return;

        if ("\r".equals(payload) || "\n".equals(payload)) {
            stdin.write("\n".getBytes());
        } else {
            stdin.write(payload.getBytes());
        }

        stdin.flush();
    }


}
