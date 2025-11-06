package com.pos.dao;

import com.pos.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    /**
     * Lấy danh sách tất cả các danh mục (Category) từ cơ sở dữ liệu.
     * Mỗi danh mục được nhóm theo tên (name) và chỉ lấy bản ghi có id nhỏ nhất.
     *
     * @return Danh sách các đối tượng Category.
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT MIN(id) AS id, name, MIN(description) AS description FROM categories GROUP BY name ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    /**
     * Thêm một danh mục (Category) mới vào cơ sở dữ liệu.
     *
     * @param category Đối tượng Category chứa thông tin tên và mô tả danh mục cần thêm.
     * @return true nếu thêm thành công, false nếu có lỗi xảy ra.
     */
    public boolean addCategory(Category category) {
        String query = "INSERT INTO categories(name, description) VALUES(?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // In lỗi chi tiết ra console nếu xảy ra lỗi SQL
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCategory(Category category) {
        String query = "UPDATE categories SET name=?, description=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa một danh mục (Category) khỏi cơ sở dữ liệu dựa theo id.
     *
     * @param id Mã định danh (id) của danh mục cần xóa.
     * @return true nếu xóa thành công (có ít nhất một dòng bị ảnh hưởng), false nếu có lỗi xảy ra hoặc không tìm thấy id.
     */
    public boolean deleteCategory(int id) {
        String query = "DELETE FROM categories WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}