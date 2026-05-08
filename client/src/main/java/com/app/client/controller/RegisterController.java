package com.app.client.controller;

import com.app.client.model.AuthModel;
import com.app.shared.exceptions.AuthenticationException;
import com.app.shared.models.user.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("BIDDER", "SELLER"));
        roleComboBox.getSelectionModel().select("BIDDER");
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        if (isBlank(username) || isBlank(email) || isBlank(password) || isBlank(confirmPassword)) {
            showError("Please fill in all fields.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        registerButton.setDisable(true);
        showError("Creating account...");

        new Thread(() -> {
            try {
                AuthModel authModel = new AuthModel();
                User registeredUser = authModel.register(username, password, email, role);
                Platform.runLater(() -> openAuctionList(event, registeredUser));
            } catch (AuthenticationException e) {
                Platform.runLater(() -> {
                    showError("Registration failed: " + e.getMessage());
                    registerButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("System error: " + e.getMessage());
                    registerButton.setDisable(false);
                });
            }
        }, "register-request").start();
    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        openLogin(event);
    }

    private void openAuctionList(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionListView.fxml"));
            Parent root = loader.load();

            AuctionListController controller = loader.getController();
            controller.initData(user.getUsername(), user.getRole());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 980, 650));
            stage.setTitle("tGauction - Auctions");
        } catch (IOException e) {
            showError("System error: " + e.getMessage());
            registerButton.setDisable(false);
        }
    }

    private void openLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 440, 560));
            stage.setTitle("tGauction - Login");
        } catch (IOException e) {
            showError("System error: " + e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void showError(String message) {
        errorLabel.setVisible(true);
        errorLabel.setText(message);
    }
}
