package com.pos.controller;

import com.pos.dao.OrderDAO;
import com.pos.dao.ProductDAO;
import com.pos.model.Order;
import com.pos.model.OrderItem;
import com.pos.model.Product;
import com.pos.util.AlertHelper;
import com.pos.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.NumberFormat;
import java.util.Locale;

public class SalesController {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> colProductId;
    @FXML
    private TableColumn<Product, String> colProductName;
    @FXML
    private TableColumn<Product, Double> colPrice;
    @FXML
    private TableColumn<Product, Integer> colStock;

    @FXML
    private TableView<OrderItem> cartTable;
    @FXML
    private TableColumn<OrderItem, String> colCartProduct;
    @FXML
    private TableColumn<OrderItem, Integer> colCartQuantity;
    @FXML
    private TableColumn<OrderItem, Double> colCartPrice;
    @FXML
    private TableColumn<OrderItem, Double> colCartSubtotal;

    @FXML
    private Label totalLabel;
    @FXML
    private ComboBox<String> paymentMethodCombo;
    @FXML
    private TextField customerPaidField;
    @FXML
    private Label changeLabel;

    private ProductDAO productDAO = new ProductDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();

    private NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    @FXML
    private void initialize() {
        // Setup product table
        colProductId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("name"));
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
                    setText(formatter.format(price) + " đ");
                }
            }
        });

        // Setup cart table
        colCartProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCartQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCartSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Format cart price columns
        colCartPrice.setCellFactory(column -> new TableCell<OrderItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(formatter.format(price) + " đ");
                }
            }
        });

        colCartSubtotal.setCellFactory(column -> new TableCell<OrderItem, Double>() {
            @Override
            protected void updateItem(Double subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                if (empty || subtotal == null) {
                    setText(null);
                } else {
                    setText(formatter.format(subtotal) + " đ");
                }
            }
        });

        // Load products
        loadProducts();

        // Set cart table
        cartTable.setItems(cartItems);

        // Payment method default
        paymentMethodCombo.getSelectionModel().selectFirst();

        // Calculate change when customer paid changes
        customerPaidField.textProperty().addListener((obs, oldVal, newVal) -> {
            calculateChange();
        });
    }

    private void loadProducts() {
        productList.clear();
        productList.addAll(productDAO.getAllProducts());
        productTable.setItems(productList);
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
    private void handleAddToCart() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Cảnh báo", "Vui lòng chọn sản phẩm!");
            return;
        }

        if (selected.getStock() <= 0) {
            AlertHelper.showWarning("Cảnh báo", "Sản phẩm đã hết hàng!");
            return;
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        OrderItem existingItem = null;
        for (OrderItem item : cartItems) {
            if (item.getProductId() == selected.getId()) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            // Tăng số lượng
            if (existingItem.getQuantity() >= selected.getStock()) {
                AlertHelper.showWarning("Cảnh báo", "Không đủ hàng trong kho!");
                return;
            }
            existingItem.setQuantity(existingItem.getQuantity() + 1);
            existingItem.setSubtotal(existingItem.getQuantity() * existingItem.getPrice());
        } else {
            // Thêm mới
            OrderItem newItem = new OrderItem(
                    selected.getId(),
                    selected.getName(),
                    1,
                    selected.getPrice()
            );
            cartItems.add(newItem);
        }

        cartTable.refresh();
        updateTotal();
    }

    @FXML
    private void handleRemoveFromCart() {
        OrderItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Cảnh báo", "Vui lòng chọn sản phẩm cần xóa!");
            return;
        }

        cartItems.remove(selected);
        updateTotal();
    }

    @FXML
    private void handleClearCart() {
        if (cartItems.isEmpty()) return;

        if (AlertHelper.showConfirmation("Xác nhận", "Bạn có chắc muốn xóa tất cả sản phẩm?")) {
            cartItems.clear();
            updateTotal();
        }
    }

    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            AlertHelper.showWarning("Cảnh báo", "Giỏ hàng trống!");
            return;
        }

        double total = calculateTotal();
        double paid = 0;

        try {
            paid = Double.parseDouble(customerPaidField.getText().trim());
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Cảnh báo", "Vui lòng nhập số tiền khách đưa!");
            return;
        }

        if (paid < total) {
            AlertHelper.showWarning("Cảnh báo", "Số tiền khách đưa không đủ!");
            return;
        }

        // Tạo order
        Order order = new Order();
        order.setEmployeeId(SessionManager.getInstance().getCurrentEmployee().getId());
        order.setTotalAmount(total);
        order.setPaymentMethod(paymentMethodCombo.getValue());
        order.setCustomerPaid(paid);
        order.setChangeAmount(paid - total);

        int orderId = orderDAO.createOrder(order, cartItems);

        if (orderId > 0) {
            AlertHelper.showInfo("Thành công",
                    "Thanh toán thành công!\n" +
                            "Mã đơn hàng: " + orderId + "\n" +
                            "Tổng tiền: " + formatter.format(total) + " đ\n" +
                            "Tiền thừa: " + formatter.format(paid - total) + " đ");

            // Reset
            cartItems.clear();
            customerPaidField.clear();
            updateTotal();
            loadProducts();
        } else {
            AlertHelper.showError("Lỗi", "Không thể tạo đơn hàng!");
        }
    }

    private void updateTotal() {
        double total = calculateTotal();
        totalLabel.setText(formatter.format(total) + " đ");
        calculateChange();
    }

    private double calculateTotal() {
        double total = 0;
        for (OrderItem item : cartItems) {
            total += item.getSubtotal();
        }
        return total;
    }

    private void calculateChange() {
        try {
            double total = calculateTotal();
            double paid = Double.parseDouble(customerPaidField.getText().trim());
            double change = paid - total;
            changeLabel.setText(formatter.format(Math.max(0, change)) + " đ");
        } catch (NumberFormatException e) {
            changeLabel.setText("0 đ");
        }
    }
}