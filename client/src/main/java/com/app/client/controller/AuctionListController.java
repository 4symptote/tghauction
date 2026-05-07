package com.app.client.controller;

import com.app.client.model.AuctionListModel;
import com.app.shared.models.auction.Auction;
import com.app.shared.network.Response;

import javafx.application.Platform;
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
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuctionListController {

    @FXML private Label statusLabel;
    @FXML private ListView<String> auctionListView;

    private String currentUsername;
    private ObservableList<String> mockAuctions;

    // Lưu object Auction thật để lấy auctionId khi đặt bid
    private List<Auction> auctions = new ArrayList<>();

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
            List<Auction> fetchedAuctions = model.fetchAuctions();

            Platform.runLater(() -> {
                auctions = fetchedAuctions;
                mockAuctions.clear();

                for (Auction a : auctions) {
                    mockAuctions.add(
                            a.getItem().getName()
                                    + " | Current Bid: $"
                                    + a.getCurrentPrice()
                                    + " | Status: "
                                    + a.getStatus()
                    );
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
        int selectedIndex = auctionListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex < 0 || selectedIndex >= auctions.size()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an auction to bid on!");
            alert.showAndWait();
            return;
        }

        Auction selectedAuction = auctions.get(selectedIndex);

        TextInputDialog dialog = new TextInputDialog(
                String.valueOf(selectedAuction.getCurrentPrice() + 1)
        );
        dialog.setTitle("Place Bid");
        dialog.setHeaderText("Bid for: " + selectedAuction.getItem().getName());
        dialog.setContentText("Enter your bid amount:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(result.get().trim());
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid bid amount.");
            alert.showAndWait();
            return;
        }

        String bidderId = currentUsername != null ? currentUsername : "guest";

        new Thread(() -> {
            Response response = model.placeBid(
                    selectedAuction.getId(),
                    bidderId,
                    amount
            );

            Platform.runLater(() -> {
                Alert alert = new Alert(
                        response.success() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        response.message()
                );
                alert.showAndWait();

                if (response.success()) {
                    refreshData();
                }
            });
        }).start();
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