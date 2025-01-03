package com.mahith.chatApp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RoutingService {
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Constructor injection for userSessions and ExecutorService
    public RoutingService( ExecutorService executorService) {
        this.executorService = executorService;
    }


    public void sendMessageToUser(ConcurrentHashMap<String, WebSocketSession> userSessions, String username, String message, String currentUser) {
        executorService.submit(() -> {
            WebSocketSession recipientSession = userSessions.get(username);
            if (recipientSession != null && recipientSession.isOpen()) {
                try {
                    String jsonString = objectMapper.writeValueAsString(Map.of(
                            "from", currentUser,
                            "message", message
                    ));
                    recipientSession.sendMessage(new TextMessage(jsonString));
                } catch (IOException e) {
                    System.out.println("Error sending message to " + username + ": " + e.getMessage());
                }
            } else {
                System.out.println("User " + username + " is not online or session is closed.");
            }
        });
    }

    @PreDestroy
    public void shutdownExecutor() {
        System.out.println("Shutting down ExecutorService...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService did not terminate!");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
