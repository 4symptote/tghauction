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
import com.app.client.model.AuthModel;
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

        loginButton.setDisable(true);
        errorLabel.setVisible(true);
        errorLabel.setText("Connecting...");

        new Thread(() -> {
            try {
                // send LOGIN request
                AuthModel authModel = new AuthModel();
                authModel.login(username);

                javafx.application.Platform.runLater(() -> {
                    try {
                        System.out.println("Login success! Switching scene...");
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionListView.fxml"));
                        Parent root = loader.load();

                        AuctionListController controller = loader.getController();
                        controller.initData(username);

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root, 800, 600));
                        stage.setTitle("tGauction - Auctions");
                    } catch (IOException e) {
                        errorLabel.setText("System error: " + e.getMessage());
                        loginButton.setDisable(false);
                    }
                });
            } catch (com.app.shared.exceptions.AuthenticationException e) {
                javafx.application.Platform.runLater(() -> {
                    errorLabel.setText("Login failed: " + e.getMessage());
                    loginButton.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    errorLabel.setText("System error: " + e.getMessage());
                    loginButton.setDisable(false);
                });
            }
        }).start();
    }
}