package com.pos.dao;

import com.pos.model.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    /**
     * Lấy danh sách tất cả nhân viên (Employee) từ cơ sở dữ liệu,
     * được sắp xếp theo tên đầy đủ (full_name) theo thứ tự tăng dần.
     *
     * @return Danh sách các đối tượng Employee, hoặc danh sách rỗng nếu không có dữ liệu.
     */
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees ORDER BY full_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Employee employee = new Employee();
                employee.setId(rs.getInt("id"));
                employee.setUsername(rs.getString("username"));
                employee.setPassword(rs.getString("password"));
                employee.setFullName(rs.getString("full_name"));
                employee.setRole(rs.getString("role"));
                employee.setPhone(rs.getString("phone"));
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    /**
     * Thêm một nhân viên (Employee) mới vào cơ sở dữ liệu.
     * Sau khi thêm, hàm sẽ tự động cập nhật ID được sinh ra (auto-increment) cho đối tượng Employee.
     *
     * @param employee Đối tượng Employee chứa thông tin cần thêm (username, password, full_name, role, phone).
     * @return true nếu thêm thành công, false nếu có lỗi xảy ra hoặc không có dòng nào được thêm.
     */
    public boolean addEmployee(Employee employee) {
        String query = "INSERT INTO employees(username, password, full_name, role, phone) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) { // thêm RETURN_GENERATED_KEYS

            pstmt.setString(1, employee.getUsername());
            pstmt.setString(2, employee.getPassword());
            pstmt.setString(3, employee.getFullName());
            pstmt.setString(4, employee.getRole());
            pstmt.setString(5, employee.getPhone());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) return false;

            // Lấy ID vừa insert
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    employee.setId(rs.getInt(1)); // set lại ID
                }
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật thông tin của một nhân viên (Employee) trong cơ sở dữ liệu dựa theo id.
     * Chỉ cập nhật các trường: họ tên (full_name), vai trò (role) và số điện thoại (phone).
     *
     * @param employee Đối tượng Employee chứa thông tin đã chỉnh sửa và id của nhân viên cần cập nhật.
     * @return true nếu cập nhật thành công (có ít nhất một bản ghi bị ảnh hưởng),
     * false nếu không tìm thấy id hoặc xảy ra lỗi SQL.
     */
    public boolean updateEmployee(Employee employee) {
        String query = "UPDATE employees SET full_name = ?, role = ?, phone = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, employee.getFullName());
            pstmt.setString(2, employee.getRole());
            pstmt.setString(3, employee.getPhone());
            pstmt.setInt(4, employee.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa một nhân viên (Employee) khỏi cơ sở dữ liệu dựa theo id.
    public boolean deleteEmployee(int id) {
        String query = "DELETE FROM employees WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra thông tin đăng nhập của nhân viên dựa vào tên đăng nhập (username) và mật khẩu (password).
     * Nếu hợp lệ, trả về đối tượng Employee tương ứng; ngược lại trả về null.
     *
     * @param username Tên đăng nhập của nhân viên.
     * @param password Mật khẩu của nhân viên.
     * @return Đối tượng Employee nếu đăng nhập thành công, hoặc null nếu sai thông tin hoặc xảy ra lỗi SQL.
     */
    public Employee login(String username, String password) {
        String query = "SELECT * FROM employees WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Employee employee = new Employee();
                employee.setId(rs.getInt("id"));
                employee.setUsername(rs.getString("username"));
                employee.setFullName(rs.getString("full_name"));
                employee.setRole(rs.getString("role"));
                employee.setPhone(rs.getString("phone"));
                return employee;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Tìm kiếm nhân viên theo tên, username hoặc số điện thoại
    public List<Employee> searchEmployees(String keyword) {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees WHERE full_name LIKE ? OR phone LIKE ? OR username LIKE ? ORDER BY full_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Employee employee = new Employee();
                employee.setId(rs.getInt("id"));
                employee.setUsername(rs.getString("username"));
                employee.setPassword(rs.getString("password"));
                employee.setFullName(rs.getString("full_name"));
                employee.setRole(rs.getString("role"));
                employee.setPhone(rs.getString("phone"));
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }
}