package com.example.haru.Server.Users;

import java.util.HashMap;
import java.util.Map;

import com.example.haru.Server.Chat.ClientHandler;

public class UserManager {
    // TODO: proper authentication
    private Map<String, String> credentials;
    private Map<String, ClientHandler> activeUsers;

    public UserManager() {
        this.credentials = new HashMap<>();
        this.activeUsers = new HashMap<>();
    }

    // add username and password restrictions and requrements
    // use proper hashing for passwords
    // consider using session tokens
    public boolean registerUser(String username, String password, ClientHandler handler) {
        if (credentials.containsKey(username)) {
            return false;
        }

        credentials.put(username, password);
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
        if (credentials.containsKey(username) && credentials.get(username).equals(password)) {
            if (handler != null) {
                activeUsers.put(username, handler);
            }
            return true;
        }
        return false;
    }
}