package com.example.haru.Server.Chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.java_websocket.WebSocket;

import com.example.haru.Server.Users.UserManager;

// singleton ChatServer as we only want one instance
public class ChatServer {
    private static ChatServer instance;
    private ServerSocket serverSocket;
    private final int portNumber;
    private boolean running;
    private List<ClientHandler> connectedClients;
    private UserManager userManager;
    private WebSocketChatServer wsServer; //Reference to WebSocket server
    
    // private constructor prevents instantiation from outside the class
    private ChatServer(int portNumber) {
        this.portNumber = portNumber;
        this.connectedClients = Collections.synchronizedList(new ArrayList<>());
        this.userManager = new UserManager();
        this.running = false;
    }

    // get the single instance
    public static synchronized ChatServer getInstance(int port) {
        if (instance == null) {
            instance = new ChatServer(port);
        }
        return instance;
    }

    // set the WebSocket server reference for cross-server broadcasting
    public void setWebSocketServer(WebSocketChatServer wsServer) {
        this.wsServer = wsServer;
    }

    // starts the server
    public void start() {
        try {
            this.running = true;
            this.serverSocket = new ServerSocket(this.portNumber);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // start a new thread for each client
                ClientHandler clientThread = new ClientHandler(clientSocket, instance);
                connectedClients.add(clientThread);
                new Thread(clientThread).start();
            } 
        } catch (IOException e) {
            System.out.println("Error accepting TCP clients: " + e.getMessage());
        }
    }

    // methods for broadcasting messages, adding/removing clients, etc
    public void removeClient(ClientHandler client) {
        this.connectedClients.remove(client);
        if (client.getUsername() != null) {
            userManager.removeUser(client.getUsername());
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        System.out.println("Broadcasting: " + message);

        // broadcast to TCP clients
        for (ClientHandler client : this.connectedClients) {
            if (sender != null && client == sender) {
                continue; // don't send the message back to the sender
            }

            // format the message
            String finalMessage = formatMessage(message);
            client.sendMessage(finalMessage);
        }

        // broadcast to WebSocket clients
        if (this.wsServer != null) {
            WebSocket senderWs = null;

            // check if sender s a WebSocket client
            if (sender instanceof WebSocketClientAdapter) {
                WebSocketClientAdapter adapter = (WebSocketClientAdapter) sender;
                senderWs = adapter.getWebSocketHandler().getWebSocket();
            }

            String finalMessage = formatMessage(message);
            wsServer.broadcastToWebSockets(finalMessage, senderWs);
        }
    }

    private String formatMessage(String message) {
        // Check if the message already has a proper prefix
        if (message.contains(" has joined the chat") || 
            message.contains(" has left the chat") ||
            message.contains(": ") ||
            message.startsWith("System:") ||
            message.startsWith("[Private")) {
            return message;
        } else {
            // Add a system prefix for messages without a sender
            return "System: " + message;
        }
    }

    public UserManager getUserManager() {
        return this.userManager;
    }

    public List<ClientHandler> getConnectedClients() {
        return Collections.unmodifiableList(this.connectedClients);
    }

    // clean shutdown method
    // TODO: be able to trigger this
    public void stop() {
        this.running = false;
        for (ClientHandler client : new ArrayList<>(connectedClients)) {
            client.disconnect(true);
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing server socket: " + e.getMessage());
            }
        }
    }

     // Getter for WebSocket server
    public WebSocketChatServer getWebSocketServer() {
        return this.wsServer;
    }
}
