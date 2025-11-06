package com.pos.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Employee {

    public final IntegerProperty id;
    public final StringProperty username;
    public final StringProperty password;
    public final StringProperty fullName;
    public final StringProperty role;
    public final StringProperty phone;

    // Constructor no-argument
    public Employee() {
        this.id = new SimpleIntegerProperty();
        this.username = new SimpleStringProperty();
        this.password = new SimpleStringProperty();
        this.fullName = new SimpleStringProperty();
        this.role = new SimpleStringProperty();
        this.phone = new SimpleStringProperty();
    }

    // Constructor full-argument
    public Employee(int id, String username, String password, String fullName, String role, String phone) {
        this();
        setId(id);
        setUsername(username);
        setPassword(password);
        setFullName(fullName);
        setRole(role);
        setPhone(phone);
    }

    //ID
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    //Username
    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    //Password
    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public StringProperty passwordProperty() {
        return password;
    }

    //FullName
    public String getFullName() {
        return fullName.get();
    }

    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    //Role
    public String getRole() {
        return role.get();
    }

    public void setRole(String role) {
        this.role.set(role);
    }

    public StringProperty roleProperty() {
        return role;
    }

    //Phone
    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public StringProperty phoneProperty() {
        return phone;
    }
}
