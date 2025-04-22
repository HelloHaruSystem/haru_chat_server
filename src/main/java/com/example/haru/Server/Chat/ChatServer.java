package com.example.haru.Server.Chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// singleton chatServer as we only want one instance
public class ChatServer {
    private static ChatServer instance;
    private ServerSocket serverSocket;
    private final int portNumber;
    private boolean running;
    private List<ClientHandler> connectedClients;
    
    // private constructor prevents instantiation from outside the class
    private ChatServer(int portNumber) {
        this.portNumber = portNumber;
        this.connectedClients = new ArrayList<>();
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
            System.out.println("Error acepting clients ");
        }
    }

    public void removeClient(ClientHandler client) {
        this.connectedClients.remove(client);
    }

    // methods for broadcasting messages, adding/removing clients, etc
    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : this.connectedClients) {
            client.sendMessage(message);
        }
    }
}
