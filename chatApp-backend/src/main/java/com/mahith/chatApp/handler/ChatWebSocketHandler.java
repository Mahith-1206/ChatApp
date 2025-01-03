package com.mahith.chatApp.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahith.chatApp.Entity.ChatMessage;
import com.mahith.chatApp.service.RoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Concurrent map to store WebSocket connections for each user
    private final ConcurrentHashMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    private final RoutingService routingService;

    @Autowired
    public ChatWebSocketHandler(RoutingService routingService) {
        this.routingService = routingService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = extractUsernameFromSession(session);
        session.getAttributes().put("username", username);
        if(username != null){
            userSessions.put(username, session);
            System.out.println("User " + username + " connected.");
        }
        else {
            session.close();
        }
        System.out.println("New session connected: " + session.getId());
    }

    private String extractUsernameFromSession(WebSocketSession session) {
        // This could be based on the WebSocket URL or headers
        String path = session.getUri().getPath();
        String[] pathSegments = path.split("/");

        // The username should be at index 2 in the path (after "/chat/")
        if (pathSegments.length > 2) {
            return pathSegments[2];  // Returns the username
        }

        return null;  // If no valid username is found  // Extract username from URL path
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String username = getUsernameFromSession(session);
        if (username == null) return;

        String payload = message.getPayload();
        System.out.println("Message received from " + username + ": " + payload);

        String recipient = extractRecipient(payload);
        String actualMessage = extractMessage(payload);


        routingService.sendMessageToUser(userSessions, recipient, actualMessage, username);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = getUsernameFromSession(session);
        userSessions.remove(username);
        System.out.println("Session closed: " + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Transport error for session: " + session.getId());
        String username = getUsernameFromSession(session);
        userSessions.remove(username);
    }


    // Extract the username from the session, this can be passed as a parameter when establishing the connection
    private String getUsernameFromSession(WebSocketSession session) {
        return (String) session.getAttributes().get("username");
    }

    // Extract recipient and message content from the incoming message
    private String extractRecipient(String message) {
        // Implement a message format like "recipient:message"
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse JSON to Java object
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);

            // Extract the values
            String to = chatMessage.getTo();
//            String message = chatMessage.getMessage();

            // Print the values
            System.out.println("To: " + to);
            return to;
        } catch (Exception e) {
            e.printStackTrace();
           // return to;
        }
        return  null;
          // First part is the recipient
    }

    private String extractMessage(String message) {
        // Extract the message part after the recipient
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse JSON to Java object
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);

            // Extract the values
//            String to = chatMessage.getTo();
            String messaged = chatMessage.getMessage();


            System.out.println("Message: " + messaged);
            return messaged;
        } catch (Exception e) {
            e.printStackTrace();
            // return to;
        }
        return  null;
    }

    // Send a message to a specific user (WebSocket session)
//    private void sendMessageToUser(String username, String message, String currentUser) {
//
//        WebSocketSession recipientSession = userSessions.get(username);
//        Map<String, String> jsonMap = new HashMap<>();
//        jsonMap.put("from", currentUser);
//        jsonMap.put("message", message);
//        if (recipientSession != null && recipientSession.isOpen()) {
//            try {
//                ObjectMapper objectMapper = new ObjectMapper();
//                String jsonString = objectMapper.writeValueAsString(jsonMap);
//                recipientSession.sendMessage(new TextMessage(jsonString));
//            } catch (IOException e) {
//                System.out.println("Error sending message to " + username);
//            }
//        } else {
//            System.out.println("User " + username + " is not online or session is closed.");
//        }
//    }




}
