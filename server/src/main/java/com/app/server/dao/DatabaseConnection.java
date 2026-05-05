package com.app.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Thông tin kết nối MySQL (Đổi lại cho khớp với database của bạn)
    private static final String URL = "jdbc:mysql://localhost:3306/auction_db";
    private static final String USER = "root";       // Username MySQL của bạn
    private static final String PASSWORD = "";       // Mật khẩu MySQL của bạn (XAMPP thường để trống)

    private static Connection connection = null;

    // Sử dụng Singleton Pattern để chỉ tạo 1 kết nối duy nhất
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Tải driver MySQL (Tùy chọn ở các bản Java mới, nhưng nên giữ cho chắc ăn)
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Tạo kết nối
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Kết nối Database thành công!");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("[DB] Lỗi kết nối Database: " + e.getMessage());
        }
        return connection;
    }
}
