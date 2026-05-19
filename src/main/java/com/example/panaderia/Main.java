package com.example.panaderia;

import utils.AppNavigator;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        AppNavigator.setStage(stage);

        Parent root = FXMLLoader.load(getClass().getResource("/view/login_usuarios.fxml"));
        stage.setScene(new Scene(root, 1600, 900));
        stage.setTitle("Panadería - Sistema de Gestión");
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.centerOnScreen();
        stage.show();
    }

}
