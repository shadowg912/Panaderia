package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Puesto;
import utils.AppNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.AlertHelper.*;

public class Crear_empleado_controller {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido1;
    @FXML private TextField txtApellido2;
    @FXML private TextField txtTelefono;
    @FXML private ComboBox<Puesto> cmbPuesto;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnCrearEmpleado;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<Puesto> listaPuestos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cargarPuestos();
    }

    private void cargarPuestos() {
        String sql = "SELECT id_puesto, nombre, area FROM PUESTO ORDER BY nombre";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaPuestos.add(new Puesto(rs.getInt("id_puesto"), rs.getString("nombre"), rs.getString("area")));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando puestos: " + e.getMessage());
        }
        cmbPuesto.setItems(listaPuestos);
    }

    @FXML
    public void fnCrearEmpleado(ActionEvent event) {
        if (!validarCampos()) return;

        String nombre = txtNombre.getText().trim();
        String apellido1 = txtApellido1.getText().trim();
        String apellido2 = txtApellido2.getText().trim();
        String telefono = txtTelefono.getText().trim();
        Integer idPuesto = cmbPuesto.getValue() != null ? cmbPuesto.getValue().getIdPuesto() : null;

        int idEmpleado = insertarEmpleado(nombre, apellido1, apellido2, telefono, idPuesto);
        if (idEmpleado > 0) {
            mostrarInfo("Empleado creado exitosamente.\nID: " + idEmpleado);
            fnLimpiar();
        } else {
            mostrarError("Error al crear el empleado");
        }
    }

    private int insertarEmpleado(String nombre, String apellido1, String apellido2, String telefono, Integer idPuesto) {
        String sql = "INSERT INTO EMPLEADO (nombre, apellido1, apellido2, numero_telefono, id_puesto) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, apellido1);
            ps.setString(3, apellido2.isEmpty() ? null : apellido2);
            ps.setString(4, telefono.isEmpty() ? null : telefono);
            if (idPuesto != null) {
                ps.setInt(5, idPuesto);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            mostrarError("Error insertando empleado: " + e.getMessage());
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
        cmbPuesto.setValue(null);
    }

    @FXML
    public void fnVolverMenu(ActionEvent event) {
        appNavigator.volverMenu();
    }



}