package com.mahith.chatApp.Entity;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ChatMessage {
    private String to;
    private String message;

    // Getters and Setters
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ChatMessage{to='" + to + "', message='" + message + "'}";
    }
}
