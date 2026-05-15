package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Empleado;
import model.Rol;
import utils.AppNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Crear_usuario_controller {

    @FXML private TextField txtIdEmpleado;
    @FXML private TextField txtNombreEmpleado;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmarPassword;
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private Button btnBuscarEmpleado;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnCrearUsuario;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<Rol> Roles = FXCollections.observableArrayList();
    private Empleado empleadoSeleccionado;

    @FXML
    public void initialize() {
        cmbRol.setItems(cargarRoles());
    }

    public ObservableList<Rol> cargarRoles() {
        String sql = "SELECT id_rol, nombre_rol FROM ROL ORDER BY nombre_rol";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Roles.add(new Rol(rs.getInt(1), rs.getString("nombre_rol")));
            }
        } catch (Exception e) {
            mostrarError("Error cargando roles: " + e.getMessage());
        }
        return Roles;
    }

    @FXML
    public void fnBuscarEmpleado(ActionEvent event) {
        String idTexto = txtIdEmpleado.getText().trim();

        if (idTexto.isEmpty()) {
            mostrarAdvertencia("Ingrese el ID del empleado a buscar.");
            return;
        }

        int idEmpleado;
        try {
            idEmpleado = Integer.parseInt(idTexto);
        } catch (NumberFormatException e) {
            mostrarAdvertencia("El ID debe ser un número válido.");
            return;
        }

        Empleado emp = buscarEmpleadoPorId(idEmpleado);
        if (emp != null) {
            empleadoSeleccionado = emp;
            txtNombreEmpleado.setText(emp.getNombreCompleto());
        } else {
            empleadoSeleccionado = null;
            txtNombreEmpleado.setText("");
            mostrarAdvertencia("No se encontró ningún empleado con ese ID.");
        }
    }

private Empleado buscarEmpleadoPorId(int idEmpleado) {
        String sql = "SELECT id_empleado, nombre, apellido1, COALESCE(apellido2, '') as apellido2 FROM EMPLEADO WHERE id_empleado = ?";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Empleado(
                        rs.getInt("id_empleado"),
                        rs.getString("nombre"),
                        rs.getString("apellido1"));
            }
        } catch (SQLException e) {
            mostrarError("Error buscando empleado: " + e.getMessage());
        }
        return null;
    }

    @FXML
    public void fnCrearUsuario(ActionEvent event) {
        if (!validarCamposUsuario()) {
            return;
        }

        String nombreUsuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();
        Rol rol = cmbRol.getValue();

        int idUsuario = insertarUsuario(nombreUsuario, password, rol.getIdRol(), empleadoSeleccionado.getIdEmpleado());
        if (idUsuario > 0) {
            mostrarInfo("Usuario creado exitosamente para: " + empleadoSeleccionado.getNombreCompleto());
            fnLimpiar();
        } else {
            mostrarError("Error al crear el usuario");
        }
    }

    private int insertarUsuario(String nombreUsuario, String password, int idRol, int idEmpleado) {
        String sql = "INSERT INTO USUARIO (nombre_usuario, password_hash, id_rol, estado, id_empleado) VALUES (?, ?, ?, 1, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, password);
            ps.setInt(3, idRol);
            ps.setInt(4, idEmpleado);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            mostrarError("Error insertando usuario: " + e.getMessage());
        }
        return 0;
    }

    private boolean validarCamposUsuario() {
        if (empleadoSeleccionado == null) {
            mostrarAdvertencia("Debe seleccionar un empleado válido.");
            return false;
        }
        if (txtUsuario.getText() == null || txtUsuario.getText().trim().isEmpty()) {
            mostrarAdvertencia("El nombre de usuario es obligatorio.");
            return false;
        }
        if (txtPassword.getText() == null || txtPassword.getText().length() < 8) {
            mostrarAdvertencia("La contraseña debe tener al menos 8 caracteres.");
            return false;
        }
        if (!txtPassword.getText().equals(txtConfirmarPassword.getText())) {
            mostrarAdvertencia("Las contraseñas no coinciden.");
            return false;
        }
        if (cmbRol.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar un rol.");
            return false;
        }
        return true;
    }

    @FXML
    public void fnLimpiar() {
        txtIdEmpleado.clear();
        txtNombreEmpleado.clear();
        txtUsuario.clear();
        txtPassword.clear();
        txtConfirmarPassword.clear();
        cmbRol.setValue(null);
        empleadoSeleccionado = null;
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