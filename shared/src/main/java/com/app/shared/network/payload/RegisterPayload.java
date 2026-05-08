package com.app.shared.network.payload;

import java.io.Serial;
import java.io.Serializable;

public record RegisterPayload(String username, String password, String email, String role) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
