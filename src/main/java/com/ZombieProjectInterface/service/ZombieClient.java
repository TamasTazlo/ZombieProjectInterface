package com.ZombieProjectInterface.service;

import com.ZombieProjectInterface.entity.SceneDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Scanner;

public class ZombieClient {
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final HttpClient client;
    private final Scanner scanner;
    private final PrintStream out;

    public ZombieClient() {
        this(
            HttpClient.newHttpClient(),
            new Scanner(System.in),
            System.out,
            new ObjectMapper(),
            System.getenv().getOrDefault("GAME_SERVICE_URL", "http://localhost:8080")
        );
    }

    public ZombieClient(HttpClient client, Scanner scanner, PrintStream out,
                        ObjectMapper objectMapper, String baseUrl) {
        this.client = client;
        this.scanner = scanner;
        this.out = out;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    public void start() throws Exception {
        out.println("CLI started");
        out.println("baseUrl = " + baseUrl);

        waitForBackend();
        out.println("""
                In order to start the game, run 'docker attach zombie-game-interface' in a terminal and enter 'start'
                        ============================================================
                        List of valid commands
                        ============================================================
                        help: display this list
                        start: start the game
                        restart: restart the game
                        inv: show inventory contents
                        quit - quit the game
                        ============================================================
                """);
        runCLILoop();
    }

    private void runCLILoop() throws Exception{
        while(true) {
            out.print("> ");
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
                out.println("""
                        ============================================================
                        List of valid commands
                        ============================================================
                        help: display this list
                        start: start the game
                        restart: restart the game
                        inv: show inventory contents
                        1 - 9: make a choice
                        quit - quit the game
                        ============================================================
                        """);
                continue;
            }

            if (input.matches("[1-9]")) {
                executeOption(input);
                continue;
            }

            if (input.equalsIgnoreCase("quit")) {
                out.println("Thanks for playing!");
                break;
            }

            out.println("Please input a valid command. Type 'help' to see all commands.");
        }
    }

    private void waitForBackend() throws Exception{
        out.println("Waiting for backend...");
        while(true) {
            try {
                sendGetScene("/game");
                out.println("Backend is ready!");
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
        out.println("\n" + "=".repeat(60));
        out.println("SCENE: " + scene.getName());
        out.println("=".repeat(60));
        out.println(scene.getDescription());

        if (scene.getOptions() != null && !scene.getOptions().isEmpty()) {
            out.println("\n" + "-".repeat(60));
            out.println("OPTIONS:");
            scene.getOptions().forEach((key, value) ->
                    out.println("  [" + key + "] " + value)
            );
            out.println("-".repeat(60));
        }
        out.println();
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

        out.println("\n" + "=".repeat(60));
        out.println("INVENTORY");
        out.println("=".repeat(60));
        inventory.forEach((key, value) ->
                out.println(key + ": " + value)
        );
        out.println("=".repeat(60) + "\n");
    }

    private void restart() throws Exception {
        sendPost("/game/restart");
        fetchScene();
    }

    private SceneDTO sendGetScene(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), SceneDTO.class);
    }

    private String sendGet(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    private void sendPost(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}