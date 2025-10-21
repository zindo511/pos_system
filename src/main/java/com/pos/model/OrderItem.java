package com.pos.model;

import javafx.beans.property.*;

public class OrderItem {

    private final IntegerProperty id;
    private final IntegerProperty orderId;
    private final IntegerProperty productId;
    private final StringProperty productName;
    private final IntegerProperty quantity;
    private final DoubleProperty price;
    private final DoubleProperty subtotal;

    // Constructor không tham số
    public OrderItem() {
        this.id = new SimpleIntegerProperty();
        this.orderId = new SimpleIntegerProperty();
        this.productId = new SimpleIntegerProperty();
        this.productName = new SimpleStringProperty();
        this.quantity = new SimpleIntegerProperty();
        this.price = new SimpleDoubleProperty();
        this.subtotal = new SimpleDoubleProperty();
    }

    // Constructor khởi tạo với product (thường dùng khi chọn từ bảng sản phẩm)
    public OrderItem(int id, String name, int quantity, double price) {
        this.id = new SimpleIntegerProperty();
        this.orderId = new SimpleIntegerProperty();
        this.productId = new SimpleIntegerProperty(id);
        this.productName = new SimpleStringProperty(name);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
        this.subtotal = new SimpleDoubleProperty(price * quantity);
    }

    // Constructor đầy đủ
    public OrderItem(int id, int orderId, int productId, String productName, int quantity, double price, double subtotal) {
        this.id = new SimpleIntegerProperty(id);
        this.orderId = new SimpleIntegerProperty(orderId);
        this.productId = new SimpleIntegerProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
        this.subtotal = new SimpleDoubleProperty(subtotal);
    }

    // Getter/Setter + Property methods
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public int getOrderId() {
        return orderId.get();
    }

    public void setOrderId(int orderId) {
        this.orderId.set(orderId);
    }

    public IntegerProperty orderIdProperty() {
        return orderId;
    }

    public int getProductId() {
        return productId.get();
    }

    public void setProductId(int productId) {
        this.productId.set(productId);
    }

    public IntegerProperty productIdProperty() {
        return productId;
    }

    public String getProductName() {
        return productName.get();
    }

    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
        setSubtotal(this.price.get() * quantity); // cập nhật subtotal tự động
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
        setSubtotal(price * this.quantity.get()); // cập nhật subtotal tự động
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public double getSubtotal() {
        return subtotal.get();
    }

    public void setSubtotal(double subtotal) {
        this.subtotal.set(subtotal);
    }

    public DoubleProperty subtotalProperty() {
        return subtotal;
    }
}
