package utils;

import controllers.Confirmar_orden_controller;
import controllers.Detalle_orden_venta_controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class AppNavigator {

    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void load(String fxml) {
        try {
            Parent root = FXMLLoader.load(AppNavigator.class.getResource(fxml));
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void volverMenu(){
        AppNavigator.load("/view/Menu.fxml");
    }

    public static void irADetalleOrden(int idOrden) {
        Detalle_orden_venta_controller.setIdOrden(idOrden);
        load("/view/Detalle_orden.fxml");
    }

    public static void irAConfirmarOrden() {
        load("/view/Confirmar_orden.fxml");
    }
}