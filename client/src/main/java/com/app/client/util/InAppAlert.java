package com.app.client.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class InAppAlert {
    private InAppAlert() {
    }

    public static void showWarning(Node owner, String message) {
        show(owner, "Warning", message);
    }

    public static void showError(Node owner, String message) {
        show(owner, "Something went wrong", message);
    }

    private static void show(Node owner, String title, String message) {
        if (owner == null || owner.getScene() == null) {
            return;
        }

        Scene scene = owner.getScene();
        Parent root = scene.getRoot();
        if (!(root instanceof Pane pane)) {
            return;
        }

        pane.getChildren().removeIf(node -> node.getStyleClass().contains("app-alert-overlay"));

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("app-alert-overlay");
        overlay.setManaged(false);
        overlay.setLayoutX(0);
        overlay.setLayoutY(0);
        overlay.prefWidthProperty().bind(scene.widthProperty());
        overlay.prefHeightProperty().bind(scene.heightProperty());

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("app-alert-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("app-alert-message");
        messageLabel.setWrapText(true);

        Button dismissButton = new Button("OK");
        dismissButton.getStyleClass().add("primary-button");
        dismissButton.setDefaultButton(true);
        dismissButton.setOnAction(event -> pane.getChildren().remove(overlay));

        VBox card = new VBox(12, titleLabel, messageLabel, dismissButton);
        card.getStyleClass().add("app-alert-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(420);

        overlay.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        pane.getChildren().add(overlay);
        overlay.toFront();

        if (root instanceof Region) {
            overlay.resize(scene.getWidth(), scene.getHeight());
        }
        dismissButton.requestFocus();
    }
}
