package com.app.client.controller;

import com.app.client.network.AuctionObserver;
import com.app.client.network.NetworkClient;
import com.app.shared.models.auction.Auction;
import com.app.shared.network.Response;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.app.client.model.AuctionListModel;

import java.io.IOException;

public class AuctionListController implements AuctionObserver {

    @FXML private Label statusLabel;
    @FXML private ListView<Auction> auctionListView;
    @FXML private Button createAuctionButton;
    @FXML private Button detailsButton;

    private String currentUsername;
    private String currentRole;
    private ObservableList<Auction> auctions;

    private final AuctionListModel model = new AuctionListModel();

    @FXML
    public void initialize() {
        auctions = FXCollections.observableArrayList();
        auctionListView.setItems(auctions);
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
                Label nameLabel = new Label(auction.getItem().getName());
                nameLabel.getStyleClass().add("auction-name");

                Label metaLabel = new Label("Winner: " + bidder + "  |  " + auction.getItem().getDescription());
                metaLabel.getStyleClass().add("auction-meta");
                metaLabel.setWrapText(true);

                VBox textBox = new VBox(5, nameLabel, metaLabel);
                HBox.setHgrow(textBox, Priority.ALWAYS);

                Label priceLabel = new Label(String.format("$%.2f", auction.getCurrentPrice()));
                priceLabel.getStyleClass().add("auction-price");

                Label statusBadge = new Label(auction.getStatus().name());
                statusBadge.getStyleClass().add("status-badge");

                VBox valueBox = new VBox(7, priceLabel, statusBadge);
                valueBox.setMinWidth(130);

                Pane spacer = new Pane();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox card = new HBox(14, textBox, spacer, valueBox);
                card.getStyleClass().add("auction-card");

                setText(null);
                setGraphic(card);
            }
        });
        auctionListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && auctionListView.getSelectionModel().getSelectedItem() != null) {
                openAuctionDetail();
            }
        });
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
        createAuctionButton.setVisible(canCreateAuction);
        createAuctionButton.setManaged(canCreateAuction);
        detailsButton.setText("SELLER".equalsIgnoreCase(currentRole) ? "View" : "Details");
        NetworkClient.getInstance().addObserver(this);
    }

    private void refreshData() {
        new Thread(() -> {
            java.util.List<Auction> fetchedAuctions = model.fetchAuctions();
            javafx.application.Platform.runLater(() -> {
                auctions.setAll(fetchedAuctions);
                auctionListView.refresh();
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
        openAuctionDetail();
    }

    @FXML
    protected void handleViewDetails(ActionEvent event) {
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
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an auction to bid on!");
            alert.showAndWait();
            return;
        }

        NetworkClient.getInstance().removeObserver(this);
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
        for (int i = 0; i < auctions.size(); i++) {
            if (auctions.get(i).getId().equals(updatedAuction.getId())) {
                auctions.set(i, updatedAuction);
                auctionListView.refresh();
                statusLabel.setText("Auction updated: " + updatedAuction.getItem().getName());
                return;
            }
        }
        auctions.add(updatedAuction);
        statusLabel.setText("New auction: " + updatedAuction.getItem().getName());
    }

    @Override
    public void onServerMessage(Response response) {
        if (response.message() != null && !response.message().isBlank()) {
            statusLabel.setText(response.message());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
