package com.app.shared.network.payload;

import java.io.Serial;
import java.io.Serializable;

public record AuctionIdPayload(String auctionId) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
