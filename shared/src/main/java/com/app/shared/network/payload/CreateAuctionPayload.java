package com.app.shared.network.payload;

import java.io.Serial;
import java.io.Serializable;

public record CreateAuctionPayload(
        String itemType,
        String name,
        String description,
        double startingPrice,
        String sellerId,
        long durationMillis
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
