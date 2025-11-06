package com.pos.controller;

import com.pos.dao.OrderDAO;
import com.pos.dao.ProductDAO;
import com.pos.model.Order;
import com.pos.model.OrderItem;
import com.pos.model.Product;
import com.pos.util.AlertHelper;
import com.pos.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SalesController {

    @FXML
    private TextField searchField, customerPaidField;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableView<OrderItem> cartTable;
    @FXML
    private TableColumn<Product, Integer> colProductId, colStock;
    @FXML
    private TableColumn<Product, String> colProductName;
    @FXML
    private TableColumn<Product, Double> colPrice;
    @FXML
    private TableColumn<OrderItem, String> colCartProduct;
    @FXML
    private TableColumn<OrderItem, Integer> colCartQuantity;
    @FXML
    private TableColumn<OrderItem, Double> colCartPrice, colCartSubtotal;
    @FXML
    private Label totalLabel, changeLabel, employeeNameLabel, dateLabel, timeLabel;
    @FXML
    private ComboBox<String> paymentMethodCombo;
    @FXML
    private ProgressBar loadingProgressBar;

    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();
    private final NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
    private Timer timer;

    @FXML
    private void initialize() {
        setupProductTable();
        setupCartTable();
        setupPaymentMethods();
        setupEmployeeInfo();
        setupDateTime();
        setupEventListeners();
        loadProducts();

        if (loadingProgressBar != null) loadingProgressBar.setVisible(false);
    }

    /* ----------------- Thiết lập ban đầu ----------------- */

    private void setupEmployeeInfo() {
        var emp = SessionManager.getInstance().getCurrentEmployee();
        employeeNameLabel.setText(emp != null ? emp.getFullName() : "Chưa đăng nhập");
    }

    private void setupDateTime() {
        if (dateLabel == null || timeLabel == null) return;
        updateDateTime();
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(SalesController.this::updateDateTime);
            }
        }, 0, 1000);
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        timeLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private void setupProductTable() {
        colProductId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // createCurrencyCell là generic để phù hợp với TableColumn<T, Double>
        colPrice.setCellFactory(column -> createCurrencyCell());

        productTable.setItems(productList);
    }

    private void setupCartTable() {
        colCartProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCartQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCartSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        colCartPrice.setCellFactory(column -> createCurrencyCell());
        colCartSubtotal.setCellFactory(column -> createCurrencyCell());

        cartTable.setItems(cartItems);
    }

    /**
     * Generic TableCell creator so the returned TableCell has the correct generic type.
     * This avoids the "bad return type in lambda expression" compile error.
     */
    private <S> TableCell<S, Double> createCurrencyCell() {
        return new TableCell<S, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : formatCurrency(value));
            }
        };
    }

    private void setupPaymentMethods() {
        paymentMethodCombo.setItems(FXCollections.observableArrayList("Tiền mặt", "Chuyển khoản", "Thẻ"));
        paymentMethodCombo.getSelectionModel().selectFirst();
        paymentMethodCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Tiền mặt".equals(newVal)) calculateChange();
            else {
                if (customerPaidField != null) customerPaidField.clear();
                if (changeLabel != null) changeLabel.setText("0 đ");
            }
        });
    }

    private void setupEventListeners() {
        if (customerPaidField != null) {
            customerPaidField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.matches("\\d*(\\.\\d*)?")) calculateChange();
                else if (!newVal.isEmpty()) customerPaidField.setText(oldVal);
            });
        }
    }

    /* ----------------- Xử lý sản phẩm & giỏ hàng ----------------- */

    private void loadProducts() {
        productList.setAll(productDAO.getAllProducts());
    }

    @FXML
    private void handleSearch() {
        String keyword = (searchField == null) ? "" : searchField.getText().trim();
        productList.setAll(keyword.isEmpty() ? productDAO.getAllProducts() : productDAO.searchProducts(keyword));
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

        OrderItem item = findCartItem(selected.getId());
        if (item != null) {
            if (item.getQuantity() >= selected.getStock()) {
                AlertHelper.showWarning("Cảnh báo", "Không đủ hàng trong kho!");
                return;
            }
            item.setQuantity(item.getQuantity() + 1);
            item.setSubtotal(item.getPrice() * item.getQuantity());
        } else {
            // Tránh giả định constructor không tồn tại -> dùng setter
            OrderItem newItem = new OrderItem();
            newItem.setProductId(selected.getId());
            newItem.setProductName(selected.getName());
            newItem.setPrice(selected.getPrice());
            newItem.setQuantity(1);
            newItem.setSubtotal(selected.getPrice());
            cartItems.add(newItem);
        }

        cartTable.refresh();
        updateTotal();
    }

    private OrderItem findCartItem(int productId) {
        for (OrderItem i : cartItems)
            if (i.getProductId() == productId) return i;
        return null;
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

    /* ----------------- Thanh toán ----------------- */

    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            AlertHelper.showWarning("Cảnh báo", "Giỏ hàng trống!");
            return;
        }
        showLoading(true);

        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() {
                try {
                    updateProgress(0.3, 1);
                    if (!validateStock()) return -1;

                    updateProgress(0.6, 1);
                    double total = calculateTotal();
                    String payment = paymentMethodCombo.getValue();
                    if (!validatePayment(total, payment)) return -2;

                    updateProgress(0.8, 1);
                    Order order = createOrder(total, payment);
                    int result = orderDAO.createOrder(order, cartItems);

                    updateProgress(1, 1);
                    return result;
                } catch (Exception e) {
                    // in ra console nếu gặp lỗi
                    e.printStackTrace();
                    return -3;
                }
            }
        };

        if (loadingProgressBar != null) {
            loadingProgressBar.progressProperty().bind(task.progressProperty());
        }

        task.setOnSucceeded(e -> {
            int result = task.getValue();
            double total = calculateTotal();
            if (result > 0) {
                double change = 0;
                if ("Tiền mặt".equals(paymentMethodCombo.getValue())) {
                    try {
                        change = Double.parseDouble(customerPaidField.getText().trim()) - total;
                    } catch (NumberFormatException ex) {
                        change = 0;
                    }
                }
                showSuccessMessage(result, total, change);
                resetForm();
            } else {
                handleCheckoutError(result);
            }
            showLoading(false);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertHelper.showError("Lỗi", "Có lỗi xảy ra: " + (ex != null ? ex.getMessage() : "unknown"));
            showLoading(false);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Không dùng 'case ->' để tương thích Java 11
     */
    private void handleCheckoutError(int code) {
        switch (code) {
            case -1:
                AlertHelper.showWarning("Cảnh báo", "Không đủ tồn kho!");
                break;
            case -2:
                AlertHelper.showWarning("Cảnh báo", "Lỗi thanh toán!");
                break;
            default:
                AlertHelper.showError("Lỗi", "Không thể tạo đơn hàng!");
                break;
        }
    }

    private void showLoading(boolean show) {
        if (loadingProgressBar != null) loadingProgressBar.setVisible(show);
        Button checkoutBtn = findCheckoutButton();
        if (checkoutBtn != null) {
            checkoutBtn.setDisable(show);
            checkoutBtn.setText(show ? "ĐANG XỬ LÝ..." : "THANH TOÁN");
            if (!show && loadingProgressBar != null) {
                loadingProgressBar.progressProperty().unbind();
                loadingProgressBar.setProgress(0);
            }
        }
    }

    private Button findCheckoutButton() {
        return (customerPaidField != null && customerPaidField.getScene() != null)
                ? (Button) customerPaidField.getScene().lookup("#checkoutButton") : null;
    }

    private boolean validateStock() {
        for (OrderItem i : cartItems) {
            Product p = productDAO.getProductById(i.getProductId());
            if (p == null || p.getStock() < i.getQuantity()) return false;
        }
        return true;
    }

    private boolean validatePayment(double total, String method) {
        if (!"Tiền mặt".equals(method)) return true;
        try {
            return Double.parseDouble(customerPaidField.getText().trim()) >= total;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Order createOrder(double total, String method) {
        var emp = SessionManager.getInstance().getCurrentEmployee();
        Order order = new Order();
        if (emp != null) {
            order.setEmployeeId(emp.getId());
            order.setEmployeeName(emp.getFullName());
        }
        order.setTotalAmount(total);
        order.setPaymentMethod(method);

        if ("Tiền mặt".equals(method)) {
            try {
                double paid = Double.parseDouble(customerPaidField.getText().trim());
                order.setCustomerPaid(paid);
                order.setChangeAmount(paid - total);
            } catch (NumberFormatException e) {
                order.setCustomerPaid(0);
                order.setChangeAmount(0);
            }
        } else {
            order.setCustomerPaid(total);
            order.setChangeAmount(0);
        }
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private void showSuccessMessage(int id, double total, double change) {
        LocalDateTime now = LocalDateTime.now();
        String empName = SessionManager.getInstance().getCurrentEmployee() != null ?
                SessionManager.getInstance().getCurrentEmployee().getFullName() : "N/A";
        String msg = String.format(
                "Thanh toán thành công!\nMã đơn hàng: %d\nThời gian: %s\nNhân viên: %s\nTổng tiền: %s\nPhương thức: %s",
                id,
                now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                empName,
                formatCurrency(total),
                paymentMethodCombo.getValue()
        );
        if ("Tiền mặt".equals(paymentMethodCombo.getValue())) {
            msg += "\nTiền thừa: " + formatCurrency(change);
        }
        AlertHelper.showInfo("Thành công", msg);
    }

    private void resetForm() {
        cartItems.clear();
        if (customerPaidField != null) customerPaidField.clear();
        updateTotal();
        loadProducts();
        if (searchField != null) searchField.clear();
        if (paymentMethodCombo != null) paymentMethodCombo.getSelectionModel().selectFirst();
    }

    private void updateTotal() {
        totalLabel.setText(formatCurrency(calculateTotal()));
        calculateChange();
    }

    private double calculateTotal() {
        return cartItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
    }

    private void calculateChange() {
        if (paymentMethodCombo == null || !"Tiền mặt".equals(paymentMethodCombo.getValue())) {
            if (changeLabel != null) changeLabel.setText("0 đ");
            return;
        }
        try {
            double total = calculateTotal();
            double paid = Double.parseDouble(customerPaidField.getText().trim());
            double change = Math.max(0, paid - total);
            changeLabel.setText(formatCurrency(change));
        } catch (Exception e) {
            changeLabel.setText("0 đ");
        }
    }

    private String formatCurrency(double amount) {
        return formatter.format(amount) + " đ";
    }

    public void cleanup() {
        if (timer != null) timer.cancel();
    }
}
