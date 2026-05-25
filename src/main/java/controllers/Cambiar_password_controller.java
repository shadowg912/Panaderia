package controllers;

import Data_base.CONEXION;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import org.mindrot.jbcrypt.BCrypt;
import utils.AppNavigator;
import utils.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static utils.AlertHelper.*;

public class Cambiar_password_controller {

    @FXML private PasswordField txtPasswordActual;
    @FXML private PasswordField txtPasswordNueva;
    @FXML private PasswordField txtConfirmarPassword;

    private CONEXION conexion = new CONEXION();

    @FXML
    public void fnCambiarPassword() {
        String passActual = txtPasswordActual.getText();
        String passNueva = txtPasswordNueva.getText();
        String passConfirm = txtConfirmarPassword.getText();

        if (passActual.isEmpty() || passNueva.isEmpty() || passConfirm.isEmpty()) {
            mostrarAdvertencia("Todos los campos son obligatorios.");
            return;
        }
        if (passNueva.length() < 8) {
            mostrarAdvertencia("La nueva contraseña debe tener al menos 8 caracteres.");
            return;
        }
        if (!passNueva.equals(passConfirm)) {
            mostrarAdvertencia("Las contraseñas nuevas no coinciden.");
            return;
        }

        int idUsuario = SesionUsuario.getIdUsuario();
        if (idUsuario <= 0) {
            mostrarError("No hay una sesión activa.");
            return;
        }

        try (Connection conn = conexion.establecerconexio()) {
            String storedHash = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT password_hash FROM USUARIO WHERE id_usuario = ?")) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) storedHash = rs.getString("password_hash");
                }
            }

            if (storedHash == null) {
                mostrarError("No se encontró el usuario.");
                return;
            }

            boolean actualValida;
            if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$")) {
                actualValida = BCrypt.checkpw(passActual, storedHash);
            } else {
                actualValida = passActual.equals(storedHash);
            }

            if (!actualValida) {
                mostrarError("La contraseña actual no es correcta.");
                return;
            }

            String nuevoHash = BCrypt.hashpw(passNueva, BCrypt.gensalt());
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE USUARIO SET password_hash = ? WHERE id_usuario = ?")) {
                ps.setString(1, nuevoHash);
                ps.setInt(2, idUsuario);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    mostrarInfo("Contraseña actualizada exitosamente.");
                    txtPasswordActual.clear();
                    txtPasswordNueva.clear();
                    txtConfirmarPassword.clear();
                } else {
                    mostrarError("No se pudo actualizar la contraseña.");
                }
            }

        } catch (Exception e) {
            mostrarError("Error al cambiar contraseña: " + e.getMessage());
        }
    }

    @FXML
    public void fnVolver() {
        AppNavigator.cargarDashboard();
    }
}
