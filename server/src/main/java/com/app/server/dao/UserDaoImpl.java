package com.app.server.dao;

import com.app.shared.models.user.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDaoImpl implements UserDao {

    @Override
    public User getUserByUsernameAndPassword(String username, String password) {
        // Dùng PreparedStatement để chống SQL Injection
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            // Nếu tìm thấy dòng dữ liệu thoả mãn
            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("email")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Không tìm thấy hoặc sai pass
    }

    @Override
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getEmail());

            int rowsAffected = stmt.executeUpdate(); // Thực thi câu lệnh Insert
            return rowsAffected > 0; // Trả về true nếu thành công

        } catch (SQLException e) {
            System.err.println("Lỗi tạo user: Trùng lặp username?");
            e.printStackTrace();
        }
        return false;
    }
}