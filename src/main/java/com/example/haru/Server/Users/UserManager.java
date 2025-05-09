package com.example.haru.Server.Users;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.example.haru.Server.Chat.ClientHandler;
import com.example.haru.Server.auth.TokenValidator;
import com.example.haru.Server.auth.TokenValidator.ValidateResult;

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
        if (authServerUrl == null) {
            System.out.println("Error: Auth server url not found"); // debug
        }

        this.tokenValidator = new TokenValidator(authServerUrl);
    }

    public boolean authenticateWithToken(String username, String token, ClientHandler clientHandler) {
        try {
            ValidateResult result = tokenValidator.validateToken(username, token);

            if (result.getIsValid()) {
                this.activeUsers.put(username, clientHandler);
                return true;
            } else {
                System.out.println("Authentication failed: " + result.getMessage());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error validating token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void removeUser(String username) {
        this.activeUsers.remove(username);
    }   

    public ClientHandler getClientHandler(String username) {
        return this.activeUsers.get(username);
    }

    public boolean isUserOnline(String username) {
        return this.activeUsers.containsKey(username);
    }
}