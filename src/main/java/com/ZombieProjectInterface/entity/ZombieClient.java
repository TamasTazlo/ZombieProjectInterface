package com.ZombieProjectInterface.entity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class ZombieClient {
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
        showScene();
        runCLILoop();
    }

    private void runCLILoop() throws Exception{
        while(true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("quit")) break;
            if (input.equalsIgnoreCase("restart")) {
                restart();
                continue;
            }
            if (input.equalsIgnoreCase("inv")) {
                inventory();
                continue;
            }
            executeChoice(input);
        }
    }

    private void waitForBackend() throws Exception{
        System.out.println("Waiting for backend...");
        while(true) {
            try {
                sendGet("/game");
                System.out.println("Backend is ready!");
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void showScene() throws Exception{
        sendGet("/game");
    }

    private void executeChoice(String option) throws Exception {
        sendGet("/game/" + option);
    }

    private void inventory() throws Exception{
        sendGet("/game/inv");
    }

    private void restart() throws Exception {
        sendPost("/game/restart");
    }

    private void sendGet(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
    }

    private void sendPost(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
    }
}
