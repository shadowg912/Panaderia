package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class AppNavigator {

    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void load(String fxml) throws Exception {
        Parent root = FXMLLoader.load(AppNavigator.class.getResource(fxml));
        stage.getScene().setRoot(root);
    }
}