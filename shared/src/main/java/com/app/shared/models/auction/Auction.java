package com.app.shared.models.auction;

import com.app.shared.models.Entity;
import com.app.shared.models.item.Item;

import java.util.ArrayList;
import java.util.List;

public class Auction extends Entity {

    public enum Status {
        OPEN, RUNNING, FINISHED, PAID, CANCELED
    }

    private Item item;

    private long startTime;
    private long endTime;

    private double currentPrice;
    private Status status;
    private String highestBidderId;
    private final List<BidTransaction> bids;

    public Auction(Item item, long startTime, long endTime) {
        super();
        this.item = item;
        this.currentPrice = item.getStartingPrice();
        this.status = Status.OPEN;
        this.bids = new ArrayList<>();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Auction(Item item, long durationMillis) {
        super();
        this.item = item;
        this.currentPrice = item.getStartingPrice();
        this.status = Status.OPEN;
        this.bids = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime + durationMillis;
    }


    public Item getItem() { return item; } // Returns the actual Item (Electronics, Art, etc.)
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public double getCurrentPrice() { return currentPrice; }
    public String getHighestBidderId() { return highestBidderId; }
    public List<BidTransaction> getBids() { return bids; }

    public Status getStatus() {
        if (status == Status.PAID || status == Status.CANCELED) {
            return status;
        }

        long now = System.currentTimeMillis();
        if (now >= endTime) {
            status = Status.FINISHED;
        } else if (now >= startTime) {
            status = Status.RUNNING;
        } else {
            status = Status.OPEN;
        }
        return status;
    }


    public void setItem(Item item) { this.item = item; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public void setStatus(Status status) { this.status = status; }
    public void setHighestBidderId(String highestBidderId) { this.highestBidderId = highestBidderId; }

    //
    public void addBid(BidTransaction bid) {
        this.bids.add(bid);
        this.currentPrice = bid.getAmount();
        this.highestBidderId = bid.getBidderId();
        this.item.setCurrentHighestBid(bid.getAmount());
    }

    @Override
    public String toString() {
        return item.getName() + " | $" + currentPrice + " | " + getStatus();
    }
}
