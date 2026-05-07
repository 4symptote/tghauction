package com.app.client.controller;

import com.app.client.network.AuctionObserver;
import com.app.client.network.NetworkClient;
import com.app.client.util.InAppAlert;
import com.app.shared.models.auction.Auction;
import com.app.shared.network.Response;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.app.client.model.AuctionListModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuctionListController implements AuctionObserver {

    @FXML private Label statusLabel;
    @FXML private ListView<Auction> auctionListView;
    @FXML private Button createAuctionButton;
    @FXML private Button sellerFilterButton;

    private String currentUsername;
    private String currentRole;
    private ObservableList<Auction> auctions;
    private final List<Auction> allAuctions = new ArrayList<>();
    private boolean showingSellerAuctions;
    private static boolean preferOwnAuctions;
    private Timeline countdownTimeline;

    private final AuctionListModel model = new AuctionListModel();

    @FXML
    public void initialize() {
        auctions = FXCollections.observableArrayList();
        auctionListView.setItems(auctions);
        sellerFilterButton.setVisible(false);
        sellerFilterButton.setManaged(false);
        auctionListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Auction auction, boolean empty) {
                super.updateItem(auction, empty);
                if (empty || auction == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String bidder = auction.getHighestBidderId() == null ? "none" : auction.getHighestBidderId();
                String bidderLabel = isWinnerDecided(auction) ? "Winner" : "Highest Bidder";
                Label nameLabel = new Label(auction.getItem().getName());
                nameLabel.getStyleClass().add("auction-name");

                Label metaLabel = new Label(
                        "Seller: " + auction.getItem().getSellerId()
                                + "  |  " + bidderLabel + ": " + bidder
                                + "  |  " + auction.getItem().getDescription()
                );
                metaLabel.getStyleClass().add("auction-meta");
                metaLabel.setWrapText(true);

                VBox textBox = new VBox(5, nameLabel, metaLabel);
                textBox.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(textBox, Priority.ALWAYS);

                Label priceLabel = new Label(String.format("$%.2f", auction.getCurrentPrice()));
                priceLabel.getStyleClass().add("auction-price");

                Label statusBadge = new Label(auction.getStatus().name());
                statusBadge.getStyleClass().add("status-badge");

                Label timeLabel = new Label(formatTimeRemaining(auction));
                timeLabel.getStyleClass().add("auction-time");
                timeLabel.setVisible(auction.getStatus() != Auction.Status.FINISHED);
                timeLabel.setManaged(auction.getStatus() != Auction.Status.FINISHED);

                VBox valueBox = new VBox(7, priceLabel, statusBadge, timeLabel);
                valueBox.setAlignment(Pos.CENTER_RIGHT);
                valueBox.setMinWidth(130);

                Pane spacer = new Pane();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox card = new HBox(14, textBox, spacer, valueBox);
                card.setAlignment(Pos.CENTER_LEFT);
                card.getStyleClass().add("auction-card");

                setText(null);
                setGraphic(card);
            }
        });
        auctionListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && auctionListView.getSelectionModel().getSelectedItem() != null) {
                openAuctionDetail();
            }
        });
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshCountdowns()));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshData();
    }

    // Called from LoginController to pass data
    public void initData(String username) {
        initData(username, "BIDDER");
    }

    public void initData(String username, String role) {
        this.currentUsername = username;
        this.currentRole = role == null ? "BIDDER" : role;
        statusLabel.setText("Welcome, " + username + " (" + currentRole + ")");
        boolean canCreateAuction = "SELLER".equalsIgnoreCase(currentRole) || "ADMIN".equalsIgnoreCase(currentRole);
        boolean canFilterOwnAuctions = "SELLER".equalsIgnoreCase(currentRole) || "ADMIN".equalsIgnoreCase(currentRole);
        createAuctionButton.setVisible(canCreateAuction);
        createAuctionButton.setManaged(canCreateAuction);
        sellerFilterButton.setVisible(canFilterOwnAuctions);
        sellerFilterButton.setManaged(canFilterOwnAuctions);
        showingSellerAuctions = canFilterOwnAuctions && preferOwnAuctions;
        updateSellerFilterButton();
        applyAuctionFilter();
        NetworkClient.getInstance().addObserver(this);
    }

    private void refreshData() {
        new Thread(() -> {
            java.util.List<Auction> fetchedAuctions = model.fetchAuctions();
            javafx.application.Platform.runLater(() -> {
                allAuctions.clear();
                allAuctions.addAll(fetchedAuctions);
                applyAuctionFilter();
            });
        }).start();
    }

    @FXML
    protected void handleToggleSellerAuctions(ActionEvent event) {
        showingSellerAuctions = !showingSellerAuctions;
        preferOwnAuctions = showingSellerAuctions;
        updateSellerFilterButton();
        applyAuctionFilter();
    }

    @FXML
    protected void handleRefresh(ActionEvent event) {
        System.out.println("Refreshing auctions from server...");
        refreshData();
    }

    @FXML
    protected void handlePlaceBid(ActionEvent event) {
        openAuctionDetail();
    }

    @FXML
    protected void handleCreateAuction(ActionEvent event) {
        if (!"SELLER".equalsIgnoreCase(currentRole) && !"ADMIN".equalsIgnoreCase(currentRole)) {
            showError("Only sellers and admins can create auctions.");
            return;
        }
        NetworkClient.getInstance().removeObserver(this);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateAuctionView.fxml"));
            Parent root = loader.load();

            CreateAuctionController controller = loader.getController();
            controller.initData(currentUsername, currentRole);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 980, 650));
            stage.setTitle("tGauction - Create Auction");
        } catch (IOException e) {
            showError("Could not open create auction screen: " + e.getMessage());
        }
    }

    private void openAuctionDetail() {
        Auction selectedAuction = auctionListView.getSelectionModel().getSelectedItem();
        if (selectedAuction == null) {
            InAppAlert.showWarning(auctionListView, "Please select an auction to bid on.");
            return;
        }

        NetworkClient.getInstance().removeObserver(this);
        stopCountdownTimer();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionDetailView.fxml"));
            Parent root = loader.load();

            AuctionDetailController controller = loader.getController();
            controller.initData(currentUsername, currentRole, selectedAuction);

            Stage stage = (Stage) auctionListView.getScene().getWindow();
            stage.setScene(new Scene(root, 980, 650));
            stage.setTitle("tGauction - Auction Detail");
        } catch (IOException e) {
            showError("Could not open auction detail: " + e.getMessage());
        }
    }

    @FXML
    protected void handleLogout(ActionEvent event) {
        NetworkClient.getInstance().removeObserver(this);
        stopCountdownTimer();
        preferOwnAuctions = false;
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 440, 560));
            stage.setTitle("Auction Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAuctionUpdated(Auction updatedAuction) {
        for (int i = 0; i < allAuctions.size(); i++) {
            if (allAuctions.get(i).getId().equals(updatedAuction.getId())) {
                allAuctions.set(i, updatedAuction);
                applyAuctionFilter();
                statusLabel.setText("Auction updated: " + updatedAuction.getItem().getName());
                return;
            }
        }
        allAuctions.add(updatedAuction);
        applyAuctionFilter();
        statusLabel.setText("New auction: " + updatedAuction.getItem().getName());
    }

    @Override
    public void onServerMessage(Response response) {
        if (response.message() != null && !response.message().isBlank()) {
            statusLabel.setText(response.message());
        }
    }

    private void showError(String message) {
        InAppAlert.showError(statusLabel, message);
    }

    private void applyAuctionFilter() {
        if (showingSellerAuctions && currentUsername != null) {
            auctions.setAll(allAuctions.stream()
                    .filter(auction -> currentUsername.equalsIgnoreCase(auction.getItem().getSellerId()))
                    .toList());
        } else {
            auctions.setAll(allAuctions);
        }
        auctionListView.refresh();
        refreshCountdowns();
    }

    private void updateSellerFilterButton() {
        sellerFilterButton.setText(showingSellerAuctions ? "All Auctions" : "My Auctions");
    }

    private void refreshCountdowns() {
        if (hasAuctionUnderFiveMinutes()) {
            auctionListView.refresh();
            startCountdownTimer();
        } else {
            stopCountdownTimer();
        }
    }

    private boolean hasAuctionUnderFiveMinutes() {
        long now = System.currentTimeMillis();
        for (Auction auction : auctions) {
            long millisLeft = auction.getEndTime() - now;
            if (millisLeft > 0 && millisLeft < 5 * 60 * 1000L) {
                return true;
            }
        }
        return false;
    }

    private void startCountdownTimer() {
        if (countdownTimeline != null && countdownTimeline.getStatus() != Timeline.Status.RUNNING) {
            countdownTimeline.play();
        }
    }

    private void stopCountdownTimer() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
    }

    private String formatTimeRemaining(Auction auction) {
        long millisLeft = auction.getEndTime() - System.currentTimeMillis();
        if (millisLeft <= 0 || auction.getStatus() == Auction.Status.FINISHED) {
            return "Ended";
        }

        long totalSeconds = millisLeft / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (days > 0) {
            return String.format("%dd %dh left", days, hours);
        }
        if (hours > 0) {
            return String.format("%dh %dm left", hours, minutes);
        }
        if (totalSeconds < 300) {
            return String.format("%dm %02ds left", minutes, seconds);
        }
        return String.format("%dm left", Math.max(1, minutes));
    }

    private boolean isWinnerDecided(Auction auction) {
        Auction.Status status = auction.getStatus();
        return status == Auction.Status.FINISHED || status == Auction.Status.PAID;
    }
}
