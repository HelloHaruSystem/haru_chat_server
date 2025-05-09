package com.example.haru.Server.Chat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.example.haru.Server.Users.UserManager;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean running;
    private UserManager userManager;

    // constants
    private static final int MAX_AUTH_ATTEMPTS = 3;

    public ClientHandler(Socket socket, ChatServer server) throws IOException {
        this.clientSocket = socket;
        this.server = server;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            // Set up communications streams second boolean argument is for auto flush
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // initialize UserManager
            this.userManager = server.getUserManager();

            // Authenticating process
            sendMessage("Authenticating please use format: username,token");

            // authentication loop
            if (!authenticateUser()) {
                // authentication failed, disconnect client
                disconnect();
                return;
            }
            
            // send welcome message
            sendMessage("Authentication successful");

            // broadcast that a new user has joined the chat
            server.broadcast(this.username + " has joined the chat!", this);

            // the main message loop
            String message;
            while(running && (message = in.readLine()) != null) {
                if (message.startsWith("/")) {
                  handleCommand(message);
                } else {
                    // regular broadcast
                    server.broadcast(this.username + " " + message, this);
                }
            }
            
        } catch (IOException e) {
            System.out.println("Error handling client... " + e.getMessage() +
                               "\n Disconnecting client(" + this.username + ")...");
        } finally {
            disconnect();
        }
    }

    public boolean authenticateUser() throws IOException {
        int attempts = 0;
        while (attempts < MAX_AUTH_ATTEMPTS) {
            String authInput = in.readLine();
            if (authInput == null) {
                return false;
            }

            String[] authParts = authInput.split(",", 2);
            if (authParts.length != 2) {
                sendMessage(("Invalid format. Use: username,token"));
                attempts++;
                continue;
            }

            String username = authParts[0];
            String token = authParts[1];

            // check if user is already logged in
            if (this.userManager.isUserOnline(username)) {
                System.out.println("User already logged in.");
                attempts++;
                continue;
            }

            // validate token with auth server
            if (this.userManager.authenticateWithToken(username, token, this)) {
                this.username = username;
                return true;
            } else {
                sendMessage("Authentication failed. Invalid token.");
                attempts++;
            }
        }

        sendMessage("Too many failed authentication attempts. Disconnecting...");
        return false;
    }

    public void handleCommand(String message) {
        if (message.startsWith("/private")) {
            handlePrivateMessage(message);
        } else if (message.startsWith("/quit")) {
            System.out.println("User:" + this.username + " has requested to disconnect");
        } else if (message.startsWith("/help")) {
            sendMessage("Available server commands:");
            sendMessage("  /private [username] [message] - Send a private message");
            sendMessage("  /quit - Disconnect from the server");
            sendMessage("  /clear - clears the screen");
        } else {
            // Unknown command
            sendMessage("Unknown command. Type /help for available commands.");
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
            System.out.println("Error disconnecting client(" + this.username + ") " + e.getMessage());
        }
    }

    private void handlePrivateMessage(String message) {
        // message format "/private username message"
        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            sendMessage("Invalid format use: /private username message");
            return;
        }

        String targetUser = parts[1];
        String privateMessage = parts[2];

        ClientHandler targetClient = this.userManager.getClientHandler(targetUser);
        if (targetClient == null) {
            sendMessage("User " + targetUser + " is not online.");
        } else {
            targetClient.sendMessage("[Private Message]\n" + this.username + privateMessage);
        }
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
