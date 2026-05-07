package com.app.client;

import com.app.client.network.NetworkClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class ClientMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // 1. CONNECT TO THE SERVER FIRST!
        try {
            // Replace 8080 with whatever port your server is using in ServerMain
            NetworkClient.getInstance().connect("localhost", 8080);
            System.out.println("Connected to the server successfully.");
        } catch (Exception e) {
            System.err.println("Failed to connect to server. Is the server running?");
            e.printStackTrace();
            // Optional: Show an error alert to the user here
        }
        URL fxmlLocation = getClass().getResource("/fxml/LoginView.fxml");

        assert fxmlLocation != null;
        Parent root = FXMLLoader.load(fxmlLocation);
        primaryStage.setTitle("tGhauction");
        primaryStage.setScene(new Scene(root, 440, 560));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
