package com.app.server;

import com.app.server.network.AuctionServer;
import com.app.server.service.AuctionManager;
import com.app.shared.models.auction.Auction;
import com.app.shared.models.item.Creator.ArtCreator;
import com.app.shared.models.item.Creator.ElectronicCreator;
import com.app.shared.models.item.Creator.VehicleCreator;

public class ServerMain {
    public static void main(String[] args) {
        seedDemoAuctions();
        int port = 8080;
        AuctionServer server = new AuctionServer(port);
        server.start();
    }

    private static void seedDemoAuctions() {
        AuctionManager auctionManager = AuctionManager.getInstance();
        if (!auctionManager.listAuctions().isEmpty()) {
            return;
        }

        long duration = 30 * 60 * 1000L;
        auctionManager.addAuction(new Auction(
                new ElectronicCreator().createItem("Gaming Laptop", "RTX laptop, 16GB RAM", 750.0, "demo-seller"),
                duration
        ));
        auctionManager.addAuction(new Auction(
                new ArtCreator().createItem("Landscape Painting", "Framed acrylic painting", 120.0, "demo-seller"),
                duration
        ));
        auctionManager.addAuction(new Auction(
                new VehicleCreator().createItem("Vintage Scooter", "Restored city scooter", 900.0, "demo-seller"),
                duration
        ));
    }
}
