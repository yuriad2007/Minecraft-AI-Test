package com.example.examplemod;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CompanionMemory {
    private static final int MAX_MESSAGES = 10;
    private static final Map<UUID, ArrayDeque<String>> HISTORY = new ConcurrentHashMap<>();

    private CompanionMemory() {}

    public static String getHistory(UUID playerId) {
        ArrayDeque<String> messages = HISTORY.get(playerId);
        if (messages == null || messages.isEmpty()) {
            return "(no previous conversation)";
        }
        return String.join("\n", messages);
    }

    public static void add(UUID playerId, String speaker, String message) {
        ArrayDeque<String> messages = HISTORY.computeIfAbsent(playerId, ignored -> new ArrayDeque<>());
        synchronized (messages) {
            messages.addLast(speaker + ": " + message);
            while (messages.size() > MAX_MESSAGES) {
                messages.removeFirst();
            }
        }
    }
}
