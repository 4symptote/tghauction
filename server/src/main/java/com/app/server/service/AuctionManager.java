package com.app.server.service;

import com.app.server.dao.MongoObjectStore;
import com.app.shared.models.auction.Auction;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class AuctionManager {

    private static AuctionManager instance;
    private static final Path DATA_FILE = resolveDataFile();

    private final Map<String, Auction> activeAuctions;
    private final MongoObjectStore mongoStore;

    private AuctionManager() {
        this.mongoStore = MongoObjectStore.getInstance();
        this.activeAuctions = new ConcurrentHashMap<>();
        loadAuctions();
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }



    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getId(), auction);
        saveAuction(auction);
    }

    public void removeAuction(String auctionId) {
        activeAuctions.remove(auctionId);
        if (mongoStore.isAvailable()) {
            mongoStore.deleteAuction(auctionId);
        } else {
            saveToDisk();
        }
    }

    public Auction getAuction(String auctionId) {
        return activeAuctions.get(auctionId);
    }

    public Map<String, Auction> getAllActiveAuctions() {
        return activeAuctions;
    }

    public List<Auction> listAuctions() {
        return new ArrayList<>(activeAuctions.values());
    }

    public synchronized void saveToDisk() {
        if (mongoStore.isAvailable()) {
            for (Auction auction : activeAuctions.values()) {
                mongoStore.saveAuction(auction);
            }
            return;
        }

        try {
            Files.createDirectories(DATA_FILE.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(DATA_FILE))) {
                out.writeObject(new ArrayList<>(activeAuctions.values()));
            }
        } catch (IOException e) {
            System.err.println("Could not save auctions: " + e.getMessage());
        }
    }

    private void loadAuctions() {
        int mongoAuctionCount = 0;
        if (mongoStore.isAvailable()) {
            for (Auction auction : mongoStore.loadAuctions()) {
                activeAuctions.put(auction.getId(), auction);
            }
            mongoAuctionCount = activeAuctions.size();
        }

        loadFromDisk();
        if (mongoStore.isAvailable()) {
            for (Auction auction : activeAuctions.values()) {
                mongoStore.saveAuction(auction);
            }
            System.out.println("MongoDB auctions ready: " + activeAuctions.size() + " total (" + mongoAuctionCount + " loaded from Atlas).");
        }
    }

    private void saveAuction(Auction auction) {
        if (mongoStore.isAvailable()) {
            mongoStore.saveAuction(auction);
        } else {
            saveToDisk();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromDisk() {
        if (!Files.exists(DATA_FILE)) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(DATA_FILE))) {
            Object data = in.readObject();
            if (data instanceof List<?>) {
                for (Object item : (List<?>) data) {
                    if (item instanceof Auction auction) {
                        activeAuctions.put(auction.getId(), auction);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load auctions: " + e.getMessage());
        }
    }

    private static Path resolveDataFile() {
        Path cwd = Path.of("").toAbsolutePath().getFileName();
        if (cwd != null && "server".equals(cwd.toString())) {
            return Path.of("data", "auctions.ser");
        }
        return Path.of("server", "data", "auctions.ser");
    }
}
