package com.app.shared.network.payload;

import java.io.Serial;
import java.io.Serializable;

public record BidPayload(String auctionId, String bidderId, double amount) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
