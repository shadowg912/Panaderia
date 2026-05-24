package controllers;

import Data_base.CONEXION;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import utils.AppNavigator;
import utils.SesionUsuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import static utils.AlertHelper.*;

public class Ver_ordenes_produccion_controller {

    @FXML private TableView<OrdenProdRow> tablaPendientes;
    @FXML private TableView<OrdenProdRow> tablaOrden;
    @FXML private ComboBox<String> cmbFiltro;
    @FXML private Label lblTotal;

    @FXML private TableColumn<OrdenProdRow, Integer> colIdP;
    @FXML private TableColumn<OrdenProdRow, String> colProductoP;
    @FXML private TableColumn<OrdenProdRow, String> colEmpleadoP;
    @FXML private TableColumn<OrdenProdRow, String> colCantidadP;
    @FXML private TableColumn<OrdenProdRow, String> colFechaP;
    @FXML private TableColumn<OrdenProdRow, String> colEstadoP;
    @FXML private TableColumn<OrdenProdRow, Void> colAccionP;

    @FXML private TableColumn<OrdenProdRow, Integer> colId;
    @FXML private TableColumn<OrdenProdRow, String> colProducto;
    @FXML private TableColumn<OrdenProdRow, String> colEmpleado;
    @FXML private TableColumn<OrdenProdRow, String> colCantidad;
    @FXML private TableColumn<OrdenProdRow, String> colFecha;
    @FXML private TableColumn<OrdenProdRow, String> colEstado;
    @FXML private TableColumn<OrdenProdRow, Void> colAccion;

    private CONEXION conexion = new CONEXION();
    private ObservableList<OrdenProdRow> listaPendientes = FXCollections.observableArrayList();
    private ObservableList<OrdenProdRow> listaOrden = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarFiltros();
        cargarDatos();
    }

    private void configurarColumnas() {
        colIdP.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colProductoP.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProducto()));
        colEmpleadoP.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEmpleado()));
        colCantidadP.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCantidad()));
        colFechaP.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFecha()));
        colEstadoP.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEstado()));
        colAccionP.setCellFactory(col -> accionCellFactory(false));

        colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colProducto.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProducto()));
        colEmpleado.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEmpleado()));
        colCantidad.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCantidad()));
        colFecha.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFecha()));
        colEstado.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEstado()));
        colAccion.setCellFactory(col -> accionCellFactory(true));

        tablaPendientes.setItems(listaPendientes);
        tablaOrden.setItems(listaOrden);
    }

    private TableCell<OrdenProdRow, Void> accionCellFactory(boolean ocultarSiCompletada) {
        return new TableCell<>() {
            private final ComboBox<String> cmbEstado = new ComboBox<>();
            private final Button btnDetalle = new Button("Ver Detalle");
            private final HBox cont = new HBox(6);

            {
                cmbEstado.getItems().addAll("En Proceso", "Completada", "Cancelar");
                cmbEstado.setOnAction(e -> {
                    OrdenProdRow row = getTableView().getItems().get(getIndex());
                    if (cmbEstado.getValue() != null) cambiarEstado(row, cmbEstado.getValue());
                    cmbEstado.setValue(null);
                });
                btnDetalle.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: #100e0a; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 6 12;");
                btnDetalle.setOnAction(e -> {
                    OrdenProdRow row = getTableView().getItems().get(getIndex());
                    verDetalle(row);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    OrdenProdRow row = getTableView().getItems().get(getIndex());
                    cont.getChildren().clear();
                    if (row.isMultiple()) cont.getChildren().add(btnDetalle);
                    boolean ocultar = ocultarSiCompletada && "Completada".equals(row.getEstado());
                    if (!ocultar) cont.getChildren().add(cmbEstado);
                    setGraphic(cont.getChildren().isEmpty() ? null : cont);
                }
            }
        };
    }

    private void cargarFiltros() {
        cmbFiltro.setItems(FXCollections.observableArrayList("Todos", "En Proceso", "Completada"));
        cmbFiltro.getSelectionModel().selectFirst();
    }

    private void cargarDatos() {
        listaPendientes.clear();
        listaOrden.clear();

        String sqlPendientes = "SELECT op.id_orden_produccion, op.id_producto, op.cantidad_planificada, " +
                "e.nombre + ' ' + e.apellido1 as empleado, op.fecha_produccion, op.estado, p.nombre as producto, " +
                "(SELECT COUNT(*) FROM ORDEN_PRODUCCION_DETALLE d WHERE d.id_orden_produccion = op.id_orden_produccion) as num_detalles " +
                "FROM ORDEN_PRODUCCION op " +
                "LEFT JOIN PRODUCTO p ON op.id_producto = p.id_producto " +
                "INNER JOIN EMPLEADO e ON op.id_empleado = e.id_empleado " +
                "WHERE op.estado = 'Pendiente' ORDER BY op.fecha_produccion DESC";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sqlPendientes);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cargarRow(rs, listaPendientes);
        } catch (SQLException e) { mostrarError("Error: " + e.getMessage()); }

        String filtro = cmbFiltro.getValue();
        StringBuilder sqlOtros = new StringBuilder(
                "SELECT op.id_orden_produccion, op.id_producto, op.cantidad_planificada, " +
                "e.nombre + ' ' + e.apellido1 as empleado, op.fecha_produccion, op.estado, p.nombre as producto, " +
                "(SELECT COUNT(*) FROM ORDEN_PRODUCCION_DETALLE d WHERE d.id_orden_produccion = op.id_orden_produccion) as num_detalles " +
                "FROM ORDEN_PRODUCCION op " +
                "LEFT JOIN PRODUCTO p ON op.id_producto = p.id_producto " +
                "INNER JOIN EMPLEADO e ON op.id_empleado = e.id_empleado " +
                "WHERE op.estado != 'Pendiente' ");
        List<Object> params = new ArrayList<>();
        if (filtro != null && !"Todos".equals(filtro)) { sqlOtros.append(" AND op.estado = ? "); params.add(filtro); }
        sqlOtros.append(" ORDER BY op.fecha_produccion DESC");
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sqlOtros.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) cargarRow(rs, listaOrden);
        } catch (SQLException e) { mostrarError("Error: " + e.getMessage()); }

        lblTotal.setText("Total: " + listaOrden.size() + " órdenes");
    }

    private void cargarRow(ResultSet rs, ObservableList<OrdenProdRow> lista) throws SQLException {
        int id = rs.getInt("id_orden_produccion");
        int numDetalles = rs.getInt("num_detalles");
        String producto;
        String cantidad;
        if (rs.getObject("id_producto") != null) {
            producto = rs.getString("producto");
            cantidad = rs.getBigDecimal("cantidad_planificada") != null ? rs.getBigDecimal("cantidad_planificada").toString() : "";
        } else {
            producto = "Múltiple (" + numDetalles + " productos)";
            cantidad = "—";
        }
        lista.add(new OrdenProdRow(
                id, producto, rs.getString("empleado"), cantidad,
                rs.getDate("fecha_produccion") != null ? rs.getDate("fecha_produccion").toString() : "",
                rs.getString("estado"),
                numDetalles > 0 || rs.getObject("id_producto") == null
        ));
    }

    private void verDetalle(OrdenProdRow row) {
        StringBuilder sb = new StringBuilder();
        sb.append("Orden #").append(row.getId()).append("\n");
        sb.append("Empleado: ").append(row.getEmpleado()).append("\n");
        sb.append("Fecha: ").append(row.getFecha()).append("\n");
        sb.append("Estado: ").append(row.getEstado()).append("\n");
        sb.append("\nProductos:\n");

        String sql = "SELECT p.nombre, d.cantidad_planificada FROM ORDEN_PRODUCCION_DETALLE d " +
                "INNER JOIN PRODUCTO p ON d.id_producto = p.id_producto WHERE d.id_orden_produccion = ? ORDER BY p.nombre";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, row.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sb.append("  • ").append(rs.getString("nombre"))
                  .append(" x ").append(rs.getBigDecimal("cantidad_planificada").stripTrailingZeros().toPlainString())
                  .append("\n");
            }
        } catch (SQLException e) { mostrarError("Error: " + e.getMessage()); }

        Alert info = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
        info.setTitle("Detalle de Orden #" + row.getId());
        info.setHeaderText(null);
        info.showAndWait();
    }

    private void cambiarEstado(OrdenProdRow row, String nuevoEstado) {
        if (nuevoEstado == null) return;
        if ("Cancelar".equals(nuevoEstado)) {
            try (Connection conn = conexion.establecerconexio();
                 PreparedStatement ps = conn.prepareStatement("UPDATE ORDEN_PRODUCCION SET estado = 'Cancelada' WHERE id_orden_produccion = ?")) {
                ps.setInt(1, row.getId()); ps.executeUpdate();
                listaPendientes.remove(row); listaOrden.remove(row);
                mostrarInfo("Orden #" + row.getId() + " cancelada.");
            } catch (SQLException e) { mostrarError("Error: " + e.getMessage()); }
            return;
        }
        if ("Completada".equals(nuevoEstado)) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Completar Orden #" + row.getId());
            dialog.setHeaderText("Ingrese una observación para la producción (opcional):");
            dialog.setContentText("Observación:");
            String obs = dialog.showAndWait().orElse(null);
            if (obs == null) return;

            try (Connection conn = conexion.establecerconexio()) {
                conn.setAutoCommit(false);
                try {
                    completarOrden(conn, row, obs);
                    conn.commit();
                    mostrarInfo("Orden #" + row.getId() + " completada. Stock actualizado e ingredientes descontados.");
                    cargarDatos();
                } catch (SQLException e) { conn.rollback(); mostrarError("Error: " + e.getMessage()); }
            } catch (SQLException e) { mostrarError("Error de conexión: " + e.getMessage()); }
            return;
        }
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement("UPDATE ORDEN_PRODUCCION SET estado = ? WHERE id_orden_produccion = ?")) {
            ps.setString(1, nuevoEstado); ps.setInt(2, row.getId()); ps.executeUpdate();
            cargarDatos(); mostrarInfo("Estado actualizado a: " + nuevoEstado);
        } catch (SQLException e) { mostrarError("Error: " + e.getMessage()); }
    }

    private void completarOrden(Connection conn, OrdenProdRow row, String obs) throws SQLException {
        int idTipoEntrada = obtenerTipoMovimiento(conn, "ENTRADA");
        int idTipoSalida = obtenerTipoMovimiento(conn, "SALIDA");
        int idUsuario = SesionUsuario.getIdUsuario();

        int idProduccion;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO PRODUCCION (fecha_inicio, fecha_fin, estado, id_usuario, observacion) VALUES (GETDATE(), GETDATE(), 'FINALIZADA', ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUsuario);
            if (obs.trim().isEmpty()) ps.setNull(2, Types.VARCHAR);
            else ps.setString(2, obs.trim());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) idProduccion = rs.getInt(1);
            else throw new SQLException("No se pudo obtener id de produccion");
        }

        boolean esMultiple = row.isMultiple();

        if (esMultiple) {
            String sql = "SELECT id_producto, cantidad_planificada FROM ORDEN_PRODUCCION_DETALLE WHERE id_orden_produccion = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, row.getId());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int idProd = rs.getInt("id_producto");
                    double cantidad = rs.getBigDecimal("cantidad_planificada").doubleValue();
                    insertarDetalleProduccion(conn, idProduccion, idProd, cantidad);
                    registrarEntradaStock(conn, idProd, cantidad, idUsuario, idTipoEntrada);
                    consumirIngredientes(conn, row.getId(), idProd, cantidad, idUsuario, idTipoSalida);
                }
            }
        } else {
            int idProd = obtenerIdProducto(conn, row.getId());
            double cantidad = obtenerCantidad(conn, row.getId());
            if (idProd > 0 && cantidad > 0) {
                insertarDetalleProduccion(conn, idProduccion, idProd, cantidad);
                registrarEntradaStock(conn, idProd, cantidad, idUsuario, idTipoEntrada);
                consumirIngredientes(conn, row.getId(), idProd, cantidad, idUsuario, idTipoSalida);
            }
        }

        try (PreparedStatement ps = conn.prepareStatement("UPDATE ORDEN_PRODUCCION SET estado = 'Completada' WHERE id_orden_produccion = ?")) {
            ps.setInt(1, row.getId()); ps.executeUpdate();
        }
    }

    private void insertarDetalleProduccion(Connection conn, int idProduccion, int idProducto, double cantidad) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO DETALLE_PRODUCCION (id_produccion, id_producto, cantidad_producida) VALUES (?, ?, ?)")) {
            ps.setInt(1, idProduccion);
            ps.setInt(2, idProducto);
            ps.setDouble(3, cantidad);
            ps.executeUpdate();
        }
    }

    private void registrarEntradaStock(Connection conn, int idProducto, double cantidad, int idUsuario, int idTipo) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO MOVIMIENTO_INVENTARIO (id_producto, cantidad, id_usuario, fecha, id_tipo_movimiento) VALUES (?, ?, ?, GETDATE(), ?)")) {
            ps.setInt(1, idProducto); ps.setDouble(2, cantidad); ps.setInt(3, idUsuario); ps.setInt(4, idTipo); ps.executeUpdate();
        }
        String merge = "MERGE INVENTARIO AS target USING (SELECT ? AS id_producto) AS source ON target.id_producto = source.id_producto " +
                "WHEN MATCHED THEN UPDATE SET stock_actual = stock_actual + ?, fecha_actualizacion = GETDATE() " +
                "WHEN NOT MATCHED THEN INSERT (id_producto, stock_actual, fecha_actualizacion) VALUES (?, ?, GETDATE());";
        try (PreparedStatement ps = conn.prepareStatement(merge)) {
            ps.setInt(1, idProducto); ps.setDouble(2, cantidad); ps.setInt(3, idProducto); ps.setDouble(4, cantidad); ps.executeUpdate();
        }
    }

    private void consumirIngredientes(Connection conn, int idOrden, int idProducto, double cantidadProducir, int idUsuario, int idTipoSalida) throws SQLException {
        String sqlReceta = "SELECT rd.id_producto_ingrediente, rd.cantidad FROM RECETA_DETALLE rd WHERE rd.id_producto_final = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlReceta)) {
            ps.setInt(1, idProducto);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int idIng = rs.getInt("id_producto_ingrediente");
                double cantConsumir = rs.getDouble("cantidad") * cantidadProducir;

                try (PreparedStatement psMov = conn.prepareStatement(
                    "INSERT INTO MOVIMIENTO_INVENTARIO (id_producto, cantidad, id_usuario, fecha, id_tipo_movimiento) VALUES (?, ?, ?, GETDATE(), ?)")) {
                    psMov.setInt(1, idIng); psMov.setDouble(2, -cantConsumir); psMov.setInt(3, idUsuario); psMov.setInt(4, idTipoSalida); psMov.executeUpdate();
                }
                try (PreparedStatement psInv = conn.prepareStatement(
                    "UPDATE INVENTARIO SET stock_actual = stock_actual - ?, fecha_actualizacion = GETDATE() WHERE id_producto = ?")) {
                    psInv.setDouble(1, cantConsumir); psInv.setInt(2, idIng); psInv.executeUpdate();
                }
            }
        }
    }

    private int obtenerIdProducto(Connection conn, int idOrden) throws SQLException {
        String sql = "SELECT id_producto FROM ORDEN_PRODUCCION WHERE id_orden_produccion = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { int v = rs.getInt("id_producto"); if (!rs.wasNull()) return v; }
        }
        return 0;
    }

    private double obtenerCantidad(Connection conn, int idOrden) throws SQLException {
        String sql = "SELECT cantidad_planificada FROM ORDEN_PRODUCCION WHERE id_orden_produccion = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal("cantidad_planificada") != null ? rs.getBigDecimal("cantidad_planificada").doubleValue() : 0;
        }
        return 0;
    }

    private int obtenerTipoMovimiento(Connection conn, String nombre) throws SQLException {
        String sql = "SELECT id_tipo FROM TIPO_MOVIMIENTO WHERE nombre = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_tipo");
        }
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO TIPO_MOVIMIENTO (nombre, naturaleza) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre); ps.setString(2, nombre); ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("No se pudo obtener/crear tipo de movimiento '" + nombre + "'");
    }

    @FXML public void fnFiltrar() { cargarDatos(); }
    @FXML public void fnVolver() { AppNavigator.cargarDashboard(); }

    public static class OrdenProdRow {
        private final int id;
        private final String producto, empleado, cantidad, fecha, estado;
        private final boolean multiple;

        public OrdenProdRow(int id, String producto, String empleado, String cantidad, String fecha, String estado, boolean multiple) {
            this.id = id; this.producto = producto; this.empleado = empleado; this.cantidad = cantidad; this.fecha = fecha; this.estado = estado; this.multiple = multiple;
        }
        public int getId() { return id; }
        public String getProducto() { return producto; }
        public String getEmpleado() { return empleado; }
        public String getCantidad() { return cantidad; }
        public String getFecha() { return fecha; }
        public String getEstado() { return estado; }
        public boolean isMultiple() { return multiple; }
    }
}
