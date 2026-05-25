package controllers;

import Data_base.CONEXION;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;
import utils.AppNavigator;
import utils.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static utils.AlertHelper.*;

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

        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.password_hash, r.nombre_rol, " +
                    "u.id_empleado, e.nombre + ' ' + e.apellido1 + ISNULL(' ' + e.apellido2, '') AS nombre_empleado " +
                    "FROM USUARIO u " +
                    "INNER JOIN ROL r ON u.id_rol = r.id_rol " +
                    "LEFT JOIN EMPLEADO e ON u.id_empleado = e.id_empleado " +
                    "WHERE u.nombre_usuario = ? AND u.estado = 1";

        try (Connection conn = conexion.establecerconexio()) {
            conn.setAutoCommit(false);

            String storedHash = null;
            int idUsuario = 0;
            String nombreUsuario = null;
            String nombreRol = null;
            int idEmpleado = 0;
            String nombreEmpleado = null;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, usuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        storedHash = rs.getString("password_hash");
                        idUsuario = rs.getInt("id_usuario");
                        nombreUsuario = rs.getString("nombre_usuario");
                        nombreRol = rs.getString("nombre_rol");
                        idEmpleado = rs.getInt("id_empleado");
                        nombreEmpleado = rs.getString("nombre_empleado");
                    }
                }
            }

            if (storedHash == null) {
                conn.rollback();
                mostrarError("Usuario o contraseña incorrectos, o la cuenta está inactiva.");
                return;
            }

            boolean valido;
            boolean migrar = false;

            if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$")) {
                valido = BCrypt.checkpw(password, storedHash);
            } else {
                valido = password.equals(storedHash);
                if (valido) migrar = true;
            }

            if (!valido) {
                conn.rollback();
                mostrarError("Usuario o contraseña incorrectos, o la cuenta está inactiva.");
                return;
            }

            if (migrar) {
                String newHash = BCrypt.hashpw(password, BCrypt.gensalt());
                try (PreparedStatement up = conn.prepareStatement(
                        "UPDATE USUARIO SET password_hash = ? WHERE id_usuario = ?")) {
                    up.setString(1, newHash);
                    up.setInt(2, idUsuario);
                    up.executeUpdate();
                }
            }

            conn.commit();

            SesionUsuario.iniciarSesion(idUsuario, nombreUsuario, nombreRol, idEmpleado, nombreEmpleado);
            appNavigator.load("/view/Menu.fxml");

        } catch (Exception e) {
            mostrarError("Error al iniciar sesión: " + e.getMessage());
        }
    }

    @FXML
    public void fnSalir(ActionEvent event) {
        System.exit(0);
    }

}
