package com.app.client.controller;

import com.app.client.model.AuctionListModel;
import com.app.client.util.InAppAlert;
import com.app.shared.network.Response;
import com.app.shared.network.payload.CreateAuctionPayload;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class CreateAuctionController {
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> itemTypeComboBox;
    @FXML private TextField itemNameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField startingPriceField;
    @FXML private TextField durationField;
    @FXML private ComboBox<String> durationUnitComboBox;

    private String currentUsername;
    private String currentRole;
    private final AuctionListModel model = new AuctionListModel();

    @FXML
    public void initialize() {
        itemTypeComboBox.setItems(FXCollections.observableArrayList("Electronics", "Art", "Vehicle"));
        itemTypeComboBox.getSelectionModel().selectFirst();
        durationUnitComboBox.setItems(FXCollections.observableArrayList("Seconds", "Minutes", "Hours"));
        durationUnitComboBox.getSelectionModel().select("Minutes");
    }

    public void initData(String username, String role) {
        this.currentUsername = username;
        this.currentRole = role;
        statusLabel.setText("Seller: " + username);
    }

    @FXML
    protected void handleCreateAuction(ActionEvent event) {
        try {
            String name = itemNameField.getText();
            String description = descriptionArea.getText();
            double startingPrice = Double.parseDouble(startingPriceField.getText());
            long duration = Long.parseLong(durationField.getText());
            long durationMillis = toDurationMillis(duration, durationUnitComboBox.getValue());

            CreateAuctionPayload payload = new CreateAuctionPayload(
                    itemTypeComboBox.getValue(),
                    name,
                    description,
                    startingPrice,
                    currentUsername,
                    durationMillis
            );
            Response response = model.createAuction(payload);
            if (!response.success()) {
                showError(response.message());
                return;
            }

            statusLabel.setText(response.message());
            openAuctionList(event);
        } catch (NumberFormatException e) {
            showError("Starting price and duration must be valid numbers.");
        }
    }

    private long toDurationMillis(long duration, String unit) {
        if (duration <= 0) {
            throw new NumberFormatException("Duration must be positive.");
        }
        return switch (unit == null ? "Minutes" : unit) {
            case "Seconds" -> Math.multiplyExact(duration, 1000L);
            case "Hours" -> Math.multiplyExact(duration, 60L * 60L * 1000L);
            default -> Math.multiplyExact(duration, 60L * 1000L);
        };
    }

    @FXML
    protected void handleBack(ActionEvent event) {
        openAuctionList(event);
    }

    private void openAuctionList(ActionEvent event) {
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

    private void showError(String message) {
        InAppAlert.showError(statusLabel, message);
    }
}
