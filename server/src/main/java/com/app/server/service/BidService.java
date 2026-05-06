package com.app.server.service;

import com.app.server.network.ClientManager;
import com.app.shared.models.auction.Auction;
import com.app.shared.models.auction.BidTransaction;
import com.app.shared.exceptions.AuctionClosedException;
import com.app.shared.exceptions.InvalidBidException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BidService {

    private static BidService instance;
    private final AuctionManager auctionManager;

    private final Map<String, ReentrantLock> auctionLocks;

    // singleton service
    private BidService() {
        this.auctionManager = AuctionManager.getInstance();
        this.auctionLocks = new ConcurrentHashMap<>();
    }

    public static synchronized BidService getInstance() {
        if (instance == null) {
            instance = new BidService();
        }
        return instance;
    }

    // concurrent bidding
    public void placeBid(String auctionId, String bidderId, double bidAmount)
            throws InvalidBidException, AuctionClosedException
    {

        Auction auction = auctionManager.getAuction(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("Auction with ID " + auctionId + " not found.");
        }

        // put a new lock for this auction id if lock is absent then lock
        auctionLocks.putIfAbsent(auctionId, new ReentrantLock());
        ReentrantLock lock = auctionLocks.get(auctionId);

        lock.lock();

        try {
            // validations
            // check if biddable
            if (auction.getStatus() != Auction.Status.RUNNING)
            {
                throw new AuctionClosedException("Auction is not RUNNING. Current status: " + auction.getStatus());
            }
            // check if bid amount is valid
            if (bidAmount <= auction.getCurrentPrice()) {
                throw new InvalidBidException("Bid must be strictly higher than current price.");
            }

            // create new transaction and add (should be the highest bid)
            BidTransaction newBid = new BidTransaction(auctionId, bidderId, bidAmount);
            auction.addBid(newBid);

            // anti sniping
            long timeLeft = auction.getEndTime() - System.currentTimeMillis();
            if (timeLeft < 30000) { // < 30s
                long newEndTime = auction.getEndTime() + 60000; // +60s
                auction.setEndTime(newEndTime);
            }

            System.out.println("> new bid placed for $" + bidAmount + " by " + bidderId);

            // observer pattern
            ClientManager.getInstance().broadcastAuctionUpdate(auction);

        } finally {
            lock.unlock();
        }
    }
}