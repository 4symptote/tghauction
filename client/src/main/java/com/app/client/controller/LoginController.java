package com.app.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.app.client.network.NetworkClient;
import com.app.shared.network.Request;
import com.app.shared.network.Response;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();

        if (username == null || username.trim().isEmpty()) {
            errorLabel.setText("Please enter a username.");
            return;
        }

        try {
            // Proper safety check before acting!
            if (!NetworkClient.getInstance().isConnected()) {
                NetworkClient.getInstance().connect("localhost", 8080);
            }

            // send LOGIN request
            Request loginReq = new Request(Request.RequestType.LOGIN, username);
            Response response = NetworkClient.getInstance().sendRequest(loginReq);

            // handle server's response
            if (response.success()) {
                System.out.println("Login success! Switching scene...");
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("Login Success! Welcome " + username);

                // TODO: Put your Scene switching code here to go to AuctionListView
            } else {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText("Login failed: " + response.message());
            }

        } catch (Exception e) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Could not connect to server!");
            e.printStackTrace();
        }
    }
}