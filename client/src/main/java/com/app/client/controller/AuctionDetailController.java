package com.app.client.controller;

import com.app.client.model.AuctionListModel;
import com.app.client.network.AuctionObserver;
import com.app.client.network.NetworkClient;
import com.app.client.util.InAppAlert;
import com.app.shared.models.auction.Auction;
import com.app.shared.models.auction.BidTransaction;
import com.app.shared.network.Response;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuctionDetailController implements AuctionObserver {
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label statusLabel;
    @FXML private Label itemNameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label auctionStatusLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label bidderTitleLabel;
    @FXML private Label highestBidderLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label timeRemainingLabel;
    @FXML private TextField bidAmountField;
    @FXML private TextField adminPriceField;
    @FXML private Button placeBidButton;
    @FXML private VBox adminControlsPanel;
    @FXML private ListView<BidTransaction> bidHistoryListView;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String currentUsername;
    private String currentRole;
    private Auction auction;
    private Timeline countdownTimeline;
    private final AuctionListModel model = new AuctionListModel();
    private final ObservableList<BidTransaction> bidHistory = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        bidHistoryListView.setItems(bidHistory);
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimeRemaining()));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        bidHistoryListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(BidTransaction bid, boolean empty) {
                super.updateItem(bid, empty);
                if (empty || bid == null) {
                    setText(null);
                    return;
                }

                String time = TIME_FORMAT.format(
                        Instant.ofEpochMilli(bid.getTimestamp()).atZone(ZoneId.systemDefault())
                );
                setText(String.format("$%.2f by %s at %s", bid.getAmount(), bid.getBidderId(), time));
            }
        });
    }

    public void initData(String username, String role, Auction selectedAuction) {
        this.currentUsername = username;
        this.currentRole = role;
        this.auction = selectedAuction;
        NetworkClient.getInstance().addObserver(this);
        boolean canBid = "BIDDER".equalsIgnoreCase(currentRole) || "ADMIN".equalsIgnoreCase(currentRole);
        bidAmountField.setVisible(canBid);
        bidAmountField.setManaged(canBid);
        placeBidButton.setVisible(canBid);
        placeBidButton.setManaged(canBid);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentRole);
        adminControlsPanel.setVisible(isAdmin);
        adminControlsPanel.setManaged(isAdmin);
        renderAuction();
        countdownTimeline.play();
        refreshHistory();
    }

    @FXML
    protected void handlePlaceBid(ActionEvent event) {
        if ("SELLER".equalsIgnoreCase(currentRole)) {
            showError("Sellers cannot place bids.");
            return;
        }
        try {
            double amount = Double.parseDouble(bidAmountField.getText());
            Response response = model.placeBid(auction.getId(), currentUsername, amount);
            if (!response.success()) {
                showError(response.message());
                return;
            }

            if (response.data() instanceof Auction updatedAuction) {
                auction = updatedAuction;
                renderAuction();
            }
            bidAmountField.clear();
            statusLabel.setText(response.message());
            refreshHistory();
        } catch (NumberFormatException e) {
            showError("Bid amount must be a valid number.");
        }
    }

    @FXML
    protected void handleRefreshHistory(ActionEvent event) {
        refreshHistory();
    }

    @FXML
    protected void handleSetAuctionPrice(ActionEvent event) {
        if (!"ADMIN".equalsIgnoreCase(currentRole)) {
            showError("Only admins can set auction prices.");
            return;
        }
        try {
            double price = Double.parseDouble(adminPriceField.getText());
            Response response = model.setAuctionPrice(auction.getId(), price);
            if (!response.success()) {
                showError(response.message());
                return;
            }
            if (response.data() instanceof Auction updatedAuction) {
                auction = updatedAuction;
                renderAuction();
            }
            adminPriceField.clear();
            statusLabel.setText(response.message());
        } catch (NumberFormatException e) {
            showError("Auction price must be a valid number.");
        }
    }

    @FXML
    protected void handleDeleteAuction(ActionEvent event) {
        if (!"ADMIN".equalsIgnoreCase(currentRole)) {
            showError("Only admins can delete auctions.");
            return;
        }

        Response response = model.deleteAuction(auction.getId());
        if (!response.success()) {
            showError(response.message());
            return;
        }
        statusLabel.setText(response.message());
        openAuctionList(event);
    }

    @FXML
    protected void handleConcludeAuction(ActionEvent event) {
        if (!"ADMIN".equalsIgnoreCase(currentRole)) {
            showError("Only admins can conclude auctions.");
            return;
        }

        Response response = model.concludeAuction(auction.getId());
        if (!response.success()) {
            showError(response.message());
            return;
        }
        if (response.data() instanceof Auction updatedAuction) {
            auction = updatedAuction;
            renderAuction();
        }
        statusLabel.setText(response.message());
    }

    @FXML
    protected void handleBack(ActionEvent event) {
        openAuctionList(event);
    }

    private void openAuctionList(ActionEvent event) {
        NetworkClient.getInstance().removeObserver(this);
        stopCountdownTimer();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionListView.fxml"));
            Parent root = loader.load();

            AuctionListController controller = loader.getController();
            controller.initData(currentUsername, currentRole);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 980, 650));
            stage.setTitle("tGauction - Auctions");
        } catch (IOException e) {
            showError("Could not return to auction list: " + e.getMessage());
        }
    }

    @Override
    public void onAuctionUpdated(Auction updatedAuction) {
        if (auction == null || !auction.getId().equals(updatedAuction.getId())) {
            return;
        }
        auction = updatedAuction;
        renderAuction();
        refreshHistory();
    }

    @Override
    public void onServerMessage(Response response) {
        if (response.message() != null && !response.message().isBlank()) {
            statusLabel.setText(response.message());
        }
    }

    private void renderAuction() {
        if (auction == null) {
            return;
        }

        titleLabel.setText(auction.getItem().getName());
        subtitleLabel.setText("Auction ID: " + auction.getId());
        itemNameLabel.setText(auction.getItem().getName());
        descriptionLabel.setText(auction.getItem().getDescription());
        auctionStatusLabel.setText(auction.getStatus().name());
        currentPriceLabel.setText(String.format("$%.2f", auction.getCurrentPrice()));
        bidderTitleLabel.setText(isWinnerDecided() ? "Winner" : "Highest Bidder");
        highestBidderLabel.setText(auction.getHighestBidderId() == null ? "No bids yet" : auction.getHighestBidderId());
        endTimeLabel.setText(TIME_FORMAT.format(
                Instant.ofEpochMilli(auction.getEndTime()).atZone(ZoneId.systemDefault())
        ));
        bidAmountField.setPromptText(String.format("Minimum %.2f", auction.getCurrentPrice() + 1));
        adminPriceField.setPromptText(String.format("Current %.2f", auction.getCurrentPrice()));
        updateTimeRemaining();
    }

    private void refreshHistory() {
        if (auction == null) {
            return;
        }

        new Thread(() -> {
            List<BidTransaction> bids = model.fetchBidHistory(auction.getId());
            Platform.runLater(() -> bidHistory.setAll(bids));
        }, "bid-history-refresh").start();
    }

    private void showError(String message) {
        InAppAlert.showError(statusLabel, message);
    }

    private void updateTimeRemaining() {
        if (auction == null || timeRemainingLabel == null) {
            return;
        }

        long millisLeft = auction.getEndTime() - System.currentTimeMillis();
        if (millisLeft <= 0 || auction.getStatus() == Auction.Status.FINISHED) {
            timeRemainingLabel.setText("Ended");
            auctionStatusLabel.setText(auction.getStatus().name());
            stopCountdownTimer();
            return;
        }

        long totalSeconds = millisLeft / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        timeRemainingLabel.setText(String.format("%d days %02d hours %02d minutes %02d seconds left",
                days, hours, minutes, seconds));
    }

    private void stopCountdownTimer() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
    }

    private boolean isWinnerDecided() {
        Auction.Status status = auction.getStatus();
        return status == Auction.Status.FINISHED || status == Auction.Status.PAID;
    }
}
