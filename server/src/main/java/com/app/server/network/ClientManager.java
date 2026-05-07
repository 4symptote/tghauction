package com.app.server.network;

import com.app.shared.models.auction.Auction;
import com.app.shared.network.AuctionObserver;

import java.util.ArrayList;
import java.util.List;

import com.app.shared.network.Subject;

public class ClientManager implements Subject<AuctionObserver> {
    private static ClientManager instance;

    // connected clients
    private final List<AuctionObserver> activeClients = new ArrayList<>();
    private final List<ClientHandler> socketClients = new ArrayList<>();

    private ClientManager() {}

    // bsingleton
    public static synchronized ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

    // Register an observer/cleint
    @Override
    public synchronized void addObserver(AuctionObserver client) {
        activeClients.add(client);
    }

    public synchronized void addClient(AuctionObserver client) {
        addObserver(client);
    }

    // Unregister an observer
    @Override
    public synchronized void removeObserver(AuctionObserver client) {
        activeClients.remove(client);
    }

    public synchronized void removeClient(AuctionObserver client) {
        activeClients.remove(client);
    }

    public synchronized void addSocketClient(ClientHandler client) {
        socketClients.add(client);
    }

    public synchronized void removeSocketClient(ClientHandler client) {
        socketClients.remove(client);
    }

    // Broadcast to all clients
    public synchronized void broadcastAuctionUpdate(Auction updatedAuction) {
        for (AuctionObserver client : activeClients) {
            client.onAuctionUpdated(updatedAuction);
        }
        for (ClientHandler client : socketClients) {
            client.onAuctionUpdated(updatedAuction);
        }
    }
}
