package com.pos.model;

import javafx.beans.property.*;

public class Product {
    private final IntegerProperty id;
    private final StringProperty name;
    private final IntegerProperty categoryId;
    private final DoubleProperty price;
    private final IntegerProperty stock;
    private final StringProperty imagePath;

    // Constructor no-argument

    public Product() {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.categoryId = new SimpleIntegerProperty();
        this.price = new SimpleDoubleProperty();
        this.stock = new SimpleIntegerProperty();
        this.imagePath = new SimpleStringProperty();
    }

    public Product(int id, String name, int categoryId, double price, int stock, String imagePath) {
        this();
        setId(id);
        setName(name);
        setCategoryId(categoryId);
        setPrice(price);
        setStock(stock);
        setImagePath(imagePath);
    }

    // ID
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    //Name
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    //CategoryID
    public int getCategoryId() {
        return categoryId.get();
    }

    public void setCategoryId(int categoryId) {
        this.categoryId.set(categoryId);
    }

    public IntegerProperty categoryIdProperty() {
        return categoryId;
    }

    //Price
    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    //Stock
    public int getStock() {
        return stock.get();
    }

    public void setStock(int stock) {
        this.stock.set(stock);
    }

    public IntegerProperty stockProperty() {
        return stock;
    }

    //ImagePath
    public String getImagePath() {
        return imagePath.get();
    }

    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }
}
