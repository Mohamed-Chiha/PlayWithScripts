package com.example.pws.web;

import com.example.pws.domain.Session;
import com.example.pws.infra.DockerAdapter;
import com.example.pws.service.SessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TerminalWebSocket extends TextWebSocketHandler {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final SessionService sessions;
    private final DockerAdapter docker;

    @Override
    public void afterConnectionEstablished(WebSocketSession ws) throws Exception {
        String sessionId = resolveSessionId(ws);
        Optional<Session> opt = sessions.get(sessionId);
        if (opt.isEmpty()) {
            ws.close(CloseStatus.NORMAL.withReason("Session not found"));
            return;
        }

        Session s = opt.get();
        ExecCreateCmdResponse exec = docker.createExec(s.getContainerId());

        PipedOutputStream stdinPipe = new PipedOutputStream();
        PipedInputStream stdinStream = new PipedInputStream(stdinPipe, 8 * 1024);

        ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame frame) {
                if (!ws.isOpen()) {
                    return;
                }
                try {
                    String output = new String(frame.getPayload(), StandardCharsets.UTF_8);
                    ws.sendMessage(new TextMessage(output));
                } catch (IOException e) {
                    log.error("Failed to push container output to client", e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Docker exec error", throwable);
                safeClose(ws, CloseStatus.SERVER_ERROR.withReason("Container error"));
            }

            @Override
            public void onComplete() {
                log.info("Stream closed for session {}", sessionId);
                safeClose(ws, CloseStatus.NORMAL);
            }
        };

        CompletableFuture<Void> streamTask = CompletableFuture.runAsync(() -> {
            try {
                docker.getClient().execStartCmd(exec.getId())
                        .withDetach(false)
                        .withTty(true)
                        .withStdIn(stdinStream)
                        .exec(callback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Streaming thread interrupted for session {}", sessionId);
            } catch (Exception e) {
                log.error("Unable to start exec stream", e);
                safeClose(ws, CloseStatus.SERVER_ERROR.withReason("Unable to start exec"));
            }
        });

        ws.getAttributes().put("terminal", new ConnectionContext(stdinPipe, callback, exec.getId(), streamTask));
        log.info("WebSocket {} connected to container {}", sessionId, s.getContainerId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ConnectionContext context = (ConnectionContext) session.getAttributes().get("terminal");
        if (context == null) {
            return;
        }

        String payload = message.getPayload();
        if (payload == null) {
            return;
        }

        if (handleResizeIfPresent(context.execId(), payload)) {
            return;
        }

        try {
            context.stdin().write(payload.getBytes(StandardCharsets.UTF_8));
            context.stdin().flush();
        } catch (IOException e) {
            log.error("Failed to send input to container", e);
            safeClose(session, CloseStatus.SERVER_ERROR.withReason("Cannot write to container"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        ConnectionContext context = (ConnectionContext) session.getAttributes().remove("terminal");
        if (context != null) {
            context.close();
        }
        super.afterConnectionClosed(session, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error", exception);
        safeClose(session, CloseStatus.SERVER_ERROR);
    }

    private boolean handleResizeIfPresent(String execId, String payload) {
        String trimmed = payload.trim();
        if (!trimmed.startsWith("{")) {
            return false;
        }

        try {
            JsonNode node = JSON.readTree(trimmed);
            if (!"resize".equals(node.path("type").asText(null))) {
                return false;
            }

            int rows = node.path("rows").asInt(0);
            int cols = node.path("cols").asInt(0);
            docker.getClient().execResizeCmd(execId)
                    .withHeight(rows)
                    .withWidth(cols)
                    .exec();
            return true;
        } catch (Exception e) {
            log.debug("Failed to parse resize event payload: {}", payload, e);
            return false;
        }
    }

    private String resolveSessionId(WebSocketSession ws) {
        Object fromAttr = ws.getAttributes().get("sessionId");
        if (fromAttr instanceof String id) {
            return id;
        }
        String path = ws.getUri() != null ? ws.getUri().getPath() : "";
        int idx = path.lastIndexOf('/') + 1;
        return idx > 0 ? path.substring(idx) : path;
    }

    private void safeClose(WebSocketSession session, CloseStatus status) {
        if (!session.isOpen()) {
            return;
        }
        try {
            session.close(status);
        } catch (IOException e) {
            log.debug("Failed to close websocket", e);
        }
    }

    private record ConnectionContext(PipedOutputStream stdin,
                                     ResultCallback<?> callback,
                                     String execId,
                                     CompletableFuture<?> streamTask) implements AutoCloseable {
        @Override
        public void close() {
            try {
                stdin.close();
            } catch (IOException ignored) {
            }
            try {
                callback.close();
            } catch (IOException ignored) {
            }
            streamTask.cancel(true);
        }
    }
}
