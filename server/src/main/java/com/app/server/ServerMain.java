package com.app.server;

import com.app.server.network.AuctionServer;

public class ServerMain {
    public static void main(String[] args) {
        int port = 8080; // Make sure this matches the port your NetworkClient tries to connect to
        AuctionServer server = new AuctionServer(port);
        server.start();
    }
}