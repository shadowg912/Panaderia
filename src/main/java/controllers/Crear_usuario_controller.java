package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import model.Empleado;
import model.Rol;
import model.Usuario;
import utils.AppNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Crear_usuario_controller {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtPuesto;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmarPassword;
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private RadioButton rbActivo;
    @FXML private RadioButton rbInactivo;
    @FXML private Button btnCrearEmpleado;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnCrearUsuario;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<Rol> Roles = FXCollections.observableArrayList();
    ToggleGroup grupoEstado = new ToggleGroup();

    public ObservableList cargarRoles() {
        String sql = "SELECT id_rol, nombre_rol FROM ROL ORDER BY nombre_rol";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Roles.add(new Rol(rs.getInt(1), rs.getString("nombre_rol")));
            }
        } catch (Exception e) {
            System.out.println("Error cargando roles: " + e.getMessage());
        }
        return Roles;
    }

    @FXML
    public void initialize() {
        cmbRol.setItems(cargarRoles());
        rbActivo.setToggleGroup(grupoEstado);
        rbInactivo.setToggleGroup(grupoEstado);
        rbActivo.setSelected(true);
    }

    @FXML
    public void fnCrearEmpleado(ActionEvent event) {
        if (!validarCamposEmpleado()) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String puesto = txtPuesto.getText().trim();

        int idEmpleado = insertarEmpleado(nombre, apellido, telefono, puesto);

        if (idEmpleado > 0) {
            System.out.println("Empleado creado exitosamente con ID: " + idEmpleado);
        } else {
            System.out.println("Error al crear empleado");
        }
    }

    @FXML
    public void fnCrearUsuario(ActionEvent event) {
        if (!validarCamposUsuario()) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String puesto = txtPuesto.getText().trim();
        String correo = txtCorreo.getText().trim();
        String nombreUsuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();
        Rol rol = cmbRol.getValue();
        boolean estado = rbActivo.isSelected();

        int idEmpleado = insertarEmpleado(nombre, apellido, telefono, puesto);
        if (idEmpleado == 0) {
            System.out.println("Error al crear empleado");
            return;
        }

        boolean usuarioCreado = insertarUsuario(nombreUsuario, password, rol.getIdRol(), estado);
        if (usuarioCreado) {
            System.out.println("Usuario creado exitosamente");
            fnLimpiar();
        } else {
            System.out.println("Error al crear usuario");
        }
    }

    private int insertarEmpleado(String nombre, String apellido, String telefono, String puesto) {
        String sql = "INSERT INTO EMPLEADO (nombre, apellido1, numero_telefono, puesto) VALUES (?, ?, ?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, telefono);
            ps.setString(4, puesto);
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

    private boolean insertarUsuario(String nombreUsuario, String password, int idRol, boolean estado) {
        String sql = "INSERT INTO USUARIO (nombre_usuario, password_hash, id_rol, estado) VALUES (?, ?, ?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, password);
            ps.setInt(3, idRol);
            ps.setBoolean(4, estado);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertando usuario: " + e.getMessage());
            return false;
        }
    }

    private boolean validarCamposEmpleado() {
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            System.out.println("El nombre es obligatorio");
            return false;
        }
        if (txtApellido.getText() == null || txtApellido.getText().trim().isEmpty()) {
            System.out.println("El apellido es obligatorio");
            return false;
        }
        return true;
    }

    private boolean validarCamposUsuario() {
        if (!validarCamposEmpleado()) {
            return false;
        }
        if (txtCorreo.getText() == null || txtCorreo.getText().trim().isEmpty()) {
            System.out.println("El correo es obligatorio");
            return false;
        }
        if (txtUsuario.getText() == null || txtUsuario.getText().trim().isEmpty()) {
            System.out.println("El nombre de usuario es obligatorio");
            return false;
        }
        if (txtPassword.getText() == null || txtPassword.getText().length() < 8) {
            System.out.println("La contraseña debe tener al menos 8 caracteres");
            return false;
        }
        if (!txtPassword.getText().equals(txtConfirmarPassword.getText())) {
            System.out.println("Las contraseñas no coinciden");
            return false;
        }
        if (cmbRol.getValue() == null) {
            System.out.println("Debe seleccionar un rol");
            return false;
        }
        return true;
    }

    @FXML
    public void fnLimpiar() {
        txtNombre.clear();
        txtApellido.clear();
        txtCorreo.clear();
        txtTelefono.clear();
        txtPuesto.clear();
        txtUsuario.clear();
        txtPassword.clear();
        txtConfirmarPassword.clear();
        cmbRol.setValue(null);
        rbActivo.setSelected(true);
        System.out.println("Formulario limpiado");
    }

    @FXML
    public void fnLimpiar(ActionEvent event) {
        fnLimpiar();
    }

    @FXML
    public void fnVolverMenu(ActionEvent event) {
        appNavigator.volverMenu();
    }
}