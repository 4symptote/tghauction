package com.app.client.network;

import com.app.shared.network.Request;
import com.app.shared.network.Response;
import com.app.shared.models.auction.Auction;
import javafx.application.Platform;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.app.shared.network.Subject;

public class ServerConnection implements Subject<AuctionObserver> {
    // Singleton pattern for the connection so all controllers use the same socket
    private static ServerConnection instance;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // List of UI controllers watching for updates
    private final List<AuctionObserver> observers = new ArrayList<>();

    private ServerConnection() {}

    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    // Connects to the server (Call this when JavaFX app starts!)
    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to the Auction Server!");

            // Start a background thread to CONSTANTLY listen for server updates
            Thread listenerThread = new Thread(this::listenToServer);
            listenerThread.setDaemon(true); // Ensures thread closes when UI closes
            listenerThread.start();

        } catch (Exception e) {
            System.err.println("Could not connect to server: " + e.getMessage());
        }
    }

    // Sends a message to the server
    public void sendRequest(Request request) {
        try {
            if (out != null) {
                out.writeObject(request);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- OBSERVER PATTERN LOGIC ---

    @Override
    public void addObserver(AuctionObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    // The Background Listener Loop
    private void listenToServer() {
        try {
            while (true) {
                Response response = (Response) in.readObject();

                // When we get a response, pass it to the UI controllers safely!
                Platform.runLater(() -> {
                    for (AuctionObserver observer : observers) {
                        observer.onServerMessage(response);

                        // If the server explicitly sent an updated Auction, trigger the specific method
                        if (response.data() != null && response.data() instanceof Auction) {
                            observer.onAuctionUpdated((Auction) response.data());
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Disconnected from server.");
        }
    }
}