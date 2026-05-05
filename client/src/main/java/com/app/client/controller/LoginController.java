package com.app.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.app.client.network.NetworkClient;
import com.app.shared.network.Request;
import com.app.shared.network.Response;
import java.io.IOException;

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
            //testing
            if (NetworkClient.getInstance().sendRequest(null) == null) { // Simple check
                NetworkClient.getInstance().connect("localhost", 8080);
            }

            // send LOGIN request
            Request loginReq = new Request(Request.RequestType.LOGIN, username);
            Response response = NetworkClient.getInstance().sendRequest(loginReq);

            // handle server's response
            if (response.success()) {
                System.out.println("Login success! Switching scene...");
                // TODO: Put your Scene switching code here to go to AuctionListView
            } else {
                errorLabel.setText("Login failed: " + response.message());
            }

        } catch (Exception e) {
            errorLabel.setText("Could not connect to server!");
            e.printStackTrace();
        }
    }
}