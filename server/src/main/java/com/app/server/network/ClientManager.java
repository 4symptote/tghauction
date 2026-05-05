package com.app.server.network;

import com.app.shared.models.auction.Auction;
import com.app.shared.network.AuctionObserver;

import java.util.ArrayList;
import java.util.List;

public class ClientManager {
    private static ClientManager instance;

    // connected clients
    private final List<AuctionObserver> activeClients = new ArrayList<>();

    private ClientManager() {}

    // bsingleton
    public static synchronized ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

    // Register an observer/cleint
    public synchronized void addClient(AuctionObserver client) {
        activeClients.add(client);
    }

    // Unregister an observer
    public synchronized void removeClient(AuctionObserver client) {
        activeClients.remove(client);
    }

    // Broadcast to all clients
    public synchronized void broadcastAuctionUpdate(Auction updatedAuction) {
        for (AuctionObserver client : activeClients) {
            client.onAuctionUpdated(updatedAuction);
        }
    }
}