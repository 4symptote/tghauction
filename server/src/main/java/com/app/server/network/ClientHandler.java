package com.app.server.network;

import com.app.server.service.AuctionManager;
import com.app.server.service.BidService;
import com.app.shared.models.auction.Auction;
import com.app.shared.network.Request;
import com.app.shared.network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final AuctionManager auctionManager = AuctionManager.getInstance();
    private final BidService bidService = BidService.getInstance();

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());

            while (!clientSocket.isClosed()) {
                Request request = (Request) in.readObject();

                if (request == null) {
                    continue;
                }

                Response response = handleRequest(request);

                out.reset();
                out.writeObject(response);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("Client disconnected or error occurred: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    private Response handleRequest(Request request) {
        switch (request.type()) {
            case LOGIN:
                String username = (String) request.payload();
                System.out.println("User logged in: " + username);
                return new Response(true, "Login successful", username);

            case GET_AUCTIONS:
                return new Response(
                        true,
                        "Auction list fetched successfully",
                        new ArrayList<>(auctionManager.getAllActiveAuctions().values())
                );

            case CREATE_AUCTION:
                Auction auction = (Auction) request.payload();
                auctionManager.addAuction(auction);
                return new Response(true, "Auction created successfully", auction);

            case PLACE_BID:
                return handlePlaceBid(request.payload());

            default:
                return new Response(false, "Unknown command", null);
        }
    }

    private Response handlePlaceBid(Object payload) {
        if (!(payload instanceof Object[] data) || data.length < 3) {
            return new Response(false, "Invalid bid payload", null);
        }

        try {
            String auctionId = (String) data[0];
            String bidderId = (String) data[1];
            double amount = ((Number) data[2]).doubleValue();

            bidService.placeBid(auctionId, bidderId, amount);

            Auction updatedAuction = auctionManager.getAuction(auctionId);
            return new Response(true, "Bid placed successfully", updatedAuction);

        } catch (Exception e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    private void closeConnections() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}