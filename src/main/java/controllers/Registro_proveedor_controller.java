package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import utils.AppNavigator;

public class Registro_proveedor_controller {
    AppNavigator appNavigator = new AppNavigator();
    @FXML
    private Button btnCancelaRegistro;


    public void fnVolverMenu(ActionEvent actionEvent) {
        appNavigator.volverMenu();
    }
}
