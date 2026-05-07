package com.app.client.model;

import com.app.client.network.NetworkClient;
import com.app.shared.network.Request;
import com.app.shared.network.Response;
import com.app.shared.models.auction.Auction;
import com.app.shared.models.auction.BidTransaction;
import com.app.shared.network.payload.AuctionIdPayload;
import com.app.shared.network.payload.BidPayload;
import com.app.shared.network.payload.CreateAuctionPayload;

import java.util.List;
import java.util.Collections;

public class AuctionListModel {

    @SuppressWarnings("unchecked")
    public List<Auction> fetchAuctions() {
        try {
            ensureConnected();
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

    public Response placeBid(String auctionId, String bidderId, double amount) {
        try {
            ensureConnected();
            return NetworkClient.getInstance().sendRequest(
                    new Request(Request.RequestType.PLACE_BID, new BidPayload(auctionId, bidderId, amount))
            );
        } catch (Exception e) {
            return new Response(false, "Could not place bid: " + e.getMessage(), null);
        }
    }

    public Response createAuction(CreateAuctionPayload payload) {
        try {
            ensureConnected();
            return NetworkClient.getInstance().sendRequest(
                    new Request(Request.RequestType.CREATE_AUCTION, payload)
            );
        } catch (Exception e) {
            return new Response(false, "Could not create auction: " + e.getMessage(), null);
        }
    }

    @SuppressWarnings("unchecked")
    public List<BidTransaction> fetchBidHistory(String auctionId) {
        try {
            ensureConnected();
            Response response = NetworkClient.getInstance().sendRequest(
                    new Request(Request.RequestType.GET_BID_HISTORY, new AuctionIdPayload(auctionId))
            );
            if (response.success() && response.data() instanceof List<?>) {
                return (List<BidTransaction>) response.data();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private void ensureConnected() throws Exception {
        if (!NetworkClient.getInstance().isConnected()) {
            NetworkClient.getInstance().connect("localhost", 8080);
        }
    }
}
