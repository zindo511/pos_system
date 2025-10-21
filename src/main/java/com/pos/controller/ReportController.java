package com.pos.controller;

import com.pos.dao.OrderDAO;
import com.pos.dao.ProductDAO;
import com.pos.model.Order;
import com.pos.model.OrderItem;
import com.pos.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportController {

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<String> reportTypeComboBox;
    @FXML
    private Button generateReportButton;
    @FXML
    private Button exportButton;

    // Tabs
    @FXML
    private TabPane reportTabPane;
    @FXML
    private Tab salesTab;
    @FXML
    private Tab productTab;
    @FXML
    private Tab chartTab;

    // Sales Report Components
    @FXML
    private TableView<Order> salesTableView;
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, String> orderDateColumn;
    @FXML
    private TableColumn<Order, String> employeeColumn;
    @FXML
    private TableColumn<Order, Double> totalColumn;
    @FXML
    private Label totalSalesLabel;
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label averageOrderLabel;

    // Product Report Components
    @FXML
    private TableView<ProductReport> productTableView;
    @FXML
    private TableColumn<ProductReport, String> productNameColumn;
    @FXML
    private TableColumn<ProductReport, Integer> quantitySoldColumn;
    @FXML
    private TableColumn<ProductReport, Double> revenueColumn;
    @FXML
    private Label topProductLabel;
    @FXML
    private Label totalProductsSoldLabel;

    // Chart Components
    @FXML
    private VBox chartContainer;
    @FXML
    private ComboBox<String> chartTypeComboBox;
    @FXML
    private BarChart<String, Number> salesBarChart;
    @FXML
    private LineChart<String, Number> salesLineChart;
    @FXML
    private PieChart productPieChart;

    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private ObservableList<Order> orderList;
    private ObservableList<ProductReport> productReportList;

    public ReportController() {
        this.orderDAO = new OrderDAO();
        this.productDAO = new ProductDAO();
        this.orderList = FXCollections.observableArrayList();
        this.productReportList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupDatePickers();
        setupComboBoxes();
        setupSalesTable();
        setupProductTable();
        setupCharts();
        setupEventHandlers();

        // Load default report (last 30 days)
        loadDefaultReport();
    }

    private void setupDatePickers() {
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
    }

    private void setupComboBoxes() {
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Daily Sales", "Weekly Sales", "Monthly Sales", "Product Performance", "Custom Range"
        ));
        reportTypeComboBox.setValue("Monthly Sales");

        chartTypeComboBox.setItems(FXCollections.observableArrayList(
                "Bar Chart", "Line Chart", "Pie Chart"
        ));
        chartTypeComboBox.setValue("Bar Chart");
    }

    private void setupSalesTable() {
        // Sử dụng id thay vì orderId
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Sử dụng createdAt và format thành String
        orderDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(formatter)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Sử dụng employeeId và format thành String
        employeeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("EMP-" + cellData.getValue().getEmployeeId())
        );

        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Format currency
        totalColumn.setCellFactory(col -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        salesTableView.setItems(orderList);
    }

    private void setupProductTable() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantitySoldColumn.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        revenueColumn.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        // Format currency
        revenueColumn.setCellFactory(col -> new TableCell<ProductReport, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        productTableView.setItems(productReportList);
    }

    private void setupCharts() {
        // Initially show bar chart only
        salesBarChart.setVisible(true);
        salesLineChart.setVisible(false);
        productPieChart.setVisible(false);
    }

    private void setupEventHandlers() {
        generateReportButton.setOnAction(e -> generateReport());
        exportButton.setOnAction(e -> exportReport());

        chartTypeComboBox.setOnAction(e -> updateChartDisplay());

        reportTypeComboBox.setOnAction(e -> {
            String reportType = reportTypeComboBox.getValue();
            if (reportType != null && !reportType.equals("Custom Range")) {
                updateDateRangeByType(reportType);
            }
        });
    }

    private void loadDefaultReport() {
        generateReport();
    }

    @FXML
    private void generateReport() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            AlertHelper.showError("Validation Error", "Please select both start and end dates.");
            return;
        }

        if (startDate.isAfter(endDate)) {
            AlertHelper.showError("Validation Error", "Start date must be before end date.");
            return;
        }

        try {
            // Load sales data
            loadSalesReport(startDate, endDate);

            // Load product data
            loadProductReport(startDate, endDate);

            // Update charts
            updateCharts(startDate, endDate);

//            AlertHelper.showInformation("Success", "Report generated successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            AlertHelper.showError("Database Error", "Failed to generate report: " + e.getMessage());
        }
    }

    private void loadSalesReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        orderList.clear();
        List<Order> orders = orderDAO.getOrdersByDateRange(startDate, endDate);
        orderList.addAll(orders);

        // Calculate statistics
        double totalSales = orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
        int totalOrders = orders.size();
        double averageOrder = totalOrders > 0 ? totalSales / totalOrders : 0;

        totalSalesLabel.setText(String.format("$%.2f", totalSales));
        totalOrdersLabel.setText(String.valueOf(totalOrders));
        averageOrderLabel.setText(String.format("$%.2f", averageOrder));
    }

    private void loadProductReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        productReportList.clear();

        // Get product sales data from orders
        Map<Integer, ProductReport> productSalesMap = new HashMap<>();
        List<Order> orders = orderDAO.getOrdersByDateRange(startDate, endDate);

        // Process each order and its items
        for (Order order : orders) {
            ObservableList<OrderItem> items = order.getItems();

            if (items != null && !items.isEmpty()) {
                for (OrderItem item : items) {
                    int productId = item.getProductId();

                    // Get or create product report
                    ProductReport report = productSalesMap.get(productId);
                    if (report == null) {
                        report = new ProductReport(
                                productId,
                                item.getProductName(),
                                0,
                                0.0
                        );
                        productSalesMap.put(productId, report);
                    }

                    // Update quantities and revenue using subtotal
                    report.setQuantitySold(report.getQuantitySold() + item.getQuantity());
                    report.setRevenue(report.getRevenue() + item.getSubtotal());
                }
            }
        }

        // Add to observable list and sort by revenue
        productReportList.addAll(productSalesMap.values());
        productReportList.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));

        // Update statistics
        if (!productReportList.isEmpty()) {
            topProductLabel.setText(productReportList.get(0).getProductName());
        } else {
            topProductLabel.setText("N/A");
        }

        int totalProductsSold = productReportList.stream()
                .mapToInt(ProductReport::getQuantitySold)
                .sum();
        totalProductsSoldLabel.setText(String.valueOf(totalProductsSold));
    }

    private void updateCharts(LocalDate startDate, LocalDate endDate) throws SQLException {
        updateBarChart(startDate, endDate);
        updateLineChart(startDate, endDate);
        updatePieChart();
    }

    private void updateBarChart(LocalDate startDate, LocalDate endDate) throws SQLException {
        salesBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales");

        // Aggregate sales by date (convert LocalDateTime to LocalDate)
        Map<LocalDate, Double> dailySales = new TreeMap<>();
        for (Order order : orderList) {
            if (order.getCreatedAt() != null) {
                LocalDate date = order.getCreatedAt().toLocalDate();
                dailySales.put(date, dailySales.getOrDefault(date, 0.0) + order.getTotalAmount());
            }
        }

        // Add data to chart
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        for (Map.Entry<LocalDate, Double> entry : dailySales.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    entry.getKey().format(formatter),
                    entry.getValue()
            ));
        }

        salesBarChart.getData().add(series);
    }

    private void updateLineChart(LocalDate startDate, LocalDate endDate) throws SQLException {
        salesLineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales Trend");

        // Aggregate sales by date (convert LocalDateTime to LocalDate)
        Map<LocalDate, Double> dailySales = new TreeMap<>();
        for (Order order : orderList) {
            if (order.getCreatedAt() != null) {
                LocalDate date = order.getCreatedAt().toLocalDate();
                dailySales.put(date, dailySales.getOrDefault(date, 0.0) + order.getTotalAmount());
            }
        }

        // Add data to chart
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        for (Map.Entry<LocalDate, Double> entry : dailySales.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    entry.getKey().format(formatter),
                    entry.getValue()
            ));
        }

        salesLineChart.getData().add(series);
    }

    private void updatePieChart() {
        productPieChart.getData().clear();

        // Show top 5 products by revenue
        List<ProductReport> topProducts = productReportList.stream()
                .limit(5)
                .collect(Collectors.toList());

        for (ProductReport report : topProducts) {
            productPieChart.getData().add(
                    new PieChart.Data(
                            report.getProductName() + " ($" + String.format("%.0f", report.getRevenue()) + ")",
                            report.getRevenue()
                    )
            );
        }
    }

    private void updateChartDisplay() {
        String chartType = chartTypeComboBox.getValue();
        if (chartType == null) return;

        // Hide all charts first
        salesBarChart.setVisible(false);
        salesLineChart.setVisible(false);
        productPieChart.setVisible(false);

        // Show selected chart
        switch (chartType) {
            case "Bar Chart":
                salesBarChart.setVisible(true);
                break;
            case "Line Chart":
                salesLineChart.setVisible(true);
                break;
            case "Pie Chart":
                productPieChart.setVisible(true);
                break;
        }
    }

    private void updateDateRangeByType(String reportType) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (reportType) {
            case "Daily Sales":
                startDate = endDate;
                break;
            case "Weekly Sales":
                startDate = endDate.minusWeeks(1);
                break;
            case "Monthly Sales":
                startDate = endDate.minusMonths(1);
                break;
            case "Product Performance":
                startDate = endDate.minusMonths(3);
                break;
            default:
                return;
        }

        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
        generateReport();
    }

    @FXML
    private void exportReport() {
        // TODO: Implementation for exporting report to CSV/PDF
//        AlertHelper.showInformation("Export", "Export functionality to be implemented.");
    }

    // Inner class for Product Report
    public static class ProductReport {
        private int productId;
        private String productName;
        private int quantitySold;
        private double revenue;

        public ProductReport(int productId, String productName, int quantitySold, double revenue) {
            this.productId = productId;
            this.productName = productName;
            this.quantitySold = quantitySold;
            this.revenue = revenue;
        }

        // Getters and Setters
        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantitySold() {
            return quantitySold;
        }

        public void setQuantitySold(int quantitySold) {
            this.quantitySold = quantitySold;
        }

        public double getRevenue() {
            return revenue;
        }

        public void setRevenue(double revenue) {
            this.revenue = revenue;
        }

        @Override
        public String toString() {
            return "ProductReport{" +
                    "productId=" + productId +
                    ", productName='" + productName + '\'' +
                    ", quantitySold=" + quantitySold +
                    ", revenue=" + revenue +
                    '}';
        }
    }
}

