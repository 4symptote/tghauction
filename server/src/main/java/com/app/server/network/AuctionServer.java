package com.app.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AuctionServer {
    private final int port;
    private final List<ClientHandler> clients;
    private boolean running;

    public AuctionServer(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Auction Server started and listening on port " + port);

            while (running) {
                // Block and wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Create a new thread for this specific client
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);

                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Called when a client disconnects
    public synchronized void removeClient(ClientHandler handler) {
        clients.remove(handler);
        System.out.println("Client disconnected. Total clients: " + clients.size());
    }
}