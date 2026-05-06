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
import com.app.client.model.AuctionListModel;

import java.io.IOException;

public class AuctionListController {

    @FXML private Label statusLabel;
    @FXML private ListView<String> auctionListView;

    private String currentUsername;
    private ObservableList<String> mockAuctions;

    private final AuctionListModel model = new AuctionListModel();

    @FXML
    public void initialize() {
        mockAuctions = FXCollections.observableArrayList();
        auctionListView.setItems(mockAuctions);
        refreshData();
    }

    // Called from LoginController to pass data
    public void initData(String username) {
        this.currentUsername = username;
        statusLabel.setText("Welcome, " + username);
    }

    private void refreshData() {
        new Thread(() -> {
            java.util.List<com.app.shared.models.auction.Auction> auctions = model.fetchAuctions();
            javafx.application.Platform.runLater(() -> {
                mockAuctions.clear();
                for (com.app.shared.models.auction.Auction a : auctions) {
                    mockAuctions.add(a.getItem().getName() + " | Current Bid: $" + a.getCurrentPrice() + " | Status: " + a.getStatus());
                }
            });
        }).start();
    }

    @FXML
    protected void handleRefresh(ActionEvent event) {
        System.out.println("Refreshing auctions from server...");
        refreshData();
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