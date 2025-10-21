package com.pos.util;

import com.pos.model.Employee;

public class SessionManager {
    private static SessionManager instance;
    private Employee currentEmployee;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentEmployee(Employee employee) {
        this.currentEmployee = employee;
    }

    public Employee getCurrentEmployee() {
        return currentEmployee;
    }

    public void clearSession() {
        this.currentEmployee = null;
    }

    public boolean isLoggedIn() {
        return currentEmployee != null;
    }

    public boolean isAdmin() {
        return currentEmployee != null && "admin".equals(currentEmployee.getRole());
    }
}