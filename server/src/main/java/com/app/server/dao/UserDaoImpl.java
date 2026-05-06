package com.app.server.dao;

import com.app.server.dao.DatabaseConnection;
import com.app.shared.models.user.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class UserDaoImpl implements UserDao {
    private MongoCollection<Document> userCollection;

    public UserDaoImpl() {
        // Lấy collection "users" (giống như bảng users trong MySQL)
        MongoDatabase database = DatabaseConnection.getDatabase();
        this.userCollection = database.getCollection("users");
    }

    @Override
    public User getUserByUsernameAndPassword(String username, String password) {
        // Tìm 1 document khớp username và password
        Document doc = userCollection.find(and(eq("username", username), eq("password", password))).first();

        if (doc != null) {
            // Parse dữ liệu từ Document sang Java Object
            return new User(
                    doc.getString("username"),
                    doc.getString("password"),
                    doc.getString("email"),
                    doc.getString("role")
            );
        }
        return null;
    }

    @Override
    public boolean registerUser(User user) {
        // Kiểm tra xem username đã tồn tại chưa
        Document existingUser = userCollection.find(eq("username", user.getUsername())).first();
        if (existingUser != null) {
            System.out.println("Tên đăng nhập đã tồn tại!");
            return false;
        }

        // Tạo Document mới để lưu vào DB
        Document newUserDoc = new Document("username", user.getUsername())
                .append("password", user.getPassword())
                .append("email", user.getEmail())
                .append("role", user.getRole())
                .append("balance", 0.0);

        // Insert vào MongoDB
        userCollection.insertOne(newUserDoc);
        return true;
    }
}