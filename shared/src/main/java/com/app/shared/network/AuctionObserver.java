package com.app.shared.network;

import com.app.shared.models.auction.Auction;

public interface AuctionObserver {
    // Called whenever an auction's price, status, or time changes
    void onAuctionUpdated(Auction auction);
}