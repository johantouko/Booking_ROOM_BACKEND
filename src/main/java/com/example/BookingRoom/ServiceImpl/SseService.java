package com.example.BookingRoom.ServiceImpl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class SseService {

    private final Map<Long, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();


    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Ajouter au groupe d'emitters du user
        emittersByUser.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        // Nettoyage sur fermeture, timeout, erreur
        emitter.onCompletion(() -> emittersByUser.get(userId).remove(emitter));
        emitter.onTimeout(() -> emittersByUser.get(userId).remove(emitter));
        emitter.onError(e -> emittersByUser.get(userId).remove(emitter));

        // Optionnel : keep-alive toutes les 15s
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> keepAliveTask = executor.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, 0, 15, TimeUnit.SECONDS);

        emitter.onCompletion(() -> {
            keepAliveTask.cancel(true);
            executor.shutdown();
            emittersByUser.get(userId).remove(emitter);
        });
        emitter.onTimeout(() -> {
            keepAliveTask.cancel(true);
            executor.shutdown();
            emittersByUser.get(userId).remove(emitter);
        });
        emitter.onError(e -> {
            keepAliveTask.cancel(true);
            executor.shutdown();
            emittersByUser.get(userId).remove(emitter);
        });


        return emitter;
    }

    public void broadcastToAllUsers(Object data,String message) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (Map.Entry<Long, List<SseEmitter>> entry : emittersByUser.entrySet()) {
            List<SseEmitter> emitters = entry.getValue();
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(message)
                            .data(data));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                    deadEmitters.add(emitter);
                }
            }
            // Nettoyage des emitters morts
            emitters.removeAll(deadEmitters);
        }
    }
}