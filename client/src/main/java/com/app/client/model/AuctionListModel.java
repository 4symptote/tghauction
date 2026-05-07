package com.app.client.model;

import com.app.client.network.NetworkClient;
import com.app.shared.network.Request;
import com.app.shared.network.Response;
import com.app.shared.models.auction.Auction;

import java.io.IOException;
import java.util.List;
import java.util.Collections;

public class AuctionListModel {

    @SuppressWarnings("unchecked")
    public List<Auction> fetchAuctions() {
        try {
            Request getAuctionsReq = new Request(Request.RequestType.GET_AUCTIONS, null);
            Response response = NetworkClient.getInstance().sendRequest(getAuctionsReq);

            if (response != null && response.success()) {
                // Use response.data() or response.payload() depending on your Response record implementation
                if (response.data() instanceof List) {
                    return (List<Auction>) response.data();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}