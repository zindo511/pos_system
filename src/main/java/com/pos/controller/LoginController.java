package com.pos.controller;

import com.pos.dao.EmployeeDAO;
import com.pos.model.Employee;
import com.pos.util.AlertHelper;
import com.pos.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label messageLabel;

    private EmployeeDAO employeeDAO = new EmployeeDAO();

    @FXML
    private void initialize() {
        // Enter key để login
        passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Kiểm tra đăng nhập
        Employee employee = employeeDAO.login(username, password);

        if (employee != null) {
            // Lưu thông tin user vào session
            SessionManager.getInstance().setCurrentEmployee(employee);

            // Chuyển sang màn hình chính
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
                stage.setTitle("POS System - " + employee.getFullName());
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
//                AlertHelper.showError("Lỗi", "Không thể mở màn hình chính!");
            }

        } else {
            messageLabel.setText("Sai tên đăng nhập hoặc mật khẩu!");
        }
    }
}