package com.app.server.service;

import com.app.shared.models.auction.Auction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class AuctionManager {

    private static AuctionManager instance;

    private final Map<String, Auction> activeAuctions;

    private AuctionManager() {
        this.activeAuctions = new ConcurrentHashMap<>();
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }



    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getId(), auction);
    }

    public void removeAuction(String auctionId) {
        activeAuctions.remove(auctionId);
    }

    public Auction getAuction(String auctionId) {
        return activeAuctions.get(auctionId);
    }

    public Map<String, Auction> getAllActiveAuctions() {
        return activeAuctions;
    }


}