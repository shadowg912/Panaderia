module com.example.panaderia {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    exports com.example.panaderia;

    opens com.example.panaderia to javafx.fxml;
    opens controllers to javafx.fxml;
}