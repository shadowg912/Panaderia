package controllers;

import Data_base.CONEXION;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.AppNavigator;
import utils.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Login_controller {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;

    private CONEXION conexion = new CONEXION();
    private AppNavigator appNavigator = new AppNavigator();

    @FXML
    public void fnIniciarSesion(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        if (usuario.isEmpty()) {
            mostrarAdvertencia("Ingrese su nombre de usuario.");
            return;
        }
        if (password.isEmpty()) {
            mostrarAdvertencia("Ingrese su contraseña.");
            return;
        }

        String sql = "SELECT u.id_usuario, u.nombre_usuario, r.nombre_rol, " +
                    "u.id_empleado, e.nombre + ' ' + e.apellido1 + ISNULL(' ' + e.apellido2, '') AS nombre_empleado " +
                    "FROM USUARIO u " +
                    "INNER JOIN ROL r ON u.id_rol = r.id_rol " +
                    "LEFT JOIN EMPLEADO e ON u.id_empleado = e.id_empleado " +
                    "WHERE u.nombre_usuario = ? AND u.password_hash = ? AND u.estado = 1";

        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                SesionUsuario.iniciarSesion(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre_usuario"),
                    rs.getString("nombre_rol"),
                    rs.getInt("id_empleado"),
                    rs.getString("nombre_empleado")
                );
                appNavigator.load("/view/Menu.fxml");
            } else {
                mostrarError("Usuario o contraseña incorrectos, o la cuenta está inactiva.");
            }
        } catch (Exception e) {
            mostrarError("Error al iniciar sesión: " + e.getMessage());
        }
    }

    @FXML
    public void fnSalir(ActionEvent event) {
        System.exit(0);
    }

    private void mostrarAdvertencia(String mensaje) {
        Alert a = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
