package com.app.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.app.client.model.AuthModel;
import com.app.shared.models.user.User;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty()) {
            errorLabel.setVisible(true);
            errorLabel.setText("Please enter a username.");
            return;
        }
        if (password == null || password.isBlank()) {
            errorLabel.setVisible(true);
            errorLabel.setText("Please enter your password.");
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setVisible(true);
        errorLabel.setText("Connecting...");

        new Thread(() -> {
            try {
                // send LOGIN request
                AuthModel authModel = new AuthModel();
                User loggedInUser = authModel.login(username, password);

                javafx.application.Platform.runLater(() -> {
                    try {
                        System.out.println("Login success! Switching scene...");
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionListView.fxml"));
                        Parent root = loader.load();

                        AuctionListController controller = loader.getController();
                        controller.initData(loggedInUser.getUsername(), loggedInUser.getRole());

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root, 980, 650));
                        stage.setTitle("tGhauction - Auctions");
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

    @FXML
    public void handleOpenRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 500, 620));
            stage.setTitle("tGauction - Register");
        } catch (IOException e) {
            errorLabel.setVisible(true);
            errorLabel.setText("System error: " + e.getMessage());
        }
    }
}
