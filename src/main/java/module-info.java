module com.example.panaderia {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.panaderia to javafx.fxml;
    exports com.example.panaderia;
}