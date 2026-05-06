package com.app.shared.network;

import java.io.Serializable;

//Example specific data object for Bidding.
public class BidRequestData implements Serializable {
    private String auctionId;
    private String bidderId;
    private double bidAmount;

    public BidRequestData(String auctionId, String bidderId, double bidAmount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
    }

    public String getAuctionId() { return auctionId; }
    public String getBidderId() { return bidderId; }
    public double getBidAmount() { return bidAmount; }
}
