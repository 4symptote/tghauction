package com.app.shared.network;

import java.io.Serial;
import java.io.Serializable;

public record Response(boolean success, String message, Object data) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //response.success(), response.message(), response.data()
}