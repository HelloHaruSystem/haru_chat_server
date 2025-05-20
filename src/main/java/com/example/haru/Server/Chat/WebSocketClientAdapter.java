package com.example.haru.Server.Chat;

import java.io.IOException;

public class WebSocketClientAdapter extends ClientHandler {
    private WebSocketClientHandler wsHandler;

    public WebSocketClientAdapter(WebSocketClientHandler wsHandler) throws IOException {
        // call the parent constructor with dummy values since we won't need TCP socket functionality
        super(null, null);
        this.wsHandler = wsHandler;
    }
    
    @Override
    public void sendMessage(String message) {
        if (this.wsHandler != null) {
            this.wsHandler.sendMessage(message);
        }
    }

    @Override
    public String getUsername() {
        return this.wsHandler != null ? this.wsHandler.getUsername() : null;
    }

    @Override
    public void disconnect(boolean broadcastLeave) {
        if (this.wsHandler != null) {
            this.wsHandler.disconnect(broadcastLeave);
        }
    }

    @Override
    public void run() {
        // WebSocket handlers don't need a run method as they're event-driven
        // This method is required by the parent class
    }

     // Additional methods to access the underlying WebSocket handler
    public WebSocketClientHandler getWebSocketHandler() {
        return this.wsHandler;
    }

    public boolean isWebSocketClient() {
        return true;
    }

    @Override
    public void handleCommand(String message) {
        // Delegate command handling to the WebSocket handler
        // This allows consistent command processing between TCP and WebSocket clients
        if (this.wsHandler != null) {
            this.wsHandler.handleMessage(message);
        }
    }
}
