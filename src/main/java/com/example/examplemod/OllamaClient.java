package com.example.examplemod;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class OllamaClient {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private OllamaClient() {}

    public static AiResponse chat(String playerName, String message, String recentHistory) throws IOException, InterruptedException {
        JsonObject root = new JsonObject();
        root.addProperty("model", Config.ollamaModel);
        root.addProperty("stream", false);

        JsonObject options = new JsonObject();
        options.addProperty("temperature", 0.7);
        root.add("options", options);

        String system = """
                You are %s, a friendly Minecraft companion living in the player's world.
                You are an actual companion, not a generic assistant. Keep replies natural and reasonably short.
                You can currently control only these actions:
                - NONE: just talk
                - FOLLOW: follow the player
                - STOP: stop following and stay where you are
                - COME: come to the player

                Return ONLY valid JSON with exactly these fields:
                {"message":"your spoken reply","action":"NONE|FOLLOW|STOP|COME"}

                Do not use markdown fences. Do not put extra fields in the JSON.
                Player name: %s
                """.formatted(Config.companionName, playerName);

        String prompt = system
                + "\nRecent conversation:\n" + recentHistory
                + "\nPlayer: " + message;

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);

        com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
        messages.add(userMessage);
        root.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.ollamaUrl + "/api/chat"))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(root.toString()))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new IOException("Ollama returned HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonObject responseRoot = JsonParser.parseString(response.body()).getAsJsonObject();
        String content = responseRoot.getAsJsonObject("message").get("content").getAsString().trim();

        try {
            JsonObject aiJson = JsonParser.parseString(content).getAsJsonObject();
            String spoken = aiJson.has("message") ? aiJson.get("message").getAsString() : content;
            String action = aiJson.has("action") ? aiJson.get("action").getAsString().toUpperCase() : "NONE";
            return new AiResponse(spoken, action);
        } catch (RuntimeException ignored) {
            // A local model may occasionally ignore the JSON instruction.
            return new AiResponse(content, "NONE");
        }
    }

    public record AiResponse(String message, String action) {}
}
