package com.app.server.dao;

import com.app.shared.models.auction.Auction;
import com.app.shared.models.auction.BidTransaction;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.ArrayList;
import static com.mongodb.client.model.Filters.eq;

public class AuctionDaoImpl implements AuctionDao {
    private final MongoCollection<Document> auctionCollection;

    public AuctionDaoImpl() {
        MongoDatabase database = DatabaseConnection.getDatabase();
        // Sẽ lưu vào collection có tên là "auctions"
        this.auctionCollection = database.getCollection("auctions");
    }

    @Override
    public void createAuction(Auction auction) {
        Document doc = new Document("_id", auction.getId())
                .append("startTime", auction.getStartTime())
                .append("endTime", auction.getEndTime())
                .append("currentPrice", auction.getCurrentPrice())
                .append("status", auction.getStatus().name())
                .append("highestBidderId", auction.getHighestBidderId());

        // Lấy nhanh các thông tin của món hàng đó
        if (auction.getItem() != null) {
            Document itemDoc = new Document("id", auction.getItem().getId())
                    .append("name", auction.getItem().getName())
                    .append("startingPrice", auction.getItem().getStartingPrice());
            doc.append("item", itemDoc);
        }

        // Tạo sẵn mảng rỗng để sau này nhét các lệnh bid của user vào
        doc.append("bids", new ArrayList<Document>());

        auctionCollection.insertOne(doc);
    }

    @Override
    public void addBid(String auctionId, BidTransaction bid) {
        Document bidDoc = new Document("bidId", bid.getId())
                .append("bidderId", bid.getBidderId())
                .append("amount", bid.getAmount())
                .append("timestamp", bid.getTimestamp());

        //Thực hiện update 3 thứ cùng 1 lúc

        auctionCollection.updateOne(
                eq("_id", auctionId),
                Updates.combine(
                        Updates.push("bids", bidDoc),
                        Updates.set("currentPrice", bid.getAmount()),
                        Updates.set("highestBidderId", bid.getBidderId())
                )
        );
    }

    @Override
    public void updateAuctionStatus(String auctionId, Auction.Status status) {
        auctionCollection.updateOne(eq("_id", auctionId), Updates.set("status", status.name()));
    }

    @Override
    public void updateAuctionEndTime(String auctionId, long newEndTime) {
        auctionCollection.updateOne(eq("_id", auctionId), Updates.set("endTime", newEndTime));
    }
}