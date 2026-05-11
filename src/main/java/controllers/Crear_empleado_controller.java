package controllers;

import Data_base.CONEXION;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.AppNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Crear_empleado_controller {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido1;
    @FXML private TextField txtApellido2;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtPuesto;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnCrearEmpleado;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();

    @FXML
    public void initialize() {
    }

    @FXML
    public void fnCrearEmpleado(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        String apellido1 = txtApellido1.getText().trim();
        String apellido2 = txtApellido2.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String puesto = txtPuesto.getText().trim();

        int idEmpleado = insertarEmpleado(nombre, apellido1, apellido2, telefono, puesto);
        if (idEmpleado > 0) {
            mostrarInfo("Empleado creado exitosamente.\nID: " + idEmpleado);
            fnLimpiar();
        } else {
            mostrarError("Error al crear el empleado");
        }
    }

    private int insertarEmpleado(String nombre, String apellido1, String apellido2, String telefono, String puesto) {
        String sql = "INSERT INTO EMPLEADO (nombre, apellido1, apellido2, numero_telefono, puesto) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, apellido1);
            ps.setString(3, apellido2.isEmpty() ? null : apellido2);
            ps.setString(4, telefono.isEmpty() ? null : telefono);
            ps.setString(5, puesto.isEmpty() ? null : puesto);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error insertando empleado: " + e.getMessage());
        }
        return 0;
    }

    private boolean validarCampos() {
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            mostrarAdvertencia("El nombre es obligatorio.");
            return false;
        }
        if (txtApellido1.getText() == null || txtApellido1.getText().trim().isEmpty()) {
            mostrarAdvertencia("El primer apellido es obligatorio.");
            return false;
        }
        return true;
    }

    @FXML
    public void fnLimpiar() {
        txtNombre.clear();
        txtApellido1.clear();
        txtApellido2.clear();
        txtTelefono.clear();
        txtPuesto.clear();
    }

    @FXML
    public void fnVolverMenu(ActionEvent event) {
        appNavigator.volverMenu();
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

    private void mostrarInfo(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}