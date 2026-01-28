package com.ZombieProjectInterface.service;

import com.ZombieProjectInterface.entity.SceneDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZombieClientTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;
    private ObjectMapper objectMapper;
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
        objectMapper = new ObjectMapper();
    }

    private ZombieClient createClient(String input) {
        Scanner scanner = new Scanner(input);
        return new ZombieClient(httpClient, scanner, printStream, objectMapper, BASE_URL);
    }

    private String getOutput() {
        return outputStream.toString();
    }

    @Test
    @DisplayName("Start screen should display welcome message")
    void startDisplaysWelcomeMessage() throws Exception {
        SceneDTO scene = new SceneDTO("Test description", "Test Scene", Map.of("1", "Option 1"));
        String sceneJson = objectMapper.writeValueAsString(scene);

        when(httpResponse.body()).thenReturn(sceneJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        ZombieClient client = createClient("quit\n");
        client.start();

        String output = getOutput();
        assertTrue(output.contains("CLI started"));
        assertTrue(output.contains("baseUrl = " + BASE_URL));
        assertTrue(output.contains("List of valid commands"));
    }

    @Test
    @DisplayName("Start screen should show a Scene with options")
    void startCommandShowsCurrentScene() throws Exception {
        SceneDTO scene = new SceneDTO("You are in a dark room.", "Dark Room",
                Map.of("1", "Look around", "2", "Open door"));
        String sceneJson = objectMapper.writeValueAsString(scene);

        when(httpResponse.body()).thenReturn(sceneJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        ZombieClient client = createClient("start\nquit\n");
        client.start();

        String output = getOutput();
        assertTrue(output.contains("SCENE: Dark Room"));
        assertTrue(output.contains("You are in a dark room."));
        assertTrue(output.contains("[1] Look around"));
        assertTrue(output.contains("[2] Open door"));
    }

    @Test
    @DisplayName("Help command should display all commands")
    void helpCommandDisplaysAllCommands() throws Exception {
        SceneDTO scene = new SceneDTO("Test", "Test", Map.of());
        String sceneJson = objectMapper.writeValueAsString(scene);

        when(httpResponse.body()).thenReturn(sceneJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        ZombieClient client = createClient("help\nquit\n");
        client.start();

        String output = getOutput();
        assertTrue(output.contains("help: display this list"));
        assertTrue(output.contains("start: start the game"));
        assertTrue(output.contains("restart: restart the game"));
        assertTrue(output.contains("inv: show inventory contents"));
        assertTrue(output.contains("1 - 9: make a choice"));
        assertTrue(output.contains("quit - quit the game"));
    }

    @Test
    @DisplayName("Quit command should show goodbye message")
    void quitCommandExitsWithMessage() throws Exception {
        SceneDTO scene = new SceneDTO("Test", "Test", Map.of());
        String sceneJson = objectMapper.writeValueAsString(scene);

        when(httpResponse.body()).thenReturn(sceneJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        ZombieClient client = createClient("quit\n");
        client.start();

        String output = getOutput();
        assertTrue(output.contains("Thanks for playing!"));
    }

    @Test
    @DisplayName("Invalid command should show error message")
    void invalidCommandShowsErrorMessage() throws Exception {
        SceneDTO scene = new SceneDTO("Test", "Test", Map.of());
        String sceneJson = objectMapper.writeValueAsString(scene);

        when(httpResponse.body()).thenReturn(sceneJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        ZombieClient client = createClient("invalidcommand\nquit\n");
        client.start();

        String output = getOutput();
        assertTrue(output.contains("Please input a valid command"));
    }

    @Test
    @DisplayName("Numeric input should execute an option")
    void numericOptionExecutesChoice() throws Exception {
        SceneDTO initialScene = new SceneDTO("Initial", "Start", Map.of("1", "Go north"));
        SceneDTO nextScene = new SceneDTO("You went north.", "North Room", Map.of());

        when(httpResponse.body())
                .thenReturn(objectMapper.writeValueAsString(initialScene))
                .thenReturn(objectMapper.writeValueAsString(nextScene));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        ZombieClient client = createClient("1\nquit\n");
        client.start();

        String output = getOutput();
        assertTrue(output.contains("SCENE: North Room"));
        assertTrue(output.contains("You went north."));
    }

    @Test
    @DisplayName("Inv command should display player inventory")
    void inventoryCommandDisplaysInventory() throws Exception {
        SceneDTO scene = new SceneDTO("Test", "Test", Map.of());
        String sceneJson = objectMapper.writeValueAsString(scene);
        Map<String, Integer> inventory = Map.of("Sword", 1, "Health Potion", 3);
        String inventoryJson = objectMapper.writeValueAsString(inventory);

        when(httpResponse.body())
                .thenReturn(sceneJson)
                .thenReturn(inventoryJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        ZombieClient client = createClient("inv\nquit\n");
        client.start();

        String output = getOutput();
        assertTrue(output.contains("INVENTORY"));
        assertTrue(output.contains("Sword: 1"));
        assertTrue(output.contains("Health Potion: 3"));
    }
}
