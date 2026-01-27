package com.ZombieProjectInterface.service;

import com.ZombieProjectInterface.entity.SceneDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Scanner;

public class ZombieClient {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL =
            System.getenv().getOrDefault(
                    "GAME_SERVICE_URL",
                    "http://localhost:8080"
            );

    private final HttpClient client = HttpClient.newHttpClient();
    private final Scanner scanner = new Scanner(System.in);

    public void start() throws Exception {
        System.out.println("CLI started");
        System.out.println("BASE_URL = " + BASE_URL);

        waitForBackend();
        fetchScene();
        runCLILoop();
    }

    private void runCLILoop() throws Exception{
        while(true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("start")) {
                fetchScene();
                continue;
            }

            if (input.equalsIgnoreCase("restart")) {
                restart();
                continue;
            }

            if (input.equalsIgnoreCase("inv")) {
                inventory();
                continue;
            }

            if (input.equalsIgnoreCase("help")) {
                System.out.println("""
                        ---List of valid commands---
                        help: display this list
                        start: start the game
                        restart: restart the game
                        inv: show inventory contents
                        1 - 9: make a choice
                        quit - quit the game
                        """);
                continue;
            }

            if (input.matches("[1-9]")) {
                executeOption(input);
                continue;
            }

            if (input.equalsIgnoreCase("quit")) {
                System.out.println("Thanks for playing!");
                break;
            }

            System.out.println("Please input a valid command. Type 'help' to see all commands.");
        }
    }

    private void waitForBackend() throws Exception{
        System.out.println("Waiting for backend...");
        while(true) {
            try {
                sendGetScene("/game");
                System.out.println("Backend is ready!");
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void fetchScene() throws Exception{
        SceneDTO scene = sendGetScene("/game");
        displayScene(scene);
    }

    private void displayScene(SceneDTO scene) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SCENE: " + scene.getName());
        System.out.println("=".repeat(60));
        System.out.println(scene.getDescription());

        if (scene.getOptions() != null && !scene.getOptions().isEmpty()) {
            System.out.println("\n" + "-".repeat(60));
            System.out.println("OPTIONS:");
            scene.getOptions().forEach((key, value) ->
                    System.out.println("  [" + key + "] " + value)
            );
            System.out.println("-".repeat(60));
        }
        System.out.println();
    }

    private void executeOption(String option) throws Exception {
        SceneDTO scene = sendGetScene("/game/" + option);
        displayScene(scene);
    }

    private void inventory() throws Exception{
        String jsonString = sendGet("/game/inv");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Integer> inventory = mapper.readValue(jsonString,
                new TypeReference<Map<String, Integer>>(){});

        inventory.forEach((key, value) ->
                System.out.println(key + ": " + value)
        );
    }

    private void restart() throws Exception {
        sendPost("/game/restart");
        fetchScene();
    }

    private SceneDTO sendGetScene(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), SceneDTO.class);
    }

    private String sendGet(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    private void sendPost(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}