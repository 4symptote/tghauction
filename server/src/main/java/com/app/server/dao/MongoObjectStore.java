package com.app.server.dao;

import com.app.shared.models.auction.Auction;
import com.app.shared.models.auction.BidTransaction;
import com.app.shared.models.item.Art;
import com.app.shared.models.item.Electronics;
import com.app.shared.models.item.Item;
import com.app.shared.models.item.Vehicle;
import com.app.shared.models.user.Admin;
import com.app.shared.models.user.Bidder;
import com.app.shared.models.user.Seller;
import com.app.shared.models.user.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

public class MongoObjectStore {
    private static MongoObjectStore instance;

    private static final String USERS_COLLECTION = "users";
    private static final String AUCTIONS_COLLECTION = "auctions";

    private final boolean available;

    private MongoObjectStore() {
        this.available = DatabaseConnection.isAvailable();
    }

    public static synchronized MongoObjectStore getInstance() {
        if (instance == null) {
            instance = new MongoObjectStore();
        }
        return instance;
    }

    public boolean isAvailable() {
        return available;
    }

    public Map<String, User> loadUsers() {
        Map<String, User> users = new HashMap<>();
        if (!available) {
            return users;
        }

        for (Document document : getCollection(USERS_COLLECTION).find()) {
            User user = documentToUser(document);
            if (user != null) {
                users.put(user.getUsername().toLowerCase(Locale.ROOT), user);
            }
        }
        return users;
    }

    public void saveUser(User user) {
        if (!available || user == null) {
            return;
        }

        String username = user.getUsername().toLowerCase(Locale.ROOT);
        getCollection(USERS_COLLECTION).replaceOne(
                eq("_id", username),
                userToDocument(user),
                new ReplaceOptions().upsert(true)
        );
    }

    public List<Auction> loadAuctions() {
        List<Auction> auctions = new ArrayList<>();
        if (!available) {
            return auctions;
        }

        for (Document document : getCollection(AUCTIONS_COLLECTION).find()) {
            Auction auction = documentToAuction(document);
            if (auction != null) {
                auctions.add(auction);
            }
        }
        return auctions;
    }

    public void saveAuction(Auction auction) {
        if (!available || auction == null) {
            return;
        }

        getCollection(AUCTIONS_COLLECTION).replaceOne(
                eq("_id", auction.getId()),
                auctionToDocument(auction),
                new ReplaceOptions().upsert(true)
        );
    }

    public void deleteAuction(String auctionId) {
        if (!available || auctionId == null) {
            return;
        }

        getCollection(AUCTIONS_COLLECTION).deleteOne(eq("_id", auctionId));
    }

    private Document userToDocument(User user) {
        Document document = new Document("_id", user.getUsername().toLowerCase(Locale.ROOT))
                .append("username", user.getUsername())
                .append("passwordHash", user.getPassword())
                .append("email", user.getEmail())
                .append("role", user.getRole());

        if (user instanceof Bidder bidder) {
            document.append("balance", bidder.getBalance());
        } else if (user instanceof Seller seller) {
            document.append("totalRevenue", seller.getTotalRevenue());
        }

        return document;
    }

    private User documentToUser(Document document) {
        String username = document.getString("username");
        String passwordHash = document.getString("passwordHash");
        String email = document.getString("email");
        String role = document.getString("role");
        if (username == null || passwordHash == null || email == null || role == null) {
            return null;
        }

        return switch (role.toUpperCase(Locale.ROOT)) {
            case "SELLER" -> new Seller(username, passwordHash, email);
            case "ADMIN" -> new Admin(username, passwordHash, email);
            default -> new Bidder(username, passwordHash, email, document.getDouble("balance") == null ? 0.0 : document.getDouble("balance"));
        };
    }

    private Document auctionToDocument(Auction auction) {
        List<Document> bidDocuments = new ArrayList<>();
        for (BidTransaction bid : auction.getBids()) {
            bidDocuments.add(bidToDocument(bid));
        }

        return new Document("_id", auction.getId())
                .append("item", itemToDocument(auction.getItem()))
                .append("startTime", auction.getStartTime())
                .append("endTime", auction.getEndTime())
                .append("currentPrice", auction.getCurrentPrice())
                .append("status", auction.getStatus().name())
                .append("highestBidderId", auction.getHighestBidderId())
                .append("bids", bidDocuments);
    }

    private Auction documentToAuction(Document document) {
        Document itemDocument = document.get("item", Document.class);
        Item item = documentToItem(itemDocument);
        if (item == null) {
            return null;
        }

        Auction auction = new Auction(
                item,
                document.getLong("startTime"),
                document.getLong("endTime")
        );
        auction.setId(document.getString("_id"));
        auction.setCurrentPrice(getDouble(document, "currentPrice", item.getStartingPrice()));
        auction.setStatus(Auction.Status.valueOf(document.getString("status")));
        auction.setHighestBidderId(document.getString("highestBidderId"));

        List<Document> bids = document.getList("bids", Document.class, List.of());
        for (Document bidDocument : bids) {
            BidTransaction bid = documentToBid(bidDocument);
            if (bid != null) {
                auction.getBids().add(bid);
            }
        }
        item.setCurrentHighestBid(auction.getCurrentPrice());
        return auction;
    }

    private Document itemToDocument(Item item) {
        Document document = new Document("id", item.getId())
                .append("type", item.getClass().getSimpleName())
                .append("name", item.getName())
                .append("description", item.getDescription())
                .append("startingPrice", item.getStartingPrice())
                .append("currentHighestBid", item.getCurrentHighestBid())
                .append("sellerId", item.getSellerId());

        if (item instanceof Art art) {
            document.append("artist", art.getArtist());
        } else if (item instanceof Electronics electronics) {
            document.append("brand", electronics.getBrand());
        } else if (item instanceof Vehicle vehicle) {
            document.append("brand", vehicle.getBrand());
        }

        return document;
    }

    private Item documentToItem(Document document) {
        if (document == null) {
            return null;
        }

        String type = document.getString("type");
        String name = document.getString("name");
        String description = document.getString("description");
        double startingPrice = getDouble(document, "startingPrice", 0.0);
        String sellerId = document.getString("sellerId");

        Item item = switch (type == null ? "" : type) {
            case "Art" -> new Art(name, description, startingPrice, sellerId).setArtist(document.getString("artist"));
            case "Vehicle" -> new Vehicle(name, description, startingPrice, sellerId).setBrand(document.getString("brand"));
            default -> new Electronics(name, description, startingPrice, sellerId).setBrand(document.getString("brand"));
        };
        item.setId(document.getString("id"));
        item.setCurrentHighestBid(getDouble(document, "currentHighestBid", startingPrice));
        return item;
    }

    private Document bidToDocument(BidTransaction bid) {
        return new Document("id", bid.getId())
                .append("auctionId", bid.getAuctionId())
                .append("bidderId", bid.getBidderId())
                .append("amount", bid.getAmount())
                .append("timestamp", bid.getTimestamp());
    }

    private BidTransaction documentToBid(Document document) {
        if (document == null) {
            return null;
        }

        BidTransaction bid = new BidTransaction(
                document.getString("auctionId"),
                document.getString("bidderId"),
                getDouble(document, "amount", 0.0)
        );
        bid.setId(document.getString("id"));
        Long timestamp = document.getLong("timestamp");
        if (timestamp != null) {
            bid.setTimestamp(timestamp);
        }
        return bid;
    }

    private double getDouble(Document document, String key, double fallback) {
        Object value = document.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
    }

    private MongoCollection<Document> getCollection(String collectionName) {
        return DatabaseConnection.getDatabase().getCollection(collectionName);
    }
}
