package com.pos.dao;

import com.pos.model.Order;
import com.pos.model.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.pos.dao.DatabaseConnection.getConnection;

public class OrderDAO {
    public int createOrder(Order order, List<OrderItem> items) {
        Connection conn = null;
        PreparedStatement pstmtOrder = null;
        PreparedStatement pstmtItem = null;
        ResultSet rs = null;
        int orderId = -1;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Thêm order
            String orderQuery = "INSERT INTO orders (employee_id, total_amount, payment_method, customer_paid, change_amount) VALUES (?, ?, ?, ?, ?)";
            pstmtOrder = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
            pstmtOrder.setInt(1, order.getEmployeeId());
            pstmtOrder.setDouble(2, order.getTotalAmount());
            pstmtOrder.setString(3, order.getPaymentMethod());
            pstmtOrder.setDouble(4, order.getCustomerPaid());
            pstmtOrder.setDouble(5, order.getChangeAmount());
            pstmtOrder.executeUpdate();

            // Lấy order ID vừa tạo
            rs = pstmtOrder.getGeneratedKeys();
            if (rs.next()) {
                orderId = rs.getInt(1);
            }

            // 2. Thêm order items
            String itemQuery = "INSERT INTO order_items (order_id, product_id, product_name, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
            pstmtItem = conn.prepareStatement(itemQuery);

            for (OrderItem item : items) {
                pstmtItem.setInt(1, orderId);
                pstmtItem.setInt(2, item.getProductId());
                pstmtItem.setString(3, item.getProductName());
                pstmtItem.setInt(4, item.getQuantity());
                pstmtItem.setDouble(5, item.getPrice());
                pstmtItem.setDouble(6, item.getSubtotal());
                pstmtItem.executeUpdate();

                // 3. Cập nhật stock
                ProductDAO productDAO = new ProductDAO();
                productDAO.updateStock(item.getProductId(), item.getQuantity());
            }

            conn.commit(); // Commit transaction
            return orderId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback nếu có lỗi
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmtOrder != null) pstmtOrder.close();
                if (pstmtItem != null) pstmtItem.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT o.*, e.full_name as employee_name FROM orders o " +
                "LEFT JOIN employees e ON o.employee_id = e.id " +
                "ORDER BY o.created_at DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setEmployeeId(rs.getInt("employee_id"));
                order.setEmployeeName(rs.getString("employee_name"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setPaymentMethod(rs.getString("payment_method"));
                order.setCustomerPaid(rs.getDouble("customer_paid"));
                order.setChangeAmount(rs.getDouble("change_amount"));
                order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String query = "SELECT * FROM order_items WHERE order_id=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getDouble("price"));
                item.setSubtotal(rs.getDouble("subtotal"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public double getTotalSalesToday() {
        String query = "SELECT COALESCE(SUM(total_amount), 0) as total FROM orders WHERE DATE(created_at) = CURDATE()";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalOrdersToday() {
        String query = "SELECT COUNT(*) as total FROM orders WHERE DATE(created_at) = CURDATE()";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Order> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Order> orders = new ArrayList<>();

        String sql = "SELECT * FROM orders WHERE DATE(created_at) BETWEEN ? AND ? ORDER BY created_at";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setEmployeeId(rs.getInt("employee_id"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setPaymentMethod(rs.getString("payment_method"));
                order.setCustomerPaid(rs.getDouble("customer_paid"));
                order.setChangeAmount(rs.getDouble("change_amount"));
                order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                // 🔹 Load danh sách OrderItem
                order.setItems(getOrderItemsByOrderId(order.getId()));

                orders.add(order);
            }
        }

        return orders;
    }

    private ObservableList<OrderItem> getOrderItemsByOrderId(int orderId) throws SQLException {
        ObservableList<OrderItem> items = FXCollections.observableArrayList();

        String sql = "SELECT oi.*, p.name AS product_name " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(orderId);
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getDouble("price"));
                item.setSubtotal(rs.getDouble("subtotal"));

                items.add(item);
            }
        }

        return items;
    }

}