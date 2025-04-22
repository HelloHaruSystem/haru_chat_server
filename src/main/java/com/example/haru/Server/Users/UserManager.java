package com.example.haru.Server.Users;

import java.util.HashMap;
import java.util.Map;

public class UserManager {
    // TODO: proper authentication
    Map<String, String> users;

    public UserManager() {
        this.users = new HashMap<>();
    }

    // add username and password restrictions and requrements
    // use proper hashing for passwords
    // consider using session tokens
    public boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, password);
        
        return true;
    }
}