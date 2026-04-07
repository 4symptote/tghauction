package com.app.shared.models.auction;

import com.app.shared.models.Entity;

public class BidTransaction extends Entity {
    private String auctionId;
    private String bidderId;
    private double amount;
    private long timestamp;

    public BidTransaction(String auctionId, String bidderId, double amount) {
        super(); // Generates the UUID
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getAuctionId() { return auctionId; }
    public String getBidderId() { return bidderId; }
    public double getAmount() { return amount; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }
    public void setBidderId(String bidderId) { this.bidderId = bidderId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}