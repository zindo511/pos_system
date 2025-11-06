package com.pos.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    //
    private static final String URL =
            "jdbc:mysql://localhost:3306/pos_system?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Ho_Chi_Minh";

    private static final String USER = "root";
    private static final String PASSWORD = "Huy160105@";

    // Singleton connection (có thể cải thiện bằng connection pooling)
    private static Connection connection;

    private DatabaseConnection() {
        // Private constructor để prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        try {
            // Load driver một lần duy nhất
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Tạo connection mới mỗi lần (thread-safe hơn)
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }

    // Thêm method đóng connection an toàn
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Test connection
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối database:");
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("Đang kiểm tra kết nối database...");
        if (testConnection()) {
            System.out.println("Kết nối database thành công!");
        } else {
            System.out.println("Kết nối database thất bại!");
        }
    }
}