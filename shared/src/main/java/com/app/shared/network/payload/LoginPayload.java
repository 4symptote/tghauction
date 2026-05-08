package com.app.shared.network.payload;

import java.io.Serial;
import java.io.Serializable;

public record LoginPayload(String username, String password, String role) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
