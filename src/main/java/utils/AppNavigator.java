package utils;

import controllers.Confirmar_orden_controller;
import controllers.Detalle_orden_venta_controller;
import controllers.menu_contoller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AppNavigator {

    private static Stage stage;
    private static Pane contentPane;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void setContentPane(Pane pane) {
        contentPane = pane;
    }

    public static void load(String fxml) {
        try {
            Parent root = FXMLLoader.load(AppNavigator.class.getResource(fxml));
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void navigateTo(String fxml) {
        if (contentPane == null) {
            load(fxml);
            return;
        }
        try {
            Parent root = FXMLLoader.load(AppNavigator.class.getResource(fxml));
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            contentPane.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cargarDashboard() {
        if (contentPane != null && contentPane.getScene() != null) {
            menu_contoller.volverAlMenu();
        } else {
            load("/view/Menu.fxml");
        }
    }

    public void volverMenu() {
        cargarDashboard();
    }

    public static void irADetalleOrden(int idOrden) {
        Detalle_orden_venta_controller.setIdOrden(idOrden);
        navigateTo("/view/Detalle_Venta.fxml");
    }

    public static void irAConfirmarOrden() {
        load("/view/Confirmar_orden.fxml");
    }
}
