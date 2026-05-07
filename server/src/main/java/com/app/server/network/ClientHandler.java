package com.app.server.network;

import com.app.server.service.AuctionManager;
import com.app.server.service.BidService;
import com.app.server.service.UserManager;
import com.app.shared.exceptions.AuctionClosedException;
import com.app.shared.exceptions.InvalidBidException;
import com.app.shared.models.auction.Auction;
import com.app.shared.models.item.Creator.ArtCreator;
import com.app.shared.models.item.Creator.ElectronicCreator;
import com.app.shared.models.item.Creator.ItemCreator;
import com.app.shared.models.item.Creator.VehicleCreator;
import com.app.shared.models.item.Item;
import com.app.shared.network.AuctionObserver;
import com.app.shared.network.Request;
import com.app.shared.network.Response;
import com.app.shared.network.payload.AuctionIdPayload;
import com.app.shared.network.payload.BidPayload;
import com.app.shared.network.payload.CreateAuctionPayload;
import com.app.shared.network.payload.LoginPayload;
import com.app.shared.network.payload.RegisterPayload;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable, AuctionObserver {
    private final Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final AuctionManager auctionManager = AuctionManager.getInstance();
    private final BidService bidService = BidService.getInstance();
    private final UserManager userManager = UserManager.getInstance();
    private String currentUsername;
    private String currentRole;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());
            ClientManager.getInstance().addSocketClient(this);

            while (!clientSocket.isClosed()) {
                Request request = (Request) in.readObject();

                // Guard clause: ignore nulls
                if (request == null) {
                    continue;
                }

                Response response = handleRequest(request);

                sendResponse(response);
            }
        } catch (Exception e) {
            // Broadened catch: This will now catch structural/NPE issues before breaking the loop blindly
            System.out.println("Client disconnected or error occurred: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    private Response handleRequest(Request request) {
        // As long as request is not null, this remains safe
        try {
            switch (request.type()) {
                case LOGIN:
                    return handleLogin(request.payload());
                case REGISTER:
                    return handleRegister(request.payload());
                case GET_AUCTIONS:
                    return new Response(true, "Auctions loaded", auctionManager.listAuctions());
                case CREATE_AUCTION:
                    return handleCreateAuction(request.payload());
                case DELETE_AUCTION:
                    return handleDeleteAuction(request.payload());
                case PLACE_BID:
                    return handlePlaceBid(request.payload());
                case GET_BID_HISTORY:
                    return handleBidHistory(request.payload());
                default:
                    return new Response(false, "Unknown command", null);
            }
        } catch (ClassCastException e) {
            return new Response(false, "Invalid request payload: " + e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    private Response handleLogin(Object payload) {
        LoginPayload loginPayload = (LoginPayload) payload;
        var user = userManager.login(loginPayload);
        currentUsername = user.getUsername();
        currentRole = user.getRole();
        System.out.println("User logged in: " + user.getUsername());
        return new Response(true, "Login successful", user);
    }

    private Response handleRegister(Object payload) {
        RegisterPayload registerPayload = (RegisterPayload) payload;
        var user = userManager.register(registerPayload);
        currentUsername = user.getUsername();
        currentRole = user.getRole();
        System.out.println("User registered: " + user.getUsername());
        return new Response(true, "Registration successful", user);
    }

    private Response handleCreateAuction(Object payload) {
        if (!"SELLER".equalsIgnoreCase(currentRole) && !"ADMIN".equalsIgnoreCase(currentRole)) {
            return new Response(false, "Only sellers and admins can create auctions.", null);
        }
        CreateAuctionPayload create = (CreateAuctionPayload) payload;
        if (create.name() == null || create.name().isBlank()) {
            return new Response(false, "Item name is required", null);
        }
        if (create.startingPrice() <= 0) {
            return new Response(false, "Starting price must be positive", null);
        }
        if (create.durationMillis() <= 0) {
            return new Response(false, "Duration must be positive", null);
        }

        String itemType = create.itemType() == null ? "" : create.itemType().toLowerCase();
        ItemCreator creator = switch (itemType) {
            case "art" -> new ArtCreator();
            case "vehicle" -> new VehicleCreator();
            default -> new ElectronicCreator();
        };
        Item item = creator.createItem(
                create.name().trim(),
                create.description() == null ? "" : create.description().trim(),
                create.startingPrice(),
                currentUsername
        );
        Auction auction = new Auction(item, create.durationMillis());
        auctionManager.addAuction(auction);
        ClientManager.getInstance().broadcastAuctionUpdate(auction);
        return new Response(true, "Auction created", auction);
    }

    private Response handleDeleteAuction(Object payload) {
        AuctionIdPayload delete = (AuctionIdPayload) payload;
        Auction auction = auctionManager.getAuction(delete.auctionId());
        if (auction == null) {
            return new Response(false, "Auction not found", null);
        }
        auctionManager.removeAuction(delete.auctionId());
        return new Response(true, "Auction deleted", delete.auctionId());
    }

    private Response handlePlaceBid(Object payload) {
        if ("SELLER".equalsIgnoreCase(currentRole)) {
            return new Response(false, "Sellers cannot place bids.", null);
        }
        BidPayload bid = (BidPayload) payload;
        try {
            Auction updated = bidService.placeBid(bid.auctionId(), bid.bidderId(), bid.amount());
            return new Response(true, "Bid accepted", updated);
        } catch (InvalidBidException | AuctionClosedException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    private Response handleBidHistory(Object payload) {
        AuctionIdPayload history = (AuctionIdPayload) payload;
        Auction auction = auctionManager.getAuction(history.auctionId());
        if (auction == null) {
            return new Response(false, "Auction not found", null);
        }
        return new Response(true, "Bid history loaded", auction.getBids());
    }

    @Override
    public void onAuctionUpdated(Auction auction) {
        sendResponse(new Response(true, "AUCTION_UPDATE", auction));
    }

    private synchronized void sendResponse(Response response) {
        try {
            if (out != null) {
                out.reset();
                out.writeObject(response);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Could not send response to client: " + e.getMessage());
            closeConnections();
        }
    }

    private void closeConnections() {
        ClientManager.getInstance().removeSocketClient(this);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
