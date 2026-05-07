package com.app.server.service;

import com.app.shared.models.auction.Auction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import com.app.server.dao.AuctionDao;
import com.app.server.dao.AuctionDaoImpl;

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

    private final AuctionDao auctionDao = new AuctionDaoImpl();

    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getId(), auction); // Đây là dòng giữ trên Memory của RAM

        // Đẩy thẳng xuống MongoDB luôn
        auctionDao.createAuction(auction);
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