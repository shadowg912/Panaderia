module com.example.panaderia {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jasperreports;
    requires jbcrypt;

    exports com.example.panaderia;

    opens com.example.panaderia to javafx.fxml;
    opens controllers to javafx.fxml;
}