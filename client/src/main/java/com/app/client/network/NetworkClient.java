package com.app.client.network;

import com.app.shared.models.auction.Auction;
import com.app.shared.network.Request;
import com.app.shared.network.Response;
import com.app.shared.network.Subject;
import javafx.application.Platform;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkClient implements Subject<AuctionObserver> {
    private static NetworkClient instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final BlockingQueue<Response> directResponses = new LinkedBlockingQueue<>();
    private final List<AuctionObserver> observers = new ArrayList<>();
    private volatile boolean listening;

    private NetworkClient() {}

    public static NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && out != null && in != null;
    }

    public void connect(String host, int port) throws Exception {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        startListener();
        System.out.println("Connected to server!");
    }

    public Response sendRequest(Request request) {
        if (!isConnected()) {
            return new Response(false, "Not connected to the server. Please try again.", null);
        }

        try {
            synchronized (out) {
                out.writeObject(request);
                out.flush();
            }
            return directResponses.take();
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Network error: " + e.getMessage(), null);
        }
    }

    @Override
    public synchronized void addObserver(AuctionObserver observer) {
        observers.add(observer);
    }

    @Override
    public synchronized void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    private void startListener() {
        if (listening) {
            return;
        }

        listening = true;
        Thread listener = new Thread(() -> {
            while (isConnected()) {
                try {
                    Response response = (Response) in.readObject();
                    if ("AUCTION_UPDATE".equals(response.message())) {
                        notifyObservers(response);
                    } else {
                        directResponses.offer(response);
                    }
                } catch (Exception e) {
                    listening = false;
                    directResponses.offer(new Response(false, "Disconnected from server", null));
                    break;
                }
            }
        }, "auction-client-listener");
        listener.setDaemon(true);
        listener.start();
    }

    private void notifyObservers(Response response) {
        List<AuctionObserver> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<>(observers);
        }

        Platform.runLater(() -> {
            for (AuctionObserver observer : snapshot) {
                observer.onServerMessage(response);
                if (response.data() instanceof Auction auction) {
                    observer.onAuctionUpdated(auction);
                }
            }
        });
    }

    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            listening = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
