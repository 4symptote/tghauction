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
import com.app.client.model.AuthModel;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();

        if (username == null || username.trim().isEmpty()) {
            errorLabel.setText("Please enter a username.");
            return;
        }

        try {
            // Delegate the logic to the Model
            AuthModel authModel = new AuthModel();
            authModel.login(username);

            // If the model doesn't throw an Exception, the login was successful. Switch scenes!
            System.out.println("Login success! Switching scene...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionListView.fxml"));
            Parent root = loader.load();

            AuctionListController controller = loader.getController();
            controller.initData(username);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("tGhauction - Auctions");

        } catch (com.app.shared.exceptions.AuthenticationException e) {
            errorLabel.setVisible(true);
            errorLabel.setText("Login failed: " + e.getMessage());
        } catch (Exception e) {
            errorLabel.setVisible(true);
            errorLabel.setText("System error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}