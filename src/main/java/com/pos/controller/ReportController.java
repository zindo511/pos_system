package com.pos.controller;

import com.pos.dao.OrderDAO;
import com.pos.dao.ProductDAO;
import com.pos.model.Order;
import com.pos.model.OrderItem;
import com.pos.model.ProductReport;
import com.pos.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportController {

    // ========== FXML Components ==========
    @FXML
    private DatePicker startDatePicker, endDatePicker;
    @FXML
    private ComboBox<String> reportTypeComboBox, chartTypeComboBox;
    @FXML
    private Button generateReportButton, exportButton;
    @FXML
    private TabPane reportTabPane;
    @FXML
    private Tab salesTab, productTab, chartTab;

    // Sales Report
    @FXML
    private TableView<Order> salesTableView;
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, String> orderDateColumn, employeeColumn;
    @FXML
    private TableColumn<Order, Double> totalColumn;
    @FXML
    private Label totalSalesLabel, totalOrdersLabel, averageOrderLabel;

    // Product Report
    @FXML
    private TableView<ProductReport> productTableView;
    @FXML
    private TableColumn<ProductReport, String> productNameColumn;
    @FXML
    private TableColumn<ProductReport, Integer> quantitySoldColumn;
    @FXML
    private TableColumn<ProductReport, Double> revenueColumn;
    @FXML
    private Label topProductLabel, totalProductsSoldLabel;

    // Charts
    @FXML
    private StackPane chartContainer;
    @FXML
    private BarChart<String, Number> salesBarChart;
    @FXML
    private LineChart<String, Number> salesLineChart;
    @FXML
    private PieChart productPieChart;

    // ========== Data ==========
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ObservableList<Order> orderList = FXCollections.observableArrayList();
    private final ObservableList<ProductReport> productReportList = FXCollections.observableArrayList();

    // ========== Initialize ==========
    @FXML
    public void initialize() {
        setupDatePickers();
        setupComboBoxes();
        setupSalesTable();
        setupProductTable();
        setupCharts();
        setupEventHandlers();
        generateReport(); // Load default (last 30 days)
    }

    private void setupDatePickers() {
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
    }

    private void setupComboBoxes() {
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Bán hàng theo ngày", "Bán hàng theo tuần", "Bán hàng theo tháng", "Hiệu suất sản phẩm", "Tùy chỉnh"
        ));
        reportTypeComboBox.setValue("Bán hàng theo tháng");

        chartTypeComboBox.setItems(FXCollections.observableArrayList(
                "Biểu đồ cột", "Biểu đồ đường", "Biểu đồ tròn"
        ));
        chartTypeComboBox.setValue("Biểu đồ cột");
    }

    // ========== Setup Tables ==========
    private void setupSalesTable() {
        orderIdColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));

        orderDateColumn.setCellValueFactory(cell -> {
            LocalDateTime date = cell.getValue().getCreatedAt();
            if (date == null) return new javafx.beans.property.SimpleStringProperty("");
            return new javafx.beans.property.SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        });

        employeeColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty("EMP-" + cell.getValue().getEmployeeId())
        );

        totalColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getTotalAmount()));
        totalColumn.setCellFactory(col -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format(new Locale("vi", "VN"), "%,.0f đ", item));
            }
        });

        salesTableView.setItems(orderList);
    }

    private void setupProductTable() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantitySoldColumn.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        revenueColumn.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        revenueColumn.setCellFactory(col -> new TableCell<ProductReport, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format(new Locale("vi", "VN"), "%,.0f đ", item));
            }
        });

        productTableView.setItems(productReportList);
    }

    private void setupCharts() {
        salesBarChart.setVisible(true);
        salesLineChart.setVisible(false);
        productPieChart.setVisible(false);
    }

    private void setupEventHandlers() {
        generateReportButton.setOnAction(e -> generateReport());
        exportButton.setOnAction(e -> exportReport());
        chartTypeComboBox.setOnAction(e -> updateChartDisplay());
        reportTypeComboBox.setOnAction(e -> {
            String type = reportTypeComboBox.getValue();
            if (type != null && !type.equals("Tùy chỉnh")) updateDateRangeByType(type);
        });
    }

    // ========== Generate Reports ==========
    @FXML
    private void generateReport() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null) {
            AlertHelper.showError("Lỗi xác thực", "Vui lòng chọn cả ngày bắt đầu và ngày kết thúc.");
            return;
        }
        if (start.isAfter(end)) {
            AlertHelper.showError("Lỗi xác thực", "Ngày bắt đầu phải trước ngày kết thúc.");
            return;
        }

        try {
            loadSalesReport(start, end);
            loadProductReport(start, end);
            updateCharts();
        } catch (SQLException e) {
            AlertHelper.showError("Lỗi cơ sở dữ liệu", "Không thể tạo báo cáo: " + e.getMessage());
        }
    }

    private void loadSalesReport(LocalDate start, LocalDate end) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByDateRange(start, end);
        orderList.setAll(orders);

        double totalSales = orders.stream().mapToDouble(Order::getTotalAmount).sum();
        int totalOrders = orders.size();
        double avgOrder = totalOrders > 0 ? totalSales / totalOrders : 0;

        totalSalesLabel.setText(String.format(new Locale("vi", "VN"), "%,.0f đ", totalSales));
        totalOrdersLabel.setText(String.valueOf(totalOrders));
        averageOrderLabel.setText(String.format(new Locale("vi", "VN"), "%,.0f đ", avgOrder));
        salesTableView.refresh();
    }

    private void loadProductReport(LocalDate start, LocalDate end) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByDateRange(start, end);
        Map<Integer, ProductReport> map = new HashMap<>();

        for (Order order : orders) {
            ObservableList<OrderItem> items = order.getItems();
            if (items == null) continue;

            for (OrderItem item : items) {
                map.compute(item.getProductId(), (id, report) -> {
                    if (report == null)
                        report = new ProductReport(id, item.getProductName(), 0, 0.0);
                    report.setQuantitySold(report.getQuantitySold() + item.getQuantity());
                    report.setRevenue(report.getRevenue() + item.getSubtotal());
                    return report;
                });
            }
        }

        productReportList.setAll(map.values().stream()
                .sorted(Comparator.comparingDouble(ProductReport::getRevenue).reversed())
                .collect(Collectors.toList()));

        topProductLabel.setText(productReportList.isEmpty() ? "N/A" : productReportList.get(0).getProductName());
        totalProductsSoldLabel.setText(String.valueOf(
                productReportList.stream().mapToInt(ProductReport::getQuantitySold).sum()
        ));
        productTableView.refresh();
    }

    // ========== Charts ==========
    private void updateCharts() {
        updateBarOrLineChart(salesBarChart);
        updateBarOrLineChart(salesLineChart);
        updatePieChart();
    }

    private void updateBarOrLineChart(XYChart<String, Number> chart) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<LocalDate, Double> dailySales = new TreeMap<>();

        for (Order order : orderList) {
            if (order.getCreatedAt() == null) continue;
            LocalDate date = order.getCreatedAt().toLocalDate();
            dailySales.merge(date, order.getTotalAmount(), Double::sum);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        dailySales.forEach((date, total) ->
                series.getData().add(new XYChart.Data<>(date.format(fmt), total))
        );

        series.setName("Doanh thu theo ngày");
        chart.getData().add(series);
    }

    private void updatePieChart() {
        productPieChart.getData().clear();
        productReportList.stream()
                .limit(5)
                .forEach(p -> productPieChart.getData().add(
                        new PieChart.Data(p.getProductName() + String.format(" (₫%,.0f)", p.getRevenue()), p.getRevenue())
                ));
    }

    private void updateChartDisplay() {
        String type = chartTypeComboBox.getValue();
        if (type == null) return;

        salesBarChart.setVisible(false);
        salesBarChart.setManaged(false);
        salesLineChart.setVisible(false);
        salesLineChart.setManaged(false);
        productPieChart.setVisible(false);
        productPieChart.setManaged(false);

        switch (type) {
            case "Biểu đồ cột":
                salesBarChart.setVisible(true);
                salesBarChart.setManaged(true);
                break;

            case "Biểu đồ đường":
                salesLineChart.setVisible(true);
                salesLineChart.setManaged(true);
                break;

            case "Biểu đồ tròn":
                productPieChart.setVisible(true);
                productPieChart.setManaged(true);
                break;

            default:
                break;
        }
    }

    private void updateDateRangeByType(String type) {
        LocalDate end = LocalDate.now(), start = end;
        switch (type) {
            case "Bán hàng theo tuần":
                start = end.minusWeeks(1);
                break;
            case "Bán hàng theo tháng":
                start = end.minusMonths(1);
                break;
            case "Hiệu suất sản phẩm":
                start = end.minusMonths(3);
                break;
            default:
                break;
        }
        startDatePicker.setValue(start);
        endDatePicker.setValue(end);
        generateReport();
    }

    // ========== Export Excel ==========
    @FXML
    private void exportReport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu báo cáo Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        chooser.setInitialFileName("BaoCao_" + LocalDate.now() + ".xlsx");

        var file = chooser.showSaveDialog(exportButton.getScene().getWindow());
        if (file == null) return;

        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(wb);
            DataFormat df = wb.createDataFormat();
            CellStyle currencyStyle = wb.createCellStyle();
            currencyStyle.setDataFormat(df.getFormat("\"₫\"#,##0"));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Sheet 1: Sales
            Sheet salesSheet = wb.createSheet("Doanh số bán hàng");
            createHeaderRow(salesSheet, headerStyle, "Mã đơn hàng", "Ngày tạo", "Nhân viên", "Tổng tiền (₫)");
            int row = 1;
            for (Order order : orderList) {
                Row r = salesSheet.createRow(row++);
                r.createCell(0).setCellValue(order.getId());
                r.createCell(1).setCellValue(order.getCreatedAt() != null
                        ? order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "");
                r.createCell(2).setCellValue("EMP-" + order.getEmployeeId());
                org.apache.poi.ss.usermodel.Cell money = r.createCell(3);
                money.setCellValue(order.getTotalAmount());
                money.setCellStyle(currencyStyle);
            }
            for (int i = 0; i < 4; i++) salesSheet.autoSizeColumn(i);

            // Sheet 2: Product
            Sheet productSheet = wb.createSheet("Hiệu suất sản phẩm");
            createHeaderRow(productSheet, headerStyle, "Tên sản phẩm", "Số lượng bán", "Doanh thu (₫)");
            row = 1;
            for (ProductReport pr : productReportList) {
                Row r = productSheet.createRow(row++);
                r.createCell(0).setCellValue(pr.getProductName());
                r.createCell(1).setCellValue(pr.getQuantitySold());
                org.apache.poi.ss.usermodel.Cell rev = r.createCell(2);
                rev.setCellValue(pr.getRevenue());
                rev.setCellStyle(currencyStyle);
            }
            for (int i = 0; i < 3; i++) productSheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

            AlertHelper.showInfo("Xuất Excel thành công", "Báo cáo đã được lưu tại:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            AlertHelper.showError("Lỗi xuất file", "Không thể ghi file Excel: " + e.getMessage());
        }
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void createHeaderRow(Sheet sheet, CellStyle style, String... headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

}
