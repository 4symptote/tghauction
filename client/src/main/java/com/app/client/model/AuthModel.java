package com.app.client.model;

import com.app.client.network.NetworkClient;
import com.app.shared.network.Request;
import com.app.shared.network.Response;
import com.app.shared.exceptions.AuthenticationException;

import java.io.IOException;

public class AuthModel {
    public void login(String username) throws AuthenticationException {
        try {
            // 1. Establish connection if none exists
            if (NetworkClient.getInstance().sendRequest(null) == null) {
                NetworkClient.getInstance().connect("localhost", 8080);
            }

            // 2. Send the Request
            Request loginReq = new Request(Request.RequestType.LOGIN, username);
            Response response = NetworkClient.getInstance().sendRequest(loginReq);

            // 3. Process the Response
            if (!response.success()) {
                throw new AuthenticationException(response.message());
            } else {
                System.out.println("Success");
            }
        } catch (IOException e) {
            throw new AuthenticationException("Could not connect to server: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}