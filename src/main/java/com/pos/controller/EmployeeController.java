package com.pos.controller;


import com.pos.dao.EmployeeDAO;
import com.pos.model.Employee;
import com.pos.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;


public class EmployeeController {


    @FXML
    private TextField searchField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField fullNameField;
    @FXML
    private ComboBox<String> roleCombo;
    @FXML
    private TextField phoneField;


    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, Integer> colId;
    @FXML
    private TableColumn<Employee, String> colUsername;
    @FXML
    private TableColumn<Employee, String> colFullName;
    @FXML
    private TableColumn<Employee, String> colRole;
    @FXML
    private TableColumn<Employee, String> colPhone;


    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();


    // =======================================================
    //  INITIALIZE
    // =======================================================
    @FXML
    private void initialize() {
        // Setup table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));


        // Load data
        loadEmployees();


        // Bind data to table
        employeeTable.setItems(employeeList);


        // Setup combo box (roles)
        roleCombo.setItems(FXCollections.observableArrayList("admin", "cashier"));
        roleCombo.getSelectionModel().selectFirst();


        // Table selection listener (click to fill form)
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                fillForm(newSel);
            } else {
                clearForm();
            }
        });
    }


    // =======================================================
    //  LOAD DATA
    // =======================================================
    private void loadEmployees() {
        employeeList.clear();
        employeeList.addAll(employeeDAO.getAllEmployees());
    }


    // =======================================================
    //  SEARCH
    // =======================================================
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        employeeList.clear();
        if (keyword.isEmpty()) {
            employeeList.addAll(employeeDAO.getAllEmployees());
        } else {
            employeeList.addAll(employeeDAO.searchEmployees(keyword));
        }
    }


    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadEmployees();
    }


    // =======================================================
    //  ADD EMPLOYEE
    // =======================================================
    @FXML
    private void handleAdd() {
        if (!validateForm()) return;


        Employee emp = new Employee();
        emp.setUsername(usernameField.getText().trim());
        emp.setPassword(passwordField.getText().trim());
        emp.setFullName(fullNameField.getText().trim());
        emp.setRole(roleCombo.getValue());
        emp.setPhone(phoneField.getText().trim());


        boolean success = employeeDAO.addEmployee(emp);
        if (success) {
            employeeList.add(emp);
            AlertHelper.showInfo("Thành công", "Thêm nhân viên mới thành công!");
            clearForm();
        } else {
            AlertHelper.showError("Lỗi", "Không thể thêm nhân viên!");
        }
    }


    // =======================================================
    //  UPDATE EMPLOYEE
    // =======================================================
    @FXML
    private void handleUpdate() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Cảnh báo", "Vui lòng chọn nhân viên để cập nhật!");
            return;
        }


        if (!validateForm()) return;


        selected.setUsername(usernameField.getText().trim());
        selected.setPassword(passwordField.getText().trim());
        selected.setFullName(fullNameField.getText().trim());
        selected.setRole(roleCombo.getValue());
        selected.setPhone(phoneField.getText().trim());


        boolean success = employeeDAO.updateEmployee(selected);
        if (success) {
            AlertHelper.showInfo("Thành công", "Cập nhật thông tin nhân viên thành công!");
            clearForm();
        } else {
            AlertHelper.showError("Lỗi", "Không thể cập nhật thông tin nhân viên!");
        }
    }


    // =======================================================
    //  DELETE EMPLOYEE
    // =======================================================
    @FXML
    private void handleDelete() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Cảnh báo", "Vui lòng chọn nhân viên cần xóa!");
            return;
        }


        if (AlertHelper.showConfirmation("Xác nhận", "Bạn có chắc muốn xóa nhân viên này?")) {
            boolean success = employeeDAO.deleteEmployee(selected.getId());
            if (success) {
                AlertHelper.showInfo("Thành công", "Đã xóa nhân viên thành công!");
                employeeList.remove(selected);
                clearForm();
            } else {
                AlertHelper.showError("Lỗi", "Không thể xóa nhân viên!");
            }
        }
    }


    // =======================================================
    //  CLEAR FORM
    // =======================================================
    @FXML
    private void handleClear() {
        clearForm();
    }


    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        fullNameField.clear();
        phoneField.clear();
        roleCombo.getSelectionModel().selectFirst();
        employeeTable.getSelectionModel().clearSelection();
    }


    private void fillForm(Employee emp) {
        usernameField.setText(emp.getUsername());
        passwordField.setText(emp.getPassword());
        fullNameField.setText(emp.getFullName());
        phoneField.setText(emp.getPhone());
        roleCombo.setValue(emp.getRole());
    }


    private boolean validateForm() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || phone.isEmpty()) {
            AlertHelper.showWarning("Cảnh báo", "Vui lòng điền đầy đủ thông tin!");
            return false;
        }

        // Kiểm tra số điện thoại Việt Nam (10 số)
        if (!phone.matches("^(0)(3|5|7|8|9)[0-9]{8}$")) {
            AlertHelper.showWarning("Cảnh báo", "Số điện thoại không hợp lệ! Vui lòng nhập đúng 10 số theo định dạng Việt Nam.");
            return false;
        }

        return true;
    }

}



