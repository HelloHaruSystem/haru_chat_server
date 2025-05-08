package com.example.haru.Server.Chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.example.haru.Server.Users.UserManager;

// singleton ChatServer as we only want one instance
public class ChatServer {
    private static ChatServer instance;
    private ServerSocket serverSocket;
    private final int portNumber;
    private boolean running;
    private List<ClientHandler> connectedClients;
    private UserManager userManager;
    
    // private constructor prevents instantiation from outside the class
    private ChatServer(int portNumber) {
        this.portNumber = portNumber;
        this.connectedClients = new ArrayList<>();
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
            System.out.println("Error accepting clients ");
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
        for (ClientHandler client : this.connectedClients) {
            if (sender != null && client == sender) {
                continue;
            }
            client.sendMessage(message);
        }
    }

    public UserManager getUserManager() {
        return this.userManager;
    }

    // clean shutdown method
    // TODO: be able to trigger this
    public void stop() {
        this.running = false;
        for (ClientHandler client : new ArrayList<>(connectedClients)) {
            client.disconnect();
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing server socket: " + e.getMessage());
            }
        }
    }
}
