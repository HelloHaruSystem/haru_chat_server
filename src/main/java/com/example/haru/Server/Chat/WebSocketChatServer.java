package com.example.haru.Server.Chat;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.example.haru.Server.Users.UserManager;

public class WebSocketChatServer extends WebSocketServer {
    private static WebSocketChatServer instance;
    private ChatServer chatServer;
    private ConcurrentHashMap<WebSocket, WebSocketClientHandler> clientHandlers;
    private UserManager userManager;

    // private constructor for the singleton pattern
    private WebSocketChatServer(int port, ChatServer chatServer) {
        super(new InetSocketAddress(port));
        this.chatServer = chatServer;
        this.clientHandlers = new ConcurrentHashMap<>();
        this.userManager = chatServer.getUserManager();

        // configure WebSocket server settings
        setReuseAddr(true);
        setTcpNoDelay(true);
    }

    // get the singleton instance
    public static synchronized WebSocketChatServer getInstance(int port, ChatServer chatServer) {
        if (instance == null) {
            instance = new WebSocketChatServer(port, chatServer);
        }
        return instance;
    }

    @Override
    public void onOpen(WebSocket connection, ClientHandshake handshake) {
        // create handler for this webSocketConnection
         WebSocketClientHandler handler = new WebSocketClientHandler(connection, this.chatServer);
        this.clientHandlers.put(connection, handler);

        // send authentication prompt
        connection.send("Authenticating please use format: username,token");
    }

    @Override
    public void onClose(WebSocket connection, int code, String reason, boolean remote) {
        System.out.println("WebSocket connection close: " + connection.getRemoteSocketAddress() + 
                            " Code: " + code + " Reason: " + reason);
        
        WebSocketClientHandler handler = clientHandlers.remove(connection);
        if (handler != null) {
            handler.disconnect(true);
        }
    }

    @Override
    public void onMessage(WebSocket connection, String message) {
        System.out.println("Received WebSocket message from " + connection.getRemoteSocketAddress());
        
        WebSocketClientHandler handler = clientHandlers.get(connection);
        if (handler != null) {
            handler.handleMessage(message);
        } else {
            System.out.println("No handler found for WebSocket connection");
        }
    }

    @Override
    public void onError(WebSocket connection, Exception e) {
        System.out.println("WebSocket error on connection " + 
                         (connection != null ? connection.getRemoteSocketAddress() : "unknown"));
        e.printStackTrace();
        
        if (connection != null) {
            WebSocketClientHandler handler = clientHandlers.remove(connection);
            if (handler != null) {
                handler.disconnect(false);
            }
        }
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on port " + getPort());
        setConnectionLostTimeout(100); // 100 seconds timeout for lost connections
    }

    public void broadcastToWebSockets(String message, WebSocket sender) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        for (WebSocket connection : getConnections()) {
            if (connection != sender && connection.isOpen()) {
                connection.send(message);
            }
        }
    }

    // get the client handler for a specific connection
    public WebSocketClientHandler getClientHandler(WebSocket connection) {
        return this.clientHandlers.get(connection);
    }

    // remove a client handler
    public void removeClientHandler(WebSocket connection) {
        this.clientHandlers.remove(connection);
    }

    // get the total number of connect WebSocket clients
    public int getConnectedClientCount() {
        return clientHandlers.size();
    }

    // shutdown in a proper way
    public void stop() throws InterruptedException {
        System.out.println("Stopping WebSocket server...");

        // disconnect all the clients
        for (WebSocketClientHandler handler : this.clientHandlers.values()) {
            try {
                handler.disconnect(false);
            } catch (Exception e) {
                System.out.println("Error disconnecting client " + e.getMessage());
            }
        }

        this.clientHandlers.clear();

        // call parent stop method
        super.stop();

        System.out.println("WebSocket server has stopped");
    }
}
