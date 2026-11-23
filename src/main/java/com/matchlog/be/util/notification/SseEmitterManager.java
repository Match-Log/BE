package com.matchlog.be.util.notification;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class SseEmitterManager {

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onCompletion(() -> emitters.remove(userId, emitter));
        emitter.onTimeout(() -> emitters.remove(userId, emitter));
        emitter.onError(e -> emitters.remove(userId, emitter));

        emitters.put(userId, emitter);

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(userId, emitter);
        }

        return emitter;
    }

    public void send(Long userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().name("notification").data(data));
        } catch (IOException e) {
            emitters.remove(userId, emitter);
        }
    }

    public void sendHeartbeat() {
        emitters.forEach(
                (userId, emitter) -> {
                    try {
                        emitter.send(SseEmitter.event().comment("heartbeat"));
                    } catch (IOException e) {
                        emitters.remove(userId, emitter);
                    }
                });
    }
}
