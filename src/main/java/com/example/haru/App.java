package com.example.haru;

import com.example.haru.Server.Chat.ChatServer;

import io.github.cdimascio.dotenv.Dotenv;

public class App {
    public static void main( String[] args ) {
        Dotenv envFile = Dotenv.load();
        String portNumber = envFile.get("PORT_NUMBER");
        Integer portAsInteger = Integer.parseInt(portNumber);
        if (envFile.get("PORT_NUMBER") == null) {
            System.out.println("Can't find port number in .env file");
            return;
        } 
        System.out.println( "Server starting on port " + portNumber + "...");
        ChatServer server = ChatServer.getInstance(portAsInteger);
        server.start();
    }

}
