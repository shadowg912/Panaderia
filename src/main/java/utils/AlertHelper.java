package utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;

public class AlertHelper {

    private static void reaplicarMaximizado() {
        Platform.runLater(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof Stage && w.isShowing()) {
                    Stage s = (Stage) w;
                    if (s.isMaximized()) {
                        s.setMaximized(false);
                        s.setMaximized(true);
                    }
                }
            }
        });
    }

    public static void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
        reaplicarMaximizado();
    }

    public static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
        reaplicarMaximizado();
    }

    public static void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
        reaplicarMaximizado();
    }
}
