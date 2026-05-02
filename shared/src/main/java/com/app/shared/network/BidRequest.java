package com.app.shared.network;
import java.io.Serializable;

 //Serializable payload sent from Client to Server.
 // Person 1: Network Payloads
public class BidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RequestType {
        LOGIN,
        PLACE_BID,
        GET_AUCTION_LIST,
        CREATE_AUCTION
    }

    private RequestType type;
    private Object data;

    public BidRequest(RequestType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public RequestType getType() { return type; }
    public Object getData() { return data; }

    @Override
    public String toString() {
        return "Request{type=" + type + ", data=" + data + "}";
    }
}
