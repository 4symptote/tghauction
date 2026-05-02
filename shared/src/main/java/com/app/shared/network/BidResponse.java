package com.app.shared.network;

import java.io.Serializable;

//Serializable payload sent from Server to Client.
// Person 1: Network Payloads
public class BidResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Object data;

    public BidResponse(boolean success, String message) {
        this(success, message, null);
    }

    public BidResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }

    @Override
    public String toString() {
        return "Response{success=" + success + ", message='" + message + "', data=" + data + "}";
    }
}
