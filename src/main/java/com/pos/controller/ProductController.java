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
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Controller quản lý giao diện sản phẩm (Product Management)
 * Chức năng: thêm, sửa, xóa, tìm kiếm, hiển thị danh sách sản phẩm
 */
public class ProductController {

    // ======================== KHAI BÁO FXML ========================
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

    // ======================== BIẾN DÙNG CHUNG ========================
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private Product selectedProduct;

    // ======================== KHỞI TẠO ========================
    @FXML
    private void initialize() {
        setupTableColumns();
        setupPriceFormat();
        loadCategories();
        loadProducts();

        // Khi chọn 1 sản phẩm trong bảng
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedProduct = newSel;
                fillForm(newSel);
            }
        });

        // Mặc định chọn danh mục đầu tiên
        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.getSelectionModel().selectFirst();
        }
    }

    // ======================== CẤU HÌNH BẢNG ========================
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
    }

    private void setupPriceFormat() {
        // Hiển thị giá có định dạng ###.### đ
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
    }

    // ======================== NẠP DỮ LIỆU ========================
    private void loadCategories() {
        ObservableList<Category> categories = FXCollections.observableArrayList(categoryDAO.getAllCategories());
        categoryCombo.setItems(categories);

        categoryCombo.setConverter(new StringConverter<>() {
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
        productList.setAll(productDAO.getAllProducts());
        productTable.setItems(productList);
    }

    // ======================== FORM ========================
    private void fillForm(Product product) {
        productNameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        stockField.setText(String.valueOf(product.getStock()));

        categoryCombo.getItems().stream()
                .filter(cat -> cat.getId() == product.getCategoryId())
                .findFirst()
                .ifPresent(cat -> categoryCombo.getSelectionModel().select(cat));
    }

    @FXML
    private void handleClear() {
        productNameField.clear();
        priceField.clear();
        stockField.clear();
        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.getSelectionModel().selectFirst();
        }
        selectedProduct = null;
        productTable.getSelectionModel().clearSelection();
    }

    // ======================== CHỨC NĂNG ========================
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadProducts();
        } else {
            productList.setAll(productDAO.searchProducts(keyword));
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

        String name = productNameField.getText().trim();
        int categoryId = categoryCombo.getValue().getId();
        double price = Double.parseDouble(priceField.getText().trim());
        int stock = Integer.parseInt(stockField.getText().trim());

        // Kiểm tra xem có sản phẩm trùng (tên, danh mục, giá)
        Product existing = productDAO.getAllProducts().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name)
                        && p.getCategoryId() == categoryId
                        && Double.compare(p.getPrice(), price) == 0)
                .findFirst()
                .orElse(null);

        if (existing != null) {
            // Gộp tồn kho
            existing.setStock(existing.getStock() + stock);
            if (productDAO.updateProduct(existing)) {
                AlertHelper.showInfo("Thành công", "Sản phẩm đã tồn tại — đã gộp tồn kho!");
                loadProducts();
                handleClear();
            } else {
                AlertHelper.showError("Lỗi", "Không thể gộp tồn kho!");
            }
            return;
        }

        // Nếu chưa có thì thêm mới
        Product product = new Product();
        product.setName(name);
        product.setCategoryId(categoryId);
        product.setPrice(price);
        product.setStock(stock);

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

    // ======================== KIỂM TRA DỮ LIỆU ========================
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
