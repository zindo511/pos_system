package com.pos.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;

public class Order {
    private final IntegerProperty id;
    private final IntegerProperty employeeId;
    private final StringProperty employeeName;
    private final DoubleProperty totalAmount;
    private final StringProperty paymentMethod;
    private final DoubleProperty customerPaid;
    private final DoubleProperty changeAmount;
    private final ObjectProperty<LocalDateTime> createdAt;

    // 🔹 Danh sách sản phẩm trong đơn hàng
    private final ObservableList<OrderItem> items;

    public Order() {
        this.id = new SimpleIntegerProperty();
        this.employeeId = new SimpleIntegerProperty();
        this.employeeName = new SimpleStringProperty();
        this.totalAmount = new SimpleDoubleProperty();
        this.paymentMethod = new SimpleStringProperty();
        this.customerPaid = new SimpleDoubleProperty();
        this.changeAmount = new SimpleDoubleProperty();
        this.createdAt = new SimpleObjectProperty<>();
        this.items = FXCollections.observableArrayList(); // ✅ khởi tạo danh sách rỗng
    }

    // ========================
    // Getters và Setters cơ bản
    // ========================

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public int getEmployeeId() {
        return employeeId.get();
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId.set(employeeId);
    }

    public IntegerProperty employeeIdProperty() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName.get();
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName.set(employeeName);
    }

    public StringProperty employeeNameProperty() {
        return employeeName;
    }

    public double getTotalAmount() {
        return totalAmount.get();
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount.set(totalAmount);
    }

    public DoubleProperty totalAmountProperty() {
        return totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod.get();
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod.set(paymentMethod);
    }

    public StringProperty paymentMethodProperty() {
        return paymentMethod;
    }

    public double getCustomerPaid() {
        return customerPaid.get();
    }

    public void setCustomerPaid(double customerPaid) {
        this.customerPaid.set(customerPaid);
    }

    public DoubleProperty customerPaidProperty() {
        return customerPaid;
    }

    public double getChangeAmount() {
        return changeAmount.get();
    }

    public void setChangeAmount(double changeAmount) {
        this.changeAmount.set(changeAmount);
    }

    public DoubleProperty changeAmountProperty() {
        return changeAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public ObservableList<OrderItem> getItems() {
        return items;
    }

    public void setItems(ObservableList<OrderItem> items) {
        this.items.setAll(items);
    }
}
