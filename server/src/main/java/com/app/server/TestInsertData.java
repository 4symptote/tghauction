package com.app.server;

import com.app.server.dao.DatabaseConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Arrays;

public class TestInsertData {
    public static void main(String[] args) {
        // kết nối và lấy database
        MongoDatabase database = DatabaseConnection.getDatabase();

        // 2. Chọn collection
        MongoCollection<Document> itemCollection = database.getCollection("items");

        // thêm 1 bản gh
        Document item1 = new Document("itemName", "Laptop Dell XPS 15")
                .append("startingPrice", 1500.0)
                .append("currentPrice", 1500.0)
                .append("status", "ACTIVE")
                .append("seller", "datdoz");

        itemCollection.insertOne(item1);
        System.out.println("-> Đã thêm 1 sản phẩm: " + item1.getString("itemName"));

        // thêm nhiều bản ghi 1 lúc
        Document item2 = new Document("itemName", "iPhone 15 Pro Max")
                .append("startingPrice", 1000.0)
                .append("status", "ACTIVE")
                .append("seller", "admin");

        Document item3 = new Document("itemName", "Đồng hồ Rolex Submariner")
                .append("startingPrice", 8000.0)
                .append("status", "PENDING")
                .append("seller", "datdoz");

        itemCollection.insertMany(Arrays.asList(item2, item3));
        System.out.println("-> Đã thêm 2 sản phẩm cùng lúc thành công!");
    }
}