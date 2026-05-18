package controllers;

import Data_base.CONEXION;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import model.Empleado;
import model.Puesto;
import utils.AppNavigator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Admin_empleados_controller {

    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colId;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colApellido1;
    @FXML private TableColumn<Empleado, String> colApellido2;
    @FXML private TableColumn<Empleado, String> colTelefono;
    @FXML private TableColumn<Empleado, String> colPuesto;
    @FXML private TableColumn<Empleado, Void> colAccion;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtIdEmpleado;
    @FXML private ComboBox<Puesto> cmbPuesto;
    @FXML private Label lblTotal;

    private CONEXION conexion = new CONEXION();
    private AppNavigator appNavigator = new AppNavigator();
    private ObservableList<Empleado> listaEmpleados = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarPuestos();
        cargarEmpleados();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdEmpleado())));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellido1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellido1()));
        colApellido2.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellido2() != null ? cellData.getValue().getApellido2() : ""));
        colTelefono.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroTelefono() != null ? cellData.getValue().getNumeroTelefono() : ""));
        colPuesto.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPuestoNombre() != null ? cellData.getValue().getPuestoNombre() : ""));

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox contenedor = new HBox(8, btnEditar, btnEliminar);

            {
                btnEditar.setStyle(
                        "-fx-background-color: #cdb08e; -fx-text-fill: #100e0a; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 8 14;"
                );
                btnEliminar.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 8 14;"
                );

                btnEditar.setOnAction(e -> {
                    Empleado emp = getTableView().getItems().get(getIndex());
                    fnEditarEmpleado(emp);
                });
                btnEliminar.setOnAction(e -> {
                    Empleado emp = getTableView().getItems().get(getIndex());
                    fnEliminarEmpleado(emp);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tablaEmpleados.setItems(listaEmpleados);
    }

    private void cargarPuestos() {
        ObservableList<Puesto> puestos = FXCollections.observableArrayList();
        puestos.add(new Puesto(0, "Todos los puestos"));
        String sql = "SELECT id_puesto, nombre FROM PUESTO ORDER BY nombre";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                puestos.add(new Puesto(rs.getInt("id_puesto"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando puestos: " + e.getMessage());
        }
        cmbPuesto.setItems(puestos);
        cmbPuesto.getSelectionModel().selectFirst();
    }

    private void cargarEmpleados() {
        listaEmpleados.clear();

        String sql = "SELECT e.id_empleado, e.nombre, e.apellido1, e.apellido2, e.numero_telefono, " +
                   "e.id_puesto, p.nombre as puesto_nombre " +
                   "FROM EMPLEADO e " +
                   "LEFT JOIN PUESTO p ON e.id_puesto = p.id_puesto " +
                   "ORDER BY e.nombre";

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Empleado emp = new Empleado();
                emp.setIdEmpleado(rs.getInt("id_empleado"));
                emp.setNombre(rs.getString("nombre"));
                emp.setApellido1(rs.getString("apellido1"));
                emp.setApellido2(rs.getString("apellido2"));
                emp.setNumeroTelefono(rs.getString("numero_telefono"));
                emp.setIdPuesto(rs.getObject("id_puesto") != null ? rs.getInt("id_puesto") : null);
                emp.setPuestoNombre(rs.getString("puesto_nombre"));
                listaEmpleados.add(emp);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar empleados: " + e.getMessage());
        }

        lblTotal.setText("Total: " + listaEmpleados.size() + " empleado(s)");
    }

    @FXML
    public void fnBuscar(ActionEvent event) {
        listaEmpleados.clear();

        String textoBusqueda = txtBuscar.getText().trim();
        String idTexto = txtIdEmpleado.getText().trim();
        Puesto puestoSeleccionado = cmbPuesto.getValue();

        StringBuilder sql = new StringBuilder(
            "SELECT e.id_empleado, e.nombre, e.apellido1, e.apellido2, e.numero_telefono, " +
            "e.id_puesto, p.nombre as puesto_nombre " +
            "FROM EMPLEADO e " +
            "LEFT JOIN PUESTO p ON e.id_puesto = p.id_puesto " +
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

        if (!idTexto.isEmpty()) {
            try {
                int id = Integer.parseInt(idTexto);
                sql.append(" AND e.id_empleado = ? ");
                parametros.add(id);
            } catch (NumberFormatException e) {
                // ignorar ID inválido
            }
        }

        if (puestoSeleccionado != null && puestoSeleccionado.getIdPuesto() > 0) {
            sql.append(" AND e.id_puesto = ? ");
            parametros.add(puestoSeleccionado.getIdPuesto());
        }

        sql.append(" ORDER BY e.nombre");

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Empleado emp = new Empleado();
                emp.setIdEmpleado(rs.getInt("id_empleado"));
                emp.setNombre(rs.getString("nombre"));
                emp.setApellido1(rs.getString("apellido1"));
                emp.setApellido2(rs.getString("apellido2"));
                emp.setNumeroTelefono(rs.getString("numero_telefono"));
                emp.setIdPuesto(rs.getObject("id_puesto") != null ? rs.getInt("id_puesto") : null);
                emp.setPuestoNombre(rs.getString("puesto_nombre"));
                listaEmpleados.add(emp);
            }
        } catch (SQLException e) {
            mostrarError("Error al buscar empleados: " + e.getMessage());
        }

        lblTotal.setText("Total: " + listaEmpleados.size() + " empleado(s)");
    }

    private void fnEditarEmpleado(Empleado empleado) {
        Dialog<Empleado> dialogo = new Dialog<>();
        dialogo.setTitle("Editar Empleado");
        dialogo.setHeaderText("Editando: " + empleado.getNombre() + " " + empleado.getApellido1());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        TextField txtNombre = new TextField(empleado.getNombre());
        TextField txtApellido1 = new TextField(empleado.getApellido1());
        TextField txtApellido2 = new TextField(empleado.getApellido2());
        TextField txtTelefono = new TextField(empleado.getNumeroTelefono());

        ComboBox<Puesto> cmbPuestoEdit = new ComboBox<>();
        ObservableList<Puesto> puestos = FXCollections.observableArrayList();
        String sql = "SELECT id_puesto, nombre FROM PUESTO ORDER BY nombre";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Puesto p = new Puesto(rs.getInt("id_puesto"), rs.getString("nombre"));
                puestos.add(p);
                if (empleado.getIdPuesto() != null && empleado.getIdPuesto() == rs.getInt("id_puesto")) {
                    cmbPuestoEdit.setValue(p);
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar datos: " + e.getMessage());
        }
        cmbPuestoEdit.setItems(puestos);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Apellido 1:"), 0, 1);
        grid.add(txtApellido1, 1, 1);
        grid.add(new Label("Apellido 2:"), 0, 2);
        grid.add(txtApellido2, 1, 2);
        grid.add(new Label("Teléfono:"), 0, 3);
        grid.add(txtTelefono, 1, 3);
        grid.add(new Label("Puesto:"), 0, 4);
        grid.add(cmbPuestoEdit, 1, 4);

        dialogo.getDialogPane().setContent(grid);

        dialogo.setResultConverter(dialogBtn -> {
            if (dialogBtn == btnGuardar) {
                empleado.setNombre(txtNombre.getText().trim());
                empleado.setApellido1(txtApellido1.getText().trim());
                empleado.setApellido2(txtApellido2.getText().trim());
                empleado.setNumeroTelefono(txtTelefono.getText().trim());
                if (cmbPuestoEdit.getValue() != null) {
                    empleado.setIdPuesto(cmbPuestoEdit.getValue().getIdPuesto());
                } else {
                    empleado.setIdPuesto(null);
                }
                return empleado;
            }
            return null;
        });

        dialogo.showAndWait().ifPresent(emp -> {
            String sqlUpd = "UPDATE EMPLEADO SET nombre=?, apellido1=?, apellido2=?, numero_telefono=?, id_puesto=? WHERE id_empleado=?";
            try (Connection conn = conexion.establecerconexio();
                 PreparedStatement ps = conn.prepareStatement(sqlUpd)) {
                ps.setString(1, emp.getNombre());
                ps.setString(2, emp.getApellido1());
                ps.setString(3, emp.getApellido2() != null && !emp.getApellido2().isEmpty() ? emp.getApellido2() : null);
                ps.setString(4, emp.getNumeroTelefono() != null && !emp.getNumeroTelefono().isEmpty() ? emp.getNumeroTelefono() : null);
                if (emp.getIdPuesto() != null) {
                    ps.setInt(5, emp.getIdPuesto());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                ps.setInt(6, emp.getIdEmpleado());
                ps.executeUpdate();
                mostrarInfo("Empleado actualizado exitosamente.");
                cargarEmpleados();
            } catch (SQLException e) {
                mostrarError("Error al actualizar: " + e.getMessage());
            }
        });
    }

    private void fnEliminarEmpleado(Empleado empleado) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Está seguro que desea eliminar al empleado \"" + empleado.getNombreCompleto() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);

        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        String sql = "DELETE FROM EMPLEADO WHERE id_empleado = ?";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empleado.getIdEmpleado());
            ps.executeUpdate();
            mostrarInfo("Empleado eliminado exitosamente.");
            cargarEmpleados();
        } catch (SQLException e) {
            mostrarError("Error al eliminar: " + e.getMessage());
        }
    }

    public void fnVolverMenu() {
        appNavigator.volverMenu();
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
}