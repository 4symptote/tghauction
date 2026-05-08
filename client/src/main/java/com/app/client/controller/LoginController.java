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

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    public void initialize() {
        // Khởi tạo các sự kiện mặc định nếu cần
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            return;
        }

        System.out.println("Attempting to login with: " + email);

        // TODO: Gửi yêu cầu đăng nhập lên Server thông qua Socket hoặc API
        // Ví dụ:
        // boolean success = AuthService.login(email, password);
        // if (success) {
        //     SceneManager.getInstance().switchScene("MainAppView.fxml");
        // } else {
        //     errorLabel.setText("Invalid credentials!");
        // }

        try {
            System.out.println("Login success! Switching scene...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionListView.fxml"));
            Parent root = loader.load();

            AuctionListController controller = loader.getController();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 980, 650));
            stage.setTitle("tGhauction - Auctions");
        } catch (IOException e) {
            errorLabel.setText("System error: " + e.getMessage());
            loginButton.setDisable(false);
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            errorLabel.setText("Please enter both email and password to register.");
            return;
        }

        System.out.println("Attempting to register: " + email);

        // TODO: Gửi yêu cầu đăng ký lên Server
        // ...
    }
}