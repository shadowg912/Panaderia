module com.example.panaderia {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.example.panaderia to javafx.fxml;
    exports com.example.panaderia;
}