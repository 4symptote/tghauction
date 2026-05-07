package com.app.client.model;

import com.app.client.network.NetworkClient;
import com.app.shared.network.Request;
import com.app.shared.network.Response;
import com.app.shared.exceptions.AuthenticationException;
import com.app.shared.models.user.User;
import com.app.shared.network.payload.LoginPayload;
import com.app.shared.network.payload.RegisterPayload;

import java.io.IOException;

public class AuthModel {
    public User login(String username, String password) throws AuthenticationException {
        try {
            // 1. Establish connection if none exists
            if (!NetworkClient.getInstance().isConnected()) {
                NetworkClient.getInstance().connect("localhost", 8080);
            }

            // 2. Send the Request
            Request loginReq = new Request(Request.RequestType.LOGIN, new LoginPayload(username, password, ""));
            Response response = NetworkClient.getInstance().sendRequest(loginReq);

            // 3. Process the Response
            if (!response.success()) {
                throw new AuthenticationException(response.message());
            }
            if (response.data() instanceof User user) {
                System.out.println("Success");
                return user;
            }
            throw new AuthenticationException("Server did not return user data.");
        } catch (AuthenticationException e) {
            throw e;
        } catch (IOException e) {
            throw new AuthenticationException("Could not connect to server: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public User register(String username, String password, String email, String role) throws AuthenticationException {
        try {
            if (!NetworkClient.getInstance().isConnected()) {
                NetworkClient.getInstance().connect("localhost", 8080);
            }

            Request registerReq = new Request(
                    Request.RequestType.REGISTER,
                    new RegisterPayload(username, password, email, role)
            );
            Response response = NetworkClient.getInstance().sendRequest(registerReq);

            if (!response.success()) {
                throw new AuthenticationException(response.message());
            }
            if (response.data() instanceof User user) {
                return user;
            }
            throw new AuthenticationException("Server did not return user data.");
        } catch (AuthenticationException e) {
            throw e;
        } catch (IOException e) {
            throw new AuthenticationException("Could not connect to server: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
