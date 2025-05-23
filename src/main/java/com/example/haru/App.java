package com.example.haru;

import com.example.haru.Server.Chat.ChatServer;
import com.example.haru.Server.Chat.WebSocketChatServer;

import io.github.cdimascio.dotenv.Dotenv;

public class App {
    public static void main( String[] args ) {
        Dotenv envFile = Dotenv.load();

        // get TCP port of desktop clients 
        String tcpPortNumber = envFile.get("TCP_PORT_NUMBER");
        if (tcpPortNumber == null) {
            System.out.println("Couldn't find TCP socket port\nShutting down...");
            return;
        }
        Integer tcpPort = Integer.parseInt(tcpPortNumber);
        
        // get WebSocket port for web clients
        String wsPortNumber = envFile.get("WEB_SOCKET_PORT");
        if (wsPortNumber == null) {
            System.out.println("couldn't find WebSocket port\nShutting down...");
            return;
        }
        Integer wsPort = Integer.parseInt(wsPortNumber);

        System.out.println( "Starting TCP server on port " + tcpPort + "...");
        System.out.println("Starting WebSocket server on port " + wsPort + "...");

        // get instance and configure chat server (TCP)
        ChatServer tcpServer = ChatServer.getInstance(tcpPort);
        // get instance and configure chat server (WebSocket)
        WebSocketChatServer wsServer = WebSocketChatServer.getInstance(wsPort, tcpServer);

        // link the servers so they can broadcast to each other's clients
        tcpServer.setWebSocketServer(wsServer);

        // shutdown hook for a clean server shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            tcpServer.stop();
            try {
                wsServer.stop();
            } catch (InterruptedException e) {
                System.out.println("Error stopping WebSocket server:" + e.getMessage());
            }
        }));

        // start TCP server on it's own thread and WebSocket server on the main thread
        new Thread(() -> {
            tcpServer.start();
        }).start();

        wsServer.start();
    }
}