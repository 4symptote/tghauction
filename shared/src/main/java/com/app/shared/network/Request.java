package com.app.shared.network;

import java.io.Serial;
import java.io.Serializable;


//Record class automatically creates the constructor, adds getters and makes all fields final (type, payload)
public record Request(RequestType type, Object payload) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum RequestType { LOGIN, PLACE_BID, CREATE_AUCTION, GET_AUCTIONS }

    // request.type() | request.payload()
}

