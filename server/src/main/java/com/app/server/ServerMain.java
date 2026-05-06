package com.app.server;

import com.app.server.network.AuctionServer;

public class ServerMain {
    public static void main(String[] args) {
        int port = 8080;
        AuctionServer server = new AuctionServer(port);
        server.start();
    }
}
