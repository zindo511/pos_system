package com.pos.controller;


import com.pos.dao.OrderDAO;
import com.pos.model.Order;
import com.pos.model.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;


import java.util.List;


public class OrderController {


    @FXML
    private TextField searchField;
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Integer> colOrderId;
    @FXML
    private TableColumn<Order, String> colEmployee;
    @FXML
    private TableColumn<Order, String> colDate;
    @FXML
    private TableColumn<Order, Double> colTotal;


    @FXML
    private TableView<OrderItem> orderItemTable;
    @FXML
    private TableColumn<OrderItem, String> colProductName;
    @FXML
    private TableColumn<OrderItem, Integer> colQuantity;
    @FXML
    private TableColumn<OrderItem, Double> colPrice;
    @FXML
    private TableColumn<OrderItem, Double> colSubtotal;


    private final OrderDAO orderDAO = new OrderDAO();


    @FXML
    public void initialize() {
        // Gán cột cho bảng đơn hàng
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));


        // Gán cột cho bảng chi tiết
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));


        loadOrders();
        orderTable.setOnMouseClicked(e -> showOrderItems());
    }


    private void loadOrders() {
        try {
            List<Order> orders = orderDAO.getAllOrders();
            orderTable.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleSearch() {
        try {
            String keyword = searchField.getText();
            List<Order> orders = orderDAO.searchOrders(keyword);
            orderTable.setItems(FXCollections.observableArrayList(orders));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadOrders();
        orderItemTable.getItems().clear();
    }


    private void showOrderItems() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                ObservableList<OrderItem> items = orderDAO.getOrderItemsByOrderId(selected.getId());
                orderItemTable.setItems(items);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

