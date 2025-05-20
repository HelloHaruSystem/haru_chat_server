package com.example.haru.Server.Chat;

import org.java_websocket.WebSocket;

import com.example.haru.Server.Users.UserManager;

public class WebSocketClientHandler {
    private WebSocket webSocket;
    private ChatServer server;
    private String username;
    private boolean authenticated;
    private UserManager userManager;

    public WebSocketClientHandler(WebSocket webSocket, ChatServer server) {
        this.webSocket = webSocket;
        this.server = server;
        this.userManager = server.getUserManager();
        this.authenticated = false;
    }

    public void handleMessage(String message) {
        System.out.println("Processing WebSocket message from " + 
                         (username != null ? username : "unauthenticated") + ": " + message);

        if (!authenticated) {
            handleAuthentication(message);
        } else {
            handleChatMessage(message);
        }
    }


    //TODO: Split this up into smaller parts!
    public void handleAuthentication(String authInput) {
        // check if this is a verification request
        boolean isVerificationOnly = false;
        if (authInput.startsWith("VERIFY:")) {
            isVerificationOnly = true;
            authInput = authInput.substring(7); // removes the prefix (VERIFY:)
        }

        // parse the auth credentials
        String[] authParts = authInput.split(",", 2);
        if (authParts.length != 2) {
            sendMessage("Invalid format. Use: username,token");
            disconnect(false);
            return;
        }

        String username = authParts[0].trim();
        String token = authParts[1].trim();

        // check if the user is already logged in (skip if thats the case)
        if (!isVerificationOnly && this.userManager.isUserOnline(username)) {
            sendMessage("User already logged in.");
            disconnect(false);
            return;
        }

        // create an "adaptor to make this WebSocket handler compatible with TCP system"
        try {
            WebSocketClientAdapter adapter = new WebSocketClientAdapter(this);

            // validate token with auth server with help from UserManager
            if (this.userManager.authenticateWithToken(username, token, adapter)) {
                this.username = username;
                this.authenticated = true;

                if (isVerificationOnly) {
                    // Verification request - send success and disconnect
                    sendMessage("VERIFIED:SUCCESS");
                    disconnect(false);
                    return;
                }

                // start regular connection
                sendMessage("Authentication successful");
                sendMessage("Welcome to the chat, " + this.username + "!");

                // broadcast that a user has joined (both to TCP and WebSocket clients)
                this.server.broadcast(this.username + " has joined the chat!", null);
            } else {
                sendMessage("Authentication failed. Invalid token.");
                disconnect(false);
            }
        } catch (Exception e) {
            System.out.println("Error during authentication: " + e.getMessage());
            sendMessage("Authentication error occurred");
            disconnect(false);
        }
    }

    private void handleChatMessage(String message) {
        String formattedMessage = this.username + ": " + message;
        this.server.broadcast(formattedMessage, null);
    }

    // find websocket client by username
    private WebSocketClientHandler findWebSocketClientByUsername(String username) {
        WebSocketChatServer wsServer = server.getWebSocketServer();
        if (wsServer != null) {
            for (WebSocket connection : wsServer.getConnections()) {
                WebSocketClientHandler handler = wsServer.getClientHandler(connection);
                if (handler != null && handler.isAuthenticated() && 
                    username.equals(handler.getUsername())) {
                    return handler;
                }
            }
        }
        return null;
    }

    public void sendMessage(String message) {
        if (this.webSocket != null && this.webSocket.isOpen()) {
            try {
                this.webSocket.send(message);
            } catch (Exception e) {
                System.out.println("Error sending message - WebSocket is not open");
            }
        } else {
            System.out.println("cannot send message - WebSocket is not open");
        }
    }

    public void disconnect(boolean broadcastLeave) {
        try {
            if (this.username != null && broadcastLeave && this.authenticated) {
                this.userManager.removeUser(this.username);
                // broadcast  that a user has left?
            }

            if (this.webSocket != null && this.webSocket.isOpen()) {
                this.webSocket.close(1000, "Server disconnect");
            }

        } catch (Exception e) {
            System.out.println("Error disconnecting WebSocket Client(" + this.username + ")" + e.getMessage());
        }
    }

    // getters and setters
    public String getUsername() {
        return this.username;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public WebSocket getWebSocket() {
        return this.webSocket;
    }
}
