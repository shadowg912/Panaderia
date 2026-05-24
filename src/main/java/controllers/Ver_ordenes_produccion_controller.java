package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.OrdenProduccion;
import utils.AppNavigator;
import utils.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import static utils.AlertHelper.*;

public class Ver_ordenes_produccion_controller {

    @FXML private TableView<OrdenProduccion> tablaPendientes;
    @FXML private TableView<OrdenProduccion> tablaOrden;
    @FXML private ComboBox<String> cmbFiltro;
    @FXML private Label lblTotal;

    @FXML private TableColumn<OrdenProduccion, Integer> colIdP;
    @FXML private TableColumn<OrdenProduccion, String> colProductoP;
    @FXML private TableColumn<OrdenProduccion, String> colEmpleadoP;
    @FXML private TableColumn<OrdenProduccion, String> colCantidadP;
    @FXML private TableColumn<OrdenProduccion, String> colFechaP;
    @FXML private TableColumn<OrdenProduccion, String> colEstadoP;
    @FXML private TableColumn<OrdenProduccion, Void> colAccionP;

    @FXML private TableColumn<OrdenProduccion, Integer> colId;
    @FXML private TableColumn<OrdenProduccion, String> colProducto;
    @FXML private TableColumn<OrdenProduccion, String> colEmpleado;
    @FXML private TableColumn<OrdenProduccion, String> colCantidad;
    @FXML private TableColumn<OrdenProduccion, String> colFecha;
    @FXML private TableColumn<OrdenProduccion, String> colEstado;
    @FXML private TableColumn<OrdenProduccion, Void> colAccion;

    private CONEXION conexion = new CONEXION();
    private ObservableList<OrdenProduccion> listaPendientes = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[]{
                    item.idOrdenProduccionProperty(),
                    item.nombreProductoProperty(),
                    item.nombreEmpleadoProperty(),
                    item.cantidadTextoProperty(),
                    item.fechaTextoProperty(),
                    item.estadoProperty()
            }
    );
    private ObservableList<OrdenProduccion> listaOrden = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[]{
                    item.idOrdenProduccionProperty(),
                    item.nombreProductoProperty(),
                    item.nombreEmpleadoProperty(),
                    item.cantidadTextoProperty(),
                    item.fechaTextoProperty(),
                    item.estadoProperty()
            }
    );

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarFiltros();
        cargarDatos();
    }

    private void configurarColumnas() {
        colIdP.setCellValueFactory(cell -> cell.getValue().idOrdenProduccionProperty().asObject());
        colProductoP.setCellValueFactory(cell -> cell.getValue().nombreProductoProperty());
        colEmpleadoP.setCellValueFactory(cell -> cell.getValue().nombreEmpleadoProperty());
        colCantidadP.setCellValueFactory(cell -> cell.getValue().cantidadTextoProperty());
        colFechaP.setCellValueFactory(cell -> cell.getValue().fechaTextoProperty());
        colEstadoP.setCellValueFactory(cell -> cell.getValue().estadoProperty());

        colAccionP.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cmbEstado = new ComboBox<>();

            {
                cmbEstado.getItems().addAll("En Proceso", "Completada", "Cancelar");
                cmbEstado.setOnAction(e -> {
                    OrdenProduccion orden = getTableView().getItems().get(getIndex());
                    String nuevoEstado = cmbEstado.getValue();
                    if (nuevoEstado != null) {
                        cambiarEstado(orden, nuevoEstado);
                    }
                    cmbEstado.setValue(null);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : cmbEstado);
            }
        });

        colId.setCellValueFactory(cell -> cell.getValue().idOrdenProduccionProperty().asObject());
        colProducto.setCellValueFactory(cell -> cell.getValue().nombreProductoProperty());
        colEmpleado.setCellValueFactory(cell -> cell.getValue().nombreEmpleadoProperty());
        colCantidad.setCellValueFactory(cell -> cell.getValue().cantidadTextoProperty());
        colFecha.setCellValueFactory(cell -> cell.getValue().fechaTextoProperty());
        colEstado.setCellValueFactory(cell -> cell.getValue().estadoProperty());

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cmbEstado = new ComboBox<>();

            {
                cmbEstado.getItems().addAll("Pendiente", "En Proceso", "Completada");
                cmbEstado.setOnAction(e -> {
                    OrdenProduccion orden = getTableView().getItems().get(getIndex());
                    String nuevoEstado = cmbEstado.getValue();
                    if (nuevoEstado != null) {
                        cambiarEstado(orden, nuevoEstado);
                    }
                    cmbEstado.setValue(null);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    OrdenProduccion orden = getTableView().getItems().get(getIndex());
                    setGraphic("Completada".equals(orden.getEstado()) ? null : cmbEstado);
                }
            }
        });

        tablaPendientes.setItems(listaPendientes);
        tablaOrden.setItems(listaOrden);
    }

    private void cargarFiltros() {
        cmbFiltro.setItems(FXCollections.observableArrayList("Todos", "En Proceso", "Completada"));
        cmbFiltro.getSelectionModel().selectFirst();
    }

    private void cargarDatos() {
        listaPendientes.clear();
        listaOrden.clear();

        String sqlPendientes = "SELECT op.id_orden_produccion, op.id_producto, p.nombre as producto, " +
                "e.nombre + ' ' + e.apellido1 as empleado, op.cantidad_planificada, " +
                "op.fecha_produccion, op.estado " +
                "FROM ORDEN_PRODUCCION op " +
                "INNER JOIN PRODUCTO p ON op.id_producto = p.id_producto " +
                "INNER JOIN EMPLEADO e ON op.id_empleado = e.id_empleado " +
                "WHERE op.estado = 'Pendiente' " +
                "ORDER BY op.fecha_produccion DESC";

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sqlPendientes);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OrdenProduccion orden = new OrdenProduccion();
                orden.setIdOrdenProduccion(rs.getInt("id_orden_produccion"));
                orden.setIdProducto(rs.getInt("id_producto"));
                orden.setNombreProducto(rs.getString("producto"));
                orden.setNombreEmpleado(rs.getString("empleado"));
                orden.setCantidadPlanificada(rs.getBigDecimal("cantidad_planificada") != null ? rs.getBigDecimal("cantidad_planificada").doubleValue() : 0);
                orden.setCantidadTexto(rs.getBigDecimal("cantidad_planificada") != null ? rs.getBigDecimal("cantidad_planificada").toString() : "");
                orden.setFechaTexto(rs.getDate("fecha_produccion") != null ? rs.getDate("fecha_produccion").toString() : "");
                orden.setEstado(rs.getString("estado"));
                listaPendientes.add(orden);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar órdenes: " + e.getMessage());
        }

        String filtro = cmbFiltro.getValue();
        StringBuilder sqlOtros = new StringBuilder(
                "SELECT op.id_orden_produccion, op.id_producto, p.nombre as producto, " +
                "e.nombre + ' ' + e.apellido1 as empleado, op.cantidad_planificada, " +
                "op.fecha_produccion, op.estado " +
                "FROM ORDEN_PRODUCCION op " +
                "INNER JOIN PRODUCTO p ON op.id_producto = p.id_producto " +
                "INNER JOIN EMPLEADO e ON op.id_empleado = e.id_empleado " +
                "WHERE op.estado != 'Pendiente' "
        );

        List<Object> params = new ArrayList<>();
        if (filtro != null && !filtro.equals("Todos")) {
            sqlOtros.append(" AND op.estado = ? ");
            params.add(filtro);
        }
        sqlOtros.append(" ORDER BY op.fecha_produccion DESC");

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sqlOtros.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrdenProduccion orden = new OrdenProduccion();
                orden.setIdOrdenProduccion(rs.getInt("id_orden_produccion"));
                orden.setIdProducto(rs.getInt("id_producto"));
                orden.setNombreProducto(rs.getString("producto"));
                orden.setNombreEmpleado(rs.getString("empleado"));
                orden.setCantidadPlanificada(rs.getBigDecimal("cantidad_planificada") != null ? rs.getBigDecimal("cantidad_planificada").doubleValue() : 0);
                orden.setCantidadTexto(rs.getBigDecimal("cantidad_planificada") != null ? rs.getBigDecimal("cantidad_planificada").toString() : "");
                orden.setFechaTexto(rs.getDate("fecha_produccion") != null ? rs.getDate("fecha_produccion").toString() : "");
                orden.setEstado(rs.getString("estado"));
                listaOrden.add(orden);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar órdenes: " + e.getMessage());
        }

        lblTotal.setText("Total: " + listaOrden.size() + " órdenes");
    }

    private void cambiarEstado(OrdenProduccion orden, String nuevoEstado) {
        if (nuevoEstado == null) return;

        if ("Cancelar".equals(nuevoEstado)) {
            String sql = "UPDATE ORDEN_PRODUCCION SET estado = ? WHERE id_orden_produccion = ?";
            try (Connection conn = conexion.establecerconexio();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, orden.getIdOrdenProduccion());
                ps.executeUpdate();
                listaPendientes.remove(orden);
                listaOrden.remove(orden);
                mostrarInfo("Orden #" + orden.getIdOrdenProduccion() + " cancelada.");
            } catch (SQLException e) {
                mostrarError("Error al cancelar orden: " + e.getMessage());
            }
            return;
        }

        if ("Completada".equals(nuevoEstado)) {
            try (Connection conn = conexion.establecerconexio()) {
                conn.setAutoCommit(false);
                try {
                    actualizarEstadoOrden(conn, orden.getIdOrdenProduccion(), nuevoEstado);
                    registrarProduccionStock(conn, orden);
                    conn.commit();
                    orden.setEstado(nuevoEstado);
                    mostrarInfo("Orden #" + orden.getIdOrdenProduccion() + " completada. Stock actualizado.");
                    cargarDatos();
                } catch (SQLException e) {
                    conn.rollback();
                    mostrarError("Error al completar orden: " + e.getMessage());
                }
            } catch (SQLException e) {
                mostrarError("Error de conexión: " + e.getMessage());
            }
            return;
        }

        String sql = "UPDATE ORDEN_PRODUCCION SET estado = ? WHERE id_orden_produccion = ?";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, orden.getIdOrdenProduccion());
            ps.executeUpdate();
            orden.setEstado(nuevoEstado);
            cargarDatos();
            mostrarInfo("Estado actualizado a: " + nuevoEstado);
        } catch (SQLException e) {
            mostrarError("Error al cambiar estado: " + e.getMessage());
        }
    }

    private void actualizarEstadoOrden(Connection conn, int idOrden, String estado) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE ORDEN_PRODUCCION SET estado = ? WHERE id_orden_produccion = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idOrden);
            ps.executeUpdate();
        }
    }

    private void registrarProduccionStock(Connection conn, OrdenProduccion orden) throws SQLException {
        int idTipoProduccion = obtenerTipoMovimientoProduccion(conn);
        int idUsuario = SesionUsuario.getIdUsuario();
        int idProducto = orden.getIdProducto();
        double cantidad = orden.getCantidadPlanificada();

        if (idProducto <= 0 || cantidad <= 0) return;

        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO MOVIMIENTO_INVENTARIO (id_producto, cantidad, id_usuario, fecha, id_tipo_movimiento) VALUES (?, ?, ?, GETDATE(), ?)")) {
            ps.setInt(1, idProducto);
            ps.setDouble(2, cantidad);
            ps.setInt(3, idUsuario);
            ps.setInt(4, idTipoProduccion);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE INVENTARIO SET stock_actual = stock_actual + ?, fecha_actualizacion = GETDATE() WHERE id_producto = ?")) {
            ps.setDouble(1, cantidad);
            ps.setInt(2, idProducto);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                try (PreparedStatement psIns = conn.prepareStatement(
                    "INSERT INTO INVENTARIO (id_producto, stock_actual, fecha_actualizacion) VALUES (?, ?, GETDATE())")) {
                    psIns.setInt(1, idProducto);
                    psIns.setDouble(2, cantidad);
                    psIns.executeUpdate();
                }
            }
        }
    }

    private int obtenerTipoMovimientoProduccion(Connection conn) throws SQLException {
        String sql = "SELECT id_tipo FROM TIPO_MOVIMIENTO WHERE nombre = 'ENTRADA'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("id_tipo");
        }
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO TIPO_MOVIMIENTO (nombre, naturaleza) VALUES ('ENTRADA', 'ENTRADA')",
            Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("No se pudo obtener/crear tipo de movimiento 'ENTRADA'");
    }

    @FXML
    public void fnFiltrar() {
        cargarDatos();
    }

    @FXML
    public void fnVolver() {
        AppNavigator.cargarDashboard();
    }


}