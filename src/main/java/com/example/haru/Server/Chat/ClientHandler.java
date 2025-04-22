package com.example.haru.Server.Chat;

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

            // first message from client is telling the rest of the server that they have joined the chat
            // TODO: log that a user have joined
            server.broadcast(this.username + " has joined the chat!", this);

            // the main message loop
            String message;
            while(running && (message = in.readLine()) != null) {
                if (message.startsWith("/private ")) {
                    // TODO: handle private messages
                } else {
                    // regular broadcast
                    server.broadcast(this.username + ": " + message, null);
                }
            }
            
        } catch (IOException e) {
            System.out.println("Error handeling client... " + e.getMessage() + "\n Disconecting client(" + this.username + ")...");
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        try {
            this.running = false;
            server.removeClient(this);
            server.broadcast(this.username + " has left the chat.", null);

            if (this.in != null) {
                this.in.close();
            }
            if (this.out != null) {
                this.out.close();
            }
            if (this.clientSocket != null) {
                this.clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error disconecting client(" + this.username + ") " + e.getMessage());
        }
    }

    private void handlePrivateMessage(String message) {

    }

    public void sendMessage(String message) {
        if (out != null && !clientSocket.isClosed()) {
            out.println(message);
        }
    }
    
    public String getUsername() {
        return this.username;
    }
}
