package com.app.server.service;

import com.app.shared.models.auction.Auction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;


public synchronized void placeBid(BidTransaction bid) {
    if (bid.getAmount() > currentHighestBid) {
        this.currentHighestBid = bid.getAmount();
        this.winner = bid.getBidder();
        notifyObservers();
    } else {
        throw new InvalidBidException("Giá đặt phải cao hơn giá hiện tại!");
    }
}