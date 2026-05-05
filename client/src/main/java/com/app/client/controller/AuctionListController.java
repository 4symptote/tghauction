package com.app.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

public class AuctionListController {

    @FXML private Label userLabel;
    @FXML private ListView<String> auctionListView; // We use String temporarily to mock data

    private String currentUsername;
    private ObservableList<String> mockAuctions;

    @FXML
    public void initialize() {
        // Runs automatically when the FXML is loaded. Let's add some mock data!
        mockAuctions = FXCollections.observableArrayList(
                "Gaming Laptop (RTX 4090) | Seller: seller1 | Current Bid: $1200 | Status: RUNNING",
                "Vintage Rolex Submariner | Seller: seller2 | Current Bid: $3500 | Status: RUNNING",
                "Original Oil Painting | Seller: artist99 | Current Bid: $500 | Status: OPEN"
        );
        auctionListView.setItems(mockAuctions);
    }

    // Called from LoginController to pass data
    public void initData(String username) {
        this.currentUsername = username;
        userLabel.setText("Welcome, " + username);
    }

    @FXML
    protected void handleRefresh(ActionEvent event) {
        // TODO: Request the latest auction list from the Server
        System.out.println("Refreshing auctions from server...");
    }

    @FXML
    protected void handlePlaceBid(ActionEvent event) {
        String selectedItem = auctionListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an auction to bid on!");
            alert.showAndWait();
            return;
        }

        System.out.println(currentUsername + " clicked to bid on: " + selectedItem);
        // TODO: Switch to the Bid/Item Detail view
    }

    @FXML
    protected void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 300));
            stage.setTitle("Auction Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}