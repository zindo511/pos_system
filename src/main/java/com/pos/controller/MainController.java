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
        String fullName = SessionManager.getInstance().getCurrentEmployee().getFullName();
        String role = SessionManager.getInstance().getCurrentEmployee().getRole();
        userLabel.setText(fullName + " (" + role + ")");


        // Ẩn menu nhân viên nếu không phải admin
        if (!SessionManager.getInstance().isAdmin()) {
            btnEmployees.setVisible(false);
            btnEmployees.setManaged(false);
        }


        // Load màn hình bán hàng mặc định
        showSales();
    }


    @FXML
    private void showSales() {
        loadView("/fxml/sales.fxml");
        highlightButton(btnSales);
    }


    @FXML
    private void showProducts() {
        loadView("/fxml/products.fxml");
        highlightButton(btnProducts);
    }


    @FXML
    private void showOrders() {
        loadView("/fxml/orders.fxml");
        highlightButton(btnOrders);
    }


    @FXML
    private void showEmployees() {
        if (SessionManager.getInstance().isAdmin()) {
            loadView("/fxml/employees.fxml");
            highlightButton(btnEmployees);
        } else {
            AlertHelper.showWarning("Cảnh báo", "Bạn không có quyền truy cập!");
        }
    }


    @FXML
    private void showReports() {
        loadView("/fxml/reports.fxml");
        highlightButton(btnReports);
    }


    @FXML
    private void handleLogout() {
        if (AlertHelper.showConfirmation("Đăng xuất", "Bạn có chắc muốn đăng xuất?")) {
            SessionManager.getInstance().clearSession();


            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();


                Stage stage = (Stage) userLabel.getScene().getWindow();
                stage.setScene(new Scene(root, 500, 400));
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
        // Reset tất cả buttons
        btnSales.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 15; -fx-alignment: center-left; -fx-cursor: hand;");
        btnProducts.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 15; -fx-alignment: center-left; -fx-cursor: hand;");
        btnOrders.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 15; -fx-alignment: center-left; -fx-cursor: hand;");
        btnReports.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 15; -fx-alignment: center-left; -fx-cursor: hand;");


        if (btnEmployees.isVisible()) {
            btnEmployees.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 15; -fx-alignment: center-left; -fx-cursor: hand;");
        }


        // Highlight button đang active
        activeButton.setStyle("-fx-background-color: #37474F; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 15; -fx-alignment: center-left; -fx-cursor: hand;");
    }
}

