package com.app.server.service;

import com.app.server.dao.MongoObjectStore;
import com.app.shared.models.user.Admin;
import com.app.shared.models.user.Bidder;
import com.app.shared.models.user.Seller;
import com.app.shared.models.user.User;
import com.app.shared.network.payload.LoginPayload;
import com.app.shared.network.payload.RegisterPayload;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserManager {
    private static UserManager instance;
    private static final Path DATA_FILE = resolveDataFile();

    private final Map<String, User> users = new HashMap<>();
    private final MongoObjectStore mongoStore;

    private UserManager() {
        this.mongoStore = MongoObjectStore.getInstance();
        loadUsers();
        seedAdmin();
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public synchronized User login(LoginPayload payload) {
        String username = normalizeUsername(payload.username());
        if (username.isBlank() || isBlank(payload.password())) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        User user = users.get(username);
        if (user == null || !hashPassword(payload.password()).equals(user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return user;
    }

    public synchronized User register(RegisterPayload payload) {
        String username = normalizeUsername(payload.username());
        String role = normalizeRole(payload.role());
        validateRegistration(username, payload.password(), payload.email(), role);

        if (users.containsKey(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        User user = createUser(username, hashPassword(payload.password()), payload.email().trim(), role);
        users.put(username, user);
        saveUser(user);
        return user;
    }

    private void validateRegistration(String username, String password, String email, String role) {
        if (username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters.");
        }
        if (isBlank(password) || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters.");
        }
        if (isBlank(email) || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email is required.");
        }
        if (!role.equals("BIDDER") && !role.equals("SELLER")) {
            throw new IllegalArgumentException("Role must be Bidder or Seller.");
        }
    }

    private User createUser(String username, String passwordHash, String email, String role) {
        return switch (role) {
            case "SELLER" -> new Seller(username, passwordHash, email);
            case "ADMIN" -> new Admin(username, passwordHash, email);
            default -> new Bidder(username, passwordHash, email, 1_000_000.0);
        };
    }

    private void seedAdmin() {
        if (!users.containsKey("admin")) {
            User admin = new Admin("admin", hashPassword("admin"), "admin@tghauction.local");
            users.put("admin", admin);
            saveUser(admin);
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "BIDDER";
        }
        return role.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : encodedHash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    private synchronized void saveToDisk() {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(DATA_FILE))) {
                out.writeObject(users);
            }
        } catch (IOException e) {
            System.err.println("Could not save users: " + e.getMessage());
        }
    }

    private void loadUsers() {
        int mongoUserCount = 0;
        if (mongoStore.isAvailable()) {
            users.putAll(mongoStore.loadUsers());
            mongoUserCount = users.size();
        }

        loadFromDisk();
        if (mongoStore.isAvailable()) {
            for (User user : users.values()) {
                mongoStore.saveUser(user);
            }
            System.out.println("MongoDB users ready: " + users.size() + " total (" + mongoUserCount + " loaded from Atlas).");
        }
    }

    private void saveUser(User user) {
        if (mongoStore.isAvailable()) {
            mongoStore.saveUser(user);
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
            if (data instanceof Map<?, ?> loadedUsers) {
                for (Map.Entry<?, ?> entry : loadedUsers.entrySet()) {
                    if (entry.getKey() instanceof String username && entry.getValue() instanceof User user) {
                        users.put(username, user);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load users: " + e.getMessage());
        }
    }

    private static Path resolveDataFile() {
        Path cwd = Path.of("").toAbsolutePath().getFileName();
        if (cwd != null && "server".equals(cwd.toString())) {
            return Path.of("data", "users.ser");
        }
        return Path.of("server", "data", "users.ser");
    }
}
