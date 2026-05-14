package com.app.server.dao;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoSecurityException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DatabaseConnection {
    private static final String DEFAULT_CONNECTION_STRING = "mongodb+srv://4symptote:tghauctionrm@tghauction.bvst4bh.mongodb.net/?appName=tGhauction";
    private static final String DEFAULT_DATABASE_NAME = "auction_db";
    private static final Map<String, String> ENV_FILE_VALUES = loadEnvFile();

    private static MongoClient mongoClient = null;
    private static Boolean available = null;

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            ConnectionString connectionString = new ConnectionString(getConnectionString());
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToClusterSettings(builder -> builder.serverSelectionTimeout(15, TimeUnit.SECONDS))
                    .build();
            mongoClient = MongoClients.create(settings);
        }
        return mongoClient.getDatabase(getDatabaseName());
    }

    public static synchronized boolean isAvailable() {
        if (available != null) {
            return available;
        }

        try {
            getDatabase().runCommand(new Document("ping", 1));
            available = true;
            System.out.println("Connected to MongoDB database: " + getDatabaseName());
        } catch (MongoSecurityException e) {
            available = false;
            System.err.println("MongoDB authentication failed. Check the Database Access user, password, and connection string in .env.");
            System.err.println("Atlas was reachable, but rejected the supplied credentials.");
        } catch (Exception e) {
            available = false;
            System.err.println("MongoDB unavailable, using local file fallback: " + e.getMessage());
        }
        return available;
    }

    private static String getConnectionString() {
        return DEFAULT_CONNECTION_STRING;
    }

    private static String getDatabaseName() {
        return DEFAULT_DATABASE_NAME;
    }

    private static String getConfigValue(String key) {
        String environmentValue = System.getenv(key);
        if (environmentValue != null && !environmentValue.isBlank()) {
            System.out.println("Using " + key + " from system environment.");
            return environmentValue;
        }
        String envFileValue = ENV_FILE_VALUES.get(key);
        if (envFileValue != null && !envFileValue.isBlank()) {
            System.out.println("Using " + key + " from .env file.");
        }
        return envFileValue;
    }

    private static Map<String, String> loadEnvFile() {
        Path envPath = findEnvFile();
        if (envPath == null) {
            return Map.of();
        }

        Map<String, String> values = new HashMap<>();
        try {
            for (String line : Files.readAllLines(envPath)) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }

                int separatorIndex = trimmedLine.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }

                String key = trimmedLine.substring(0, separatorIndex).trim();
                String value = trimmedLine.substring(separatorIndex + 1).trim();
                values.put(key, stripQuotes(value));
            }
        } catch (IOException e) {
            System.err.println("Could not read .env file at " + envPath + ": " + e.getMessage());
        }
        if (!values.isEmpty()) {
            System.out.println("Loaded MongoDB configuration from: " + envPath);
        }
        return values;
    }

    private static Path findEnvFile() {
        Path currentDirectory = Path.of("").toAbsolutePath();
        List<Path> candidates = List.of(
                currentDirectory.resolve(".env"),
                currentDirectory.resolve("server").resolve(".env"),
                currentDirectory.getParent() == null ? currentDirectory.resolve(".env") : currentDirectory.getParent().resolve(".env")
        );

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
