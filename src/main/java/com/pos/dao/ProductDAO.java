package com.pos.dao;

import com.pos.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.*, c.name as category_name FROM products as p " +
                "LEFT JOIN categories as c on c.id = p.category_id " +
                "ORDER BY p.name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setCategoryName(rs.getString("category_name"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                product.setImagePath(rs.getString("image_path"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean addProduct(Product product) {
        String query = "INSERT INTO products(name, category_id, price, stock, image_path) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getCategoryId());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getStock());
            pstmt.setString(5, product.getImagePath());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(Product product) {
        String query = "UPDATE products SET name=?, category_id=?, price=?, stock=?, image_path=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getCategoryId());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getStock());
            pstmt.setString(5, product.getImagePath());
            pstmt.setInt(6, product.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int id) {
        String query = "DELETE FROM products WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStock(int productId, int quantity) {
        String query = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.*, c.name as category_name FROM products as p " +
                "LEFT JOIN categories as c ON p.category_id = c.id " +
                "WHERE p.name LIKE ? ORDER BY p.name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setCategoryName(rs.getString("category_name"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                product.setImagePath(rs.getString("image_path"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public Product getProductById(int productId) {
        String query = "SELECT p.*, c.name as category_name FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setCategoryName(rs.getString("category_name"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                product.setImagePath(rs.getString("image_path"));
                return product;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}