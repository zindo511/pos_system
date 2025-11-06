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
    private TextField userName;

    @FXML
    private PasswordField password;

    @FXML
    private TextField passwordText;

    @FXML
    private CheckBox showPasswordCheckBox;

    @FXML
    private Button loginButton;

    private EmployeeDAO employeeDAO = new EmployeeDAO();

    @FXML
    private void initialize() {
        // Ban đầu ẩn TextField hiển thị password
        passwordText.setVisible(false);
        passwordText.setManaged(false);

        // Bind text giữa PasswordField và TextField
        passwordText.textProperty().bindBidirectional(password.textProperty());

        // Xử lý checkbox hiển thị mật khẩu
        showPasswordCheckBox.setOnAction(event -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordText.setVisible(true);
                passwordText.setManaged(true);
                password.setVisible(false);
                password.setManaged(false);
            } else {
                passwordText.setVisible(false);
                passwordText.setManaged(false);
                password.setVisible(true);
                password.setManaged(true);
            }
        });

        // Enter key để login
        password.setOnAction(event -> handleLogin());
        passwordText.setOnAction(event -> handleLogin());

        // Bind action cho nút đăng nhập
        loginButton.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = userName.getText().trim();
        String passwordValue = password.getText().trim();

        // Validation
        if (username.isEmpty() || passwordValue.isEmpty()) {
            AlertHelper.showWarning("Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Kiểm tra đăng nhập
        Employee employee = employeeDAO.login(username, passwordValue);

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
                AlertHelper.showError("Lỗi", "Không thể mở màn hình chính!");
            }

        } else {
            AlertHelper.showWarning("Lỗi đăng nhập", "Sai tên đăng nhập hoặc mật khẩu!");
        }
    }
}