module com.pos {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires static lombok;

    // Apache POI
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    // Cho phép FXML và TableView
    opens com.pos to javafx.fxml;
    opens com.pos.controller to javafx.fxml;
    opens com.pos.model to javafx.base;

    exports com.pos;
    exports com.pos.controller;
}
