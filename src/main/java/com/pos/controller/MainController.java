package com.pos.controller;

import com.pos.util.AlertHelper;
import com.pos.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private Label userLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnSales;

    @FXML
    private Button btnProducts;

    @FXML
    private Button btnOrders;

    @FXML
    private Button btnEmployees;

    @FXML
    private Button btnReports;

    @FXML
    private void initialize() {
        // Hiển thị thông tin user
        if (SessionManager.getInstance().getCurrentEmployee() != null) {
            String fullName = SessionManager.getInstance().getCurrentEmployee().getFullName();
            String role = SessionManager.getInstance().getCurrentEmployee().getRole();
            userLabel.setText(fullName + " (" + role + ")");
        }

        // Ẩn menu nhân viên nếu không phải admin
        if (!SessionManager.getInstance().isAdmin()) {
            if (btnEmployees != null) {
                btnEmployees.setVisible(false);
                btnEmployees.setManaged(false);
            }
        }

        // Bind actions cho các button
        if (btnSales != null) {
            btnSales.setOnAction(event -> showSales());
        }
        if (btnProducts != null) {
            btnProducts.setOnAction(event -> showProducts());
        }
        if (btnOrders != null) {
            btnOrders.setOnAction(event -> showOrders());
        }
        if (btnEmployees != null) {
            btnEmployees.setOnAction(event -> showEmployees());
        }
        if (btnReports != null) {
            btnReports.setOnAction(event -> showReports());
        }

        // Load màn hình bán hàng mặc định
        showSales();
    }

    @FXML
    private void showSales() {
        loadView("/fxml/sales.fxml");
        if (btnSales != null) {
            highlightButton(btnSales);
        }
    }

    @FXML
    private void showProducts() {
        loadView("/fxml/products.fxml");
        if (btnProducts != null) {
            highlightButton(btnProducts);
        }
    }

    @FXML
    private void showOrders() {
        loadView("/fxml/orders.fxml");
        if (btnOrders != null) {
            highlightButton(btnOrders);
        }
    }

    @FXML
    private void showEmployees() {
        if (SessionManager.getInstance().isAdmin()) {
            loadView("/fxml/employees.fxml");
            if (btnEmployees != null) {
                highlightButton(btnEmployees);
            }
        } else {
            AlertHelper.showWarning("Cảnh báo", "Bạn không có quyền truy cập!");
        }
    }

    @FXML
    private void showReports() {
        loadView("/fxml/reports.fxml");
        if (btnReports != null) {
            highlightButton(btnReports);
        }
    }

    @FXML
    private void handleLogout() {
        if (AlertHelper.showConfirmation("Đăng xuất", "Bạn có chắc muốn đăng xuất?")) {
            SessionManager.getInstance().clearSession();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) userLabel.getScene().getWindow();
                stage.setScene(new Scene(root, 1250, 600));
                stage.centerOnScreen();
                stage.setTitle("POS System - Login");

            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showError("Lỗi", "Không thể quay lại màn hình đăng nhập!");
            }
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Lỗi", "Không thể tải giao diện: " + fxmlPath);
        }
    }

    private void highlightButton(Button activeButton) {
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 15; -fx-alignment: center-left; -fx-cursor: hand;";
        String activeStyle = "-fx-background-color: #37474F; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 15; -fx-alignment: center-left; -fx-cursor: hand;";

        // Reset tất cả buttons
        if (btnSales != null) btnSales.setStyle(defaultStyle);
        if (btnProducts != null) btnProducts.setStyle(defaultStyle);
        if (btnOrders != null) btnOrders.setStyle(defaultStyle);
        if (btnReports != null) btnReports.setStyle(defaultStyle);

        if (btnEmployees != null && btnEmployees.isVisible()) {
            btnEmployees.setStyle(defaultStyle);
        }

        // Highlight button đang active
        if (activeButton != null) {
            activeButton.setStyle(activeStyle);
        }
    }
}