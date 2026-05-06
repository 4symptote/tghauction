package com.app.server.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DatabaseConnection {
    private static final String CONNECTION_STRING = "mongodb+srv://auction_db_user:Auction123456@cluster0.0yw8m61.mongodb.net/?appName=Cluster0";
    private static final String DATABASE_NAME = "auction_db";

    private static MongoClient mongoClient = null;

    // Sử dụng Singleton Pattern để chỉ tạo 1 kết nối dùng chung
    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            System.out.println("Đã kết nối tới MongoDB Cloud thành công!");
        }
        return mongoClient.getDatabase(DATABASE_NAME);
    }
}