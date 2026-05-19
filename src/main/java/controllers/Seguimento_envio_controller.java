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
import model.EnvioResumen;
import utils.AppNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import static utils.AlertHelper.*;

public class Seguimento_envio_controller {

    @FXML private TableView<EnvioResumen> tablaEnvio;
    @FXML private TableColumn<EnvioResumen, Integer> colId;
    @FXML private TableColumn<EnvioResumen, Integer> colOrden;
    @FXML private TableColumn<EnvioResumen, String> colCliente;
    @FXML private TableColumn<EnvioResumen, String> colTransportista;
    @FXML private TableColumn<EnvioResumen, String> colEstado;
    @FXML private TableColumn<EnvioResumen, String> colSeguimiento;
    @FXML private TableColumn<EnvioResumen, String> colFechaEst;
    @FXML private TableColumn<EnvioResumen, Void> colAccion;
    @FXML private TextField txtIdOrden;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<Empleado> cmbTransportista;
    @FXML private Label lblTotal;

    private CONEXION conexion = new CONEXION();
    private AppNavigator appNavigator = new AppNavigator();
    private ObservableList<EnvioResumen> listaEnvio = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[]{
                    item.idEnvioProperty(), item.idOrdenProperty(), item.clienteProperty(),
                    item.transportistaProperty(), item.estadoProperty(),
                    item.numeroSeguimientoProperty(), item.fechaEstimadaProperty()
            }
    );

    private static final Map<String, List<String>> TRANSICIONES = new LinkedHashMap<>();
    static {
        TRANSICIONES.put("PENDIENTE", Arrays.asList("ASIGNADO", "CANCELADO"));
        TRANSICIONES.put("ASIGNADO", Arrays.asList("EN_RUTA", "PENDIENTE", "CANCELADO"));
        TRANSICIONES.put("EN_RUTA", Arrays.asList("ENTREGADO", "DEVUELTO", "CANCELADO"));
        TRANSICIONES.put("ENTREGADO", Collections.emptyList());
        TRANSICIONES.put("DEVUELTO", Collections.emptyList());
        TRANSICIONES.put("CANCELADO", Collections.emptyList());
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarFiltros();
        cargarEnvios();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(c -> c.getValue().idEnvioProperty().asObject());
        colOrden.setCellValueFactory(c -> c.getValue().idOrdenProperty().asObject());
        colCliente.setCellValueFactory(c -> c.getValue().clienteProperty());
        colTransportista.setCellValueFactory(c -> c.getValue().transportistaProperty());
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colSeguimiento.setCellValueFactory(c -> c.getValue().numeroSeguimientoProperty());
        colFechaEst.setCellValueFactory(c -> c.getValue().fechaEstimadaProperty());

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("Ver");
            private final ComboBox<String> cmbEst = new ComboBox<>();
            private final HBox cont = new HBox(6);

            {
                btnVer.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: #100e0a; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 6 12;");
                cmbEst.setPromptText("Cambiar");
                cmbEst.setPrefWidth(120);

                btnVer.setOnAction(e -> {
                    EnvioResumen env = getTableView().getItems().get(getIndex());
                    fnVerEnvio(env);
                });

                cmbEst.setOnAction(e -> {
                    EnvioResumen env = getTableView().getItems().get(getIndex());
                    String nuevoEstado = cmbEst.getValue();
                    if (nuevoEstado != null) {
                        fnCambiarEstado(env, nuevoEstado);
                    }
                    cmbEst.setValue(null);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EnvioResumen env = getTableView().getItems().get(getIndex());
                    cmbEst.getItems().setAll(TRANSICIONES.getOrDefault(env.getEstado(), Collections.emptyList()));
                    cont.getChildren().setAll(btnVer, cmbEst);
                    setGraphic(cont);
                }
            }
        });

        tablaEnvio.setItems(listaEnvio);
    }

    private void cargarFiltros() {
        ObservableList<String> estados = FXCollections.observableArrayList("Todos");
        String sqlEst = "SELECT nombre FROM ESTADO_ENVIO ORDER BY id_estado_envio";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sqlEst);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) estados.add(rs.getString("nombre"));
        } catch (SQLException e) { mostrarError("Error al cargar estados: " + e.getMessage()); }
        cmbEstado.setItems(estados);
        cmbEstado.getSelectionModel().selectFirst();

        ObservableList<Empleado> trans = FXCollections.observableArrayList();
        trans.add(new Empleado(0, "Todos", ""));
        String sqlTr = "SELECT e.id_empleado, e.nombre, e.apellido1 FROM EMPLEADO e " +
                     "INNER JOIN PUESTO p ON e.id_puesto = p.id_puesto " +
                     "WHERE LOWER(p.nombre) LIKE '%repartidor%' ORDER BY e.nombre";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sqlTr);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) trans.add(new Empleado(rs.getInt("id_empleado"), rs.getString("nombre"), rs.getString("apellido1")));
        } catch (SQLException e) { mostrarError("Error al cargar transportistas: " + e.getMessage()); }
        cmbTransportista.setItems(trans);
        cmbTransportista.getSelectionModel().selectFirst();
    }

    private void cargarEnvios() {
        listaEnvio.clear();
        String filtroEstado = cmbEstado.getValue();
        Empleado filtroTrans = cmbTransportista.getValue();
        String idTexto = txtIdOrden.getText().trim();

        StringBuilder sql = new StringBuilder(
            "SELECT e.id_envio, e.id_orden_venta, e.numero_seguimiento, e.fecha_entrega_estimada, e.fecha_asignacion, e.fecha_salida, e.fecha_entrega_real, " +
            "e.id_estado_envio, e.id_empleado_transportista, " +
            "ee.nombre as estado_nombre, " +
            "emp.nombre + ' ' + emp.apellido1 as transportista_nombre, " +
            "c.razon_social as cliente_nombre " +
            "FROM ENVIO e " +
            "INNER JOIN ESTADO_ENVIO ee ON e.id_estado_envio = ee.id_estado_envio " +
            "LEFT JOIN EMPLEADO emp ON e.id_empleado_transportista = emp.id_empleado " +
            "INNER JOIN ORDEN_VENTA ov ON e.id_orden_venta = ov.id_orden_venta " +
            "INNER JOIN CLIENTE c ON ov.id_cliente = c.id_cliente WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (!idTexto.isEmpty()) {
            try { params.add(Integer.parseInt(idTexto)); sql.append(" AND e.id_orden_venta = ? "); }
            catch (NumberFormatException ignored) {}
        }
        if (filtroEstado != null && !filtroEstado.equals("Todos")) {
            sql.append(" AND ee.nombre = ? "); params.add(filtroEstado);
        }
        if (filtroTrans != null && filtroTrans.getIdEmpleado() > 0) {
            sql.append(" AND e.id_empleado_transportista = ? "); params.add(filtroTrans.getIdEmpleado());
        }
        sql.append(" ORDER BY e.id_envio DESC");

        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                EnvioResumen env = new EnvioResumen();
                env.setIdEnvio(rs.getInt("id_envio"));
                env.setIdOrden(rs.getInt("id_orden_venta"));
                env.setCliente(rs.getString("cliente_nombre"));
                env.setTransportista(rs.getString("transportista_nombre") != null ? rs.getString("transportista_nombre") : "—");
                env.setEstado(rs.getString("estado_nombre"));
                env.setNumeroSeguimiento(rs.getString("numero_seguimiento") != null ? rs.getString("numero_seguimiento") : "—");
                env.setIdEstadoEnvio(rs.getInt("id_estado_envio"));
                env.setIdTransportista(rs.getObject("id_empleado_transportista") != null ? rs.getInt("id_empleado_transportista") : null);
                env.setFechaAsignacion(rs.getDate("fecha_asignacion"));
                env.setFechaSalida(rs.getDate("fecha_salida"));
                env.setFechaEntregaReal(rs.getDate("fecha_entrega_real"));
                java.sql.Date fe = rs.getDate("fecha_entrega_estimada");
                env.setFechaEstimada(fe != null ? fe.toString() : "—");
                listaEnvio.add(env);
            }
        } catch (SQLException e) { mostrarError("Error al cargar envíos: " + e.getMessage()); }
        lblTotal.setText("Total: " + listaEnvio.size() + " envío(s)");
    }

    @FXML
    public void fnBuscar(ActionEvent e) { cargarEnvios(); }

    private void fnVerEnvio(EnvioResumen env) {
        StringBuilder info = new StringBuilder();
        info.append("Envío #").append(env.getIdEnvio()).append(" | Orden #").append(env.getIdOrden()).append("\n");
        info.append("Cliente: ").append(env.getCliente()).append("\n");
        info.append("Estado: ").append(env.getEstado()).append("\n");
        info.append("Transportista: ").append(env.getTransportista()).append("\n");
        info.append("N° Seguimiento: ").append(env.getNumeroSeguimiento()).append("\n");
        info.append("Fecha estimada: ").append(env.getFechaEstimada()).append("\n\n");

        String sqlDir = "SELECT d.calle + ' #' + CAST(d.numero AS varchar) + ', ' + s.nombre + ', ' + c.nombre as direccion " +
                      "FROM DIRECCION d INNER JOIN SECTOR s ON d.id_sector = s.id_sector " +
                      "INNER JOIN CIUDAD c ON s.id_ciudad = c.id_ciudad WHERE d.id_direccion = ?";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sqlDir)) {
            ps.setInt(1, env.getIdEnvio());
            ResultSet rs = ps.executeQuery();
            // Simplified - fetch from ENVIO table if stored
        } catch (SQLException ex) { /* ignore */ }

        info.append("\n--- Historial de Estados ---\n");
        String sqlHist = "SELECT h.fecha_evento, ee.nombre as estado, h.observaciones " +
                       "FROM HISTORICO_ENVIO h INNER JOIN ESTADO_ENVIO ee ON h.id_estado_envio = ee.id_estado_envio " +
                       "WHERE h.id_envio = ? ORDER BY h.fecha_evento";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sqlHist)) {
            ps.setInt(1, env.getIdEnvio());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                info.append(rs.getDate("fecha_evento")).append(" — ").append(rs.getString("estado"));
                String obs = rs.getString("observaciones");
                if (obs != null && !obs.isEmpty()) info.append(" (").append(obs).append(")");
                info.append("\n");
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        info.append("\n--- Reasignaciones de Transportista ---\n");
        String sqlTrans = "SELECT ht.fecha_cambio, emp.nombre + ' ' + emp.apellido1 as nuevo_transportista, ht.motivo " +
                        "FROM HISTORICO_TRANSPORTISTA_ENVIO ht " +
                        "INNER JOIN EMPLEADO emp ON ht.id_empleado_transportista_nuevo = emp.id_empleado " +
                        "WHERE ht.id_envio = ? ORDER BY ht.fecha_cambio";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sqlTrans)) {
            ps.setInt(1, env.getIdEnvio());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                info.append(rs.getDate("fecha_cambio")).append(" → ").append(rs.getString("nuevo_transportista"));
                String motivo = rs.getString("motivo");
                if (motivo != null && !motivo.isEmpty()) info.append(" (").append(motivo).append(")");
                info.append("\n");
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        Alert a = new Alert(Alert.AlertType.INFORMATION, info.toString(), ButtonType.OK);
        a.setTitle("Envío #" + env.getIdEnvio());
        a.setHeaderText("Detalle de Envío");
        a.getDialogPane().setPrefWidth(600);
        a.showAndWait();
    }

    private void fnCambiarEstado(EnvioResumen env, String nuevoEstado) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Cambiar estado del envío #" + env.getIdEnvio() + " a \"" + nuevoEstado + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        boolean success = false;
        if (nuevoEstado.equals("ASIGNADO")) {
            success = fnAsignarEnvio(env);
        } else {
            String sql = "UPDATE ENVIO SET id_estado_envio = (SELECT id_estado_envio FROM ESTADO_ENVIO WHERE nombre = ?) WHERE id_envio = ?";
            try (Connection con = conexion.establecerconexio();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, env.getIdEnvio());
                ps.executeUpdate();
                success = true;
            } catch (SQLException ex) {
                mostrarError("Error: " + ex.getMessage());
            }
        }
        if (success) {
            sincronizarOrdenEnvio(env.getIdOrden(), nuevoEstado);
            cargarEnvios();
            mostrarInfo("Estado actualizado a: " + nuevoEstado);
        }
    }

    private void sincronizarOrdenEnvio(int idOrden, String estadoEnvio) {
        String estadoOrden = null;
        if ("ENTREGADO".equals(estadoEnvio)) estadoOrden = "FACTURADA";
        else if ("CANCELADO".equals(estadoEnvio)) estadoOrden = "CANCELADA";
        else return;

        String sql = "UPDATE ORDEN_VENTA SET estado = ? WHERE id_orden_venta = ?";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estadoOrden);
            ps.setInt(2, idOrden);
            ps.executeUpdate();
        } catch (SQLException e) {
            mostrarError("Error sincronizando orden: " + e.getMessage());
        }
    }

    private boolean fnAsignarEnvio(EnvioResumen env) {
        ObservableList<Empleado> transportistas = FXCollections.observableArrayList();
        String sqlTr = "SELECT e.id_empleado, e.nombre, e.apellido1 FROM EMPLEADO e " +
                     "INNER JOIN PUESTO p ON e.id_puesto = p.id_puesto " +
                     "WHERE LOWER(p.nombre) LIKE '%repartidor%' ORDER BY e.nombre";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sqlTr);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) transportistas.add(new Empleado(rs.getInt("id_empleado"), rs.getString("nombre"), rs.getString("apellido1")));
        } catch (SQLException e) { mostrarError("Error al cargar transportistas: " + e.getMessage()); }

        ComboBox<Empleado> cmbTrans = new ComboBox<>(transportistas);
        cmbTrans.setPromptText("Seleccionar...");
        cmbTrans.setPrefWidth(300);

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10);
        gp.add(new Label("Transportista:"), 0, 0);
        gp.add(cmbTrans, 1, 0);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Asignar Transportista");
        dialog.setHeaderText("Asignar transportista al envío #" + env.getIdEnvio());
        dialog.getDialogPane().setContent(gp);
        dialog.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return false;
        Empleado emp = cmbTrans.getValue();
        if (emp == null) { mostrarAdvertencia("Seleccione un transportista."); return false; }

        String sql = "UPDATE ENVIO SET id_empleado_transportista = ?, id_estado_envio = (SELECT id_estado_envio FROM ESTADO_ENVIO WHERE nombre = 'ASIGNADO') WHERE id_envio = ?";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, emp.getIdEmpleado());
            ps.setInt(2, env.getIdEnvio());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            mostrarError("Error al asignar: " + e.getMessage());
            return false;
        }
    }

    public void fnVolverMenu() { appNavigator.volverMenu(); }

}