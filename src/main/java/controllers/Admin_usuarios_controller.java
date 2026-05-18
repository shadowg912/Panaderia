package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Usuario;
import utils.AppNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Admin_usuarios_controller {

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colEmpleado;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEstado;
    @FXML private TableColumn<Usuario, Void> colAccion;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblTotal;

    private CONEXION conexion = new CONEXION();
    private AppNavigator appNavigator = new AppNavigator();
    private ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[]{
                    item.idUsuarioProperty(),
                    item.nombreUsuarioProperty(),
                    item.nombreEmpleadoProperty(),
                    item.nombreRolProperty(),
                    item.estadoProperty()
            }
    );

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarEstados();
        cargarUsuarios();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(cellData -> cellData.getValue().idUsuarioProperty().asObject());
        colUsuario.setCellValueFactory(cellData -> cellData.getValue().nombreUsuarioProperty());
        colEmpleado.setCellValueFactory(cellData -> cellData.getValue().nombreEmpleadoProperty());
        colRol.setCellValueFactory(cellData -> cellData.getValue().nombreRolProperty());

        colEstado.setCellValueFactory(cellData -> {
            String estadoTexto = cellData.getValue().isEstado() ? "Activo" : "Inactivo";
            return new javafx.beans.property.SimpleStringProperty(estadoTexto);
        });

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnCambiarEstado = new Button("Cambiar");

            {
                btnCambiarEstado.setStyle(
                        "-fx-background-color: #cdb08e; -fx-text-fill: #100e0a; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;"
                );
                btnCambiarEstado.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    fnCambiarEstado(usuario);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btnCambiarEstado);
            }
        });

        tablaUsuarios.setItems(listaUsuarios);
    }

    private void cargarEstados() {
        cmbEstado.setItems(FXCollections.observableArrayList("Todos", "Activos", "Inactivos"));
        cmbEstado.getSelectionModel().selectFirst();
    }

    private void cargarUsuarios() {
        listaUsuarios.clear();
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.estado, r.nombre_rol, " +
                   "e.id_empleado, e.nombre + ' ' + e.apellido1 + ISNULL(' ' + e.apellido2, '') as nombre_empleado " +
                   "FROM USUARIO u " +
                   "INNER JOIN ROL r ON u.id_rol = r.id_rol " +
                   "INNER JOIN EMPLEADO e ON u.id_empleado = e.id_empleado " +
                   "ORDER BY u.nombre_usuario";

        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNombreUsuario(rs.getString("nombre_usuario"));
                usuario.setEstado(rs.getBoolean("estado"));
                usuario.setNombreRol(rs.getString("nombre_rol"));
                usuario.setNombreEmpleado(rs.getString("nombre_empleado"));
                usuario.setIdEmpleado(rs.getInt("id_empleado"));
                listaUsuarios.add(usuario);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar datos: " + e.getMessage());
        }

        lblTotal.setText("Total: " + listaUsuarios.size() + " usuario(s)");
    }

    @FXML
    public void fnBuscar(ActionEvent event) {
        listaUsuarios.clear();

        String textoBusqueda = txtBuscar.getText().trim();
        String estadoFiltro = cmbEstado.getValue();

        StringBuilder sql = new StringBuilder(
            "SELECT u.id_usuario, u.nombre_usuario, u.estado, r.nombre_rol, " +
            "e.id_empleado, e.nombre + ' ' + e.apellido1 + ISNULL(' ' + e.apellido2, '') as nombre_empleado " +
            "FROM USUARIO u " +
            "INNER JOIN ROL r ON u.id_rol = r.id_rol " +
            "INNER JOIN EMPLEADO e ON u.id_empleado = e.id_empleado " +
            "WHERE 1=1 "
        );

        List<Object> parametros = new ArrayList<>();

        if (!textoBusqueda.isEmpty()) {
            sql.append(" AND (e.nombre LIKE ? OR e.apellido1 LIKE ? OR e.apellido2 LIKE ?) ");
            String likeTexto = "%" + textoBusqueda + "%";
            parametros.add(likeTexto);
            parametros.add(likeTexto);
            parametros.add(likeTexto);
        }

        if (estadoFiltro != null && !estadoFiltro.equals("Todos")) {
            if (estadoFiltro.equals("Activos")) {
                sql.append(" AND u.estado = 1 ");
            } else if (estadoFiltro.equals("Inactivos")) {
                sql.append(" AND u.estado = 0 ");
            }
        }

        sql.append(" ORDER BY u.nombre_usuario");

        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNombreUsuario(rs.getString("nombre_usuario"));
                usuario.setEstado(rs.getBoolean("estado"));
                usuario.setNombreRol(rs.getString("nombre_rol"));
                usuario.setNombreEmpleado(rs.getString("nombre_empleado"));
                usuario.setIdEmpleado(rs.getInt("id_empleado"));
                listaUsuarios.add(usuario);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar datos: " + e.getMessage());
        }

        lblTotal.setText("Total: " + listaUsuarios.size() + " usuario(s)");
    }

    private void fnCambiarEstado(Usuario usuario) {
        boolean nuevoEstado = !usuario.isEstado();
        String nombreAccion = nuevoEstado ? "activar" : "desactivar";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Está seguro que desea " + nombreAccion + " al usuario \"" + usuario.getNombreUsuario() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);

        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        String sql = "UPDATE USUARIO SET estado = ? WHERE id_usuario = ?";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, nuevoEstado);
            ps.setInt(2, usuario.getIdUsuario());
            ps.executeUpdate();

            usuario.setEstado(nuevoEstado);
            tablaUsuarios.refresh();

            String mensaje = nuevoEstado ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";
            mostrarInfo(mensaje);
        } catch (SQLException e) {
            mostrarError("Error al cambiar el estado: " + e.getMessage());
        }
    }

    private void mostrarInfo(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    public void fnVolverMenu() {
        appNavigator.volverMenu();
    }
}