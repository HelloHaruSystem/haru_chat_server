package com.example.haru.Server;

import java.io.IOException;
import java.net.ServerSocket;
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

    }

    // methods for broadcasting messages, adding/removing clients, etc
}
