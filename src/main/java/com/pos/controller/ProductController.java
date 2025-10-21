package com.pos.controller;

import com.pos.dao.CategoryDAO;
import com.pos.dao.ProductDAO;
import com.pos.model.Category;
import com.pos.model.Product;
import com.pos.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductController {

    @FXML
    private TextField searchField;
    @FXML
    private TextField productNameField;
    @FXML
    private ComboBox<Category> categoryCombo;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockField;

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> colId;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, String> colCategory;
    @FXML
    private TableColumn<Product, Double> colPrice;
    @FXML
    private TableColumn<Product, Integer> colStock;

    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private Product selectedProduct = null;

    @FXML
    private void initialize() {
        // Setup table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Format price column
        colPrice.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                    setText(formatter.format(price) + " đ");
                }
            }
        });

        // Load categories
        loadCategories();

        // Load products
        loadProducts();

        // Selection listener
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProduct = newSelection;
                fillForm(newSelection);
            }
        });

        // Set default payment method
        if (categoryCombo.getItems().size() > 0) {
            categoryCombo.getSelectionModel().selectFirst();
        }
    }

    private void loadCategories() {
        categoryCombo.setItems(FXCollections.observableArrayList(categoryDAO.getAllCategories()));
        categoryCombo.setConverter(new javafx.util.StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "";
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        });
    }

    private void loadProducts() {
        productList.clear();
        productList.addAll(productDAO.getAllProducts());
        productTable.setItems(productList);
    }

    private void fillForm(Product product) {
        productNameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        stockField.setText(String.valueOf(product.getStock()));

        // Select category
        for (Category cat : categoryCombo.getItems()) {
            if (cat.getId() == product.getCategoryId()) {
                categoryCombo.getSelectionModel().select(cat);
                break;
            }
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadProducts();
        } else {
            productList.clear();
            productList.addAll(productDAO.searchProducts(keyword));
            productTable.setItems(productList);
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadProducts();
        handleClear();
    }

    @FXML
    private void handleAdd() {
        if (!validateInput()) return;

        Product product = new Product();
        product.setName(productNameField.getText().trim());
        product.setCategoryId(categoryCombo.getValue().getId());
        product.setPrice(Double.parseDouble(priceField.getText().trim()));
        product.setStock(Integer.parseInt(stockField.getText().trim()));

        if (productDAO.addProduct(product)) {
            AlertHelper.showInfo("Thành công", "Thêm sản phẩm thành công!");
            loadProducts();
            handleClear();
        } else {
            AlertHelper.showError("Lỗi", "Không thể thêm sản phẩm!");
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedProduct == null) {
            AlertHelper.showWarning("Cảnh báo", "Vui lòng chọn sản phẩm cần cập nhật!");
            return;
        }

        if (!validateInput()) return;

        selectedProduct.setName(productNameField.getText().trim());
        selectedProduct.setCategoryId(categoryCombo.getValue().getId());
        selectedProduct.setPrice(Double.parseDouble(priceField.getText().trim()));
        selectedProduct.setStock(Integer.parseInt(stockField.getText().trim()));

        if (productDAO.updateProduct(selectedProduct)) {
            AlertHelper.showInfo("Thành công", "Cập nhật sản phẩm thành công!");
            loadProducts();
            handleClear();
        } else {
            AlertHelper.showError("Lỗi", "Không thể cập nhật sản phẩm!");
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedProduct == null) {
            AlertHelper.showWarning("Cảnh báo", "Vui lòng chọn sản phẩm cần xóa!");
            return;
        }

        if (AlertHelper.showConfirmation("Xác nhận", "Bạn có chắc muốn xóa sản phẩm này?")) {
            if (productDAO.deleteProduct(selectedProduct.getId())) {
                AlertHelper.showInfo("Thành công", "Xóa sản phẩm thành công!");
                loadProducts();
                handleClear();
            } else {
                AlertHelper.showError("Lỗi", "Không thể xóa sản phẩm!");
            }
        }
    }

    @FXML
    private void handleClear() {
        productNameField.clear();
        priceField.clear();
        stockField.clear();
        if (categoryCombo.getItems().size() > 0) {
            categoryCombo.getSelectionModel().selectFirst();
        }
        selectedProduct = null;
        productTable.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        if (productNameField.getText().trim().isEmpty()) {
            AlertHelper.showWarning("Cảnh báo", "Vui lòng nhập tên sản phẩm!");
            return false;
        }

        if (categoryCombo.getValue() == null) {
            AlertHelper.showWarning("Cảnh báo", "Vui lòng chọn danh mục!");
            return false;
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                AlertHelper.showWarning("Cảnh báo", "Giá phải lớn hơn 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Cảnh báo", "Giá không hợp lệ!");
            return false;
        }

        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                AlertHelper.showWarning("Cảnh báo", "Số lượng không được âm!");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Cảnh báo", "Số lượng không hợp lệ!");
            return false;
        }

        return true;
    }
}