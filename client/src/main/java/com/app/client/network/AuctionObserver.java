package com.app.client.network;

import com.app.shared.models.auction.Auction;
import com.app.shared.network.Response;

public interface AuctionObserver {
    // Called when the server broadcasts an auction update (e.g. a new bid)
    void onAuctionUpdated(Auction updatedAuction);

    // Optional: Called when a general server message arrives
    void onServerMessage(Response response);
}