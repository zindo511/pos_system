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


    // Tạo đơn hàng nhanh, an toàn, chỉ 1 transaction
    public int createOrder(Order order, List<OrderItem> items) {
        String orderQuery = "INSERT INTO orders (employee_id, total_amount, payment_method, customer_paid, change_amount) VALUES (?, ?, ?, ?, ?)";
        String itemQuery = "INSERT INTO order_items (order_id, product_id, product_name, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        String updateStockQuery = "UPDATE products SET stock = stock - ? WHERE id = ?";


        try (Connection conn = getConnection();
             PreparedStatement pstmtOrder = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmtItem = conn.prepareStatement(itemQuery);
             PreparedStatement pstmtStock = conn.prepareStatement(updateStockQuery)) {


            conn.setAutoCommit(false);


            // 1️⃣ Thêm đơn hàng
            pstmtOrder.setInt(1, order.getEmployeeId());
            pstmtOrder.setDouble(2, order.getTotalAmount());
            pstmtOrder.setString(3, order.getPaymentMethod());
            pstmtOrder.setDouble(4, order.getCustomerPaid());
            pstmtOrder.setDouble(5, order.getChangeAmount());
            pstmtOrder.executeUpdate();


            // 2️⃣ Lấy order ID vừa tạo
            int orderId = -1;
            try (ResultSet rs = pstmtOrder.getGeneratedKeys()) {
                if (rs.next()) {
                    orderId = rs.getInt(1);
                }
            }


            // 3️⃣ Gộp thêm item + cập nhật stock vào batch
            for (OrderItem item : items) {
                // Insert order_items
                pstmtItem.setInt(1, orderId);
                pstmtItem.setInt(2, item.getProductId());
                pstmtItem.setString(3, item.getProductName());
                pstmtItem.setInt(4, item.getQuantity());
                pstmtItem.setDouble(5, item.getPrice());
                pstmtItem.setDouble(6, item.getSubtotal());
                pstmtItem.addBatch();


                // Update tồn kho
                pstmtStock.setInt(1, item.getQuantity());
                pstmtStock.setInt(2, item.getProductId());
                pstmtStock.addBatch();
            }


            // 4️⃣ Thực thi batch
            pstmtItem.executeBatch();
            pstmtStock.executeBatch();


            // 5️⃣ Hoàn tất
            conn.commit();
            return orderId;


        } catch (SQLException e) {
            e.printStackTrace();
            // rollback nếu có lỗi
            try (Connection conn = getConnection()) {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return -1;
        }
    }


    // Lấy tất cả đơn hàng theo tên nhân vieên
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
            if (rs.next()) return rs.getDouble("total");
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
            if (rs.next()) return rs.getInt("total");
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
                order.setItems(getOrderItemsByOrderId(order.getId()));
                orders.add(order);
            }
        }


        return orders;
    }


    public ObservableList<OrderItem> getOrderItemsByOrderId(int orderId) throws SQLException {
        ObservableList<OrderItem> items = FXCollections.observableArrayList();
        String sql = "SELECT oi.*, p.name AS product_name " +
                "FROM order_items oi JOIN products p ON oi.product_id = p.id WHERE oi.order_id = ?";


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


    public List<Order> searchOrders(String keyword) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT o.*, e.full_name as employee_name FROM orders o " +
                "LEFT JOIN employees e ON o.employee_id = e.id " +
                "WHERE CAST(o.id AS CHAR) LIKE ? OR e.full_name LIKE ? " +
                "OR o.payment_method LIKE ? OR CAST(o.total_amount AS CHAR) LIKE ? " +
                "ORDER BY o.created_at DESC";


        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            ResultSet rs = pstmt.executeQuery();


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
}

