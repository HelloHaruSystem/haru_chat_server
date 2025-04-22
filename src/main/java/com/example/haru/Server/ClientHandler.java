package com.example.haru.Server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean running;

    public ClientHandler(Socket socket, ChatServer server) throws IOException {
        this.clientSocket = socket;
        this.server = server;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            // Set up communications streams second bolean arguemnt is for auto flush
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error handeling client... " + e.getMessage() + "\n Disconecting client(" + this.username + ")...");
        } finally {
            disconnect();
        }
    }

    public void disconnect() {

    }

    public void broadcast(String message) {
        
    }
    
}
