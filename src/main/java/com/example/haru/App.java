package com.example.haru;

import io.github.cdimascio.dotenv.Dotenv;

public class App {
    public static void main( String[] args ) {
        Dotenv envFile = Dotenv.load();
        String portNumber = envFile.get("PORT_NUMBER");
        if (envFile.get("PORT_NUMBER") == null) {
            System.out.println("Can't find port number in .env file");
            return;
        } 
        System.out.println( "Server starting on port " + portNumber + "...");

    }

}
