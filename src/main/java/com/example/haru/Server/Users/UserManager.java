package com.example.haru.Server.Users;

import java.util.HashMap;
import java.util.Map;

import com.example.haru.Server.Chat.ClientHandler;
import com.example.haru.Server.auth.TokenValidator;

import io.github.cdimascio.dotenv.Dotenv;

public class UserManager {
    // TODO: proper authentication
    private Map<String, ClientHandler> activeUsers;
    private TokenValidator tokenValidator;

    public UserManager() {
        this.activeUsers = new HashMap<>();

        // Load auth server URL from .env files
        Dotenv envFile = Dotenv.load();
        String authServerUrl = envFile.get("AUTH_SERVER_URL");
    }

    // add username and password restrictions and requirements
    // use proper hashing for passwords
    // consider using session tokens
    public boolean registerUser(String username, String password, ClientHandler handler) {
        if (.containsKey(username)) {
            return false;
        }


        activeUsers.put(username, handler);
        return true;
    }

    public void removeUser(String username) {
        activeUsers.remove(username);
    }

    public ClientHandler getClientHandlerByUsername(String username) {
        return activeUsers.get(username);
    }

    public boolean isUserOnline(String username) {
        return activeUsers.containsKey(username);
    }

    public boolean authenticate(String username, String password, ClientHandler handler) {
        if (.containsKey(username) && .get(username).equals(password)) {
            if (handler != null) {
                activeUsers.put(username, handler);
            }
            return true;
        }
        return false;
    }
}