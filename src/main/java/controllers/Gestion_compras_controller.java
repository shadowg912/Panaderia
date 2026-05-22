package controllers;

import Data_base.CONEXION;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.*;
import utils.AppNavigator;
import utils.SesionUsuario;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import static utils.AlertHelper.*;

public class Gestion_compras_controller implements Initializable {

    @FXML private TableView<CompraMaterial> tablaCompras;
    @FXML private TableColumn<CompraMaterial, Integer> colId;
    @FXML private TableColumn<CompraMaterial, String> colProveedor;
    @FXML private TableColumn<CompraMaterial, LocalDate> colFecha;
    @FXML private TableColumn<CompraMaterial, String> colFormaPago;
    @FXML private TableColumn<CompraMaterial, Double> colMonto;
    @FXML private TableColumn<CompraMaterial, String> colEstado;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private ComboBox<Proveedor> cmbFiltroProveedor;
    @FXML private Label lblTotalRegistros;
    @FXML private Button btnEntregar;
    @FXML private Button btnCancelar;

    private CONEXION conexion = new CONEXION();
    private ObservableList<CompraMaterial> listaCompras = FXCollections.observableArrayList();
    private ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnas();
        cargarFiltros();
        cargarDatos();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cd.getValue().getIdCompraMaterial()));
        colProveedor.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getNombreProveedor()));
        colFecha.setCellValueFactory(cd -> {
            LocalDate f = cd.getValue().getFecha();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(f);
        });
        colFormaPago.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getNombreFormaPago()));
        colMonto.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cd.getValue().getMontoTotal()));
        colEstado.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getEstado()));

        tablaCompras.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean p = sel != null;
            btnEntregar.setDisable(!p);
            btnCancelar.setDisable(!p);
        });
        btnEntregar.setDisable(true);
        btnCancelar.setDisable(true);
    }

    private void cargarFiltros() {
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos", "PENDIENTE", "ENTREGADA", "CANCELADA"
        ));
        cmbFiltroEstado.getSelectionModel().selectFirst();

        listaProveedores.clear();
        String sql = "SELECT id_proveedor, nombre FROM PROVEEDOR WHERE estado = 1 ORDER BY nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setIdProveedor(rs.getInt("id_proveedor"));
                p.setNombre(rs.getString("nombre"));
                listaProveedores.add(p);
            }
        } catch (Exception e) { mostrarError("Error cargando proveedores: " + e.getMessage()); }
        cmbFiltroProveedor.setItems(listaProveedores);
    }

    private void cargarDatos() {
        listaCompras.clear();
        StringBuilder sql = new StringBuilder(
            "SELECT cm.id_compra_material, cm.fecha, cm.estado, " +
            "p.id_proveedor, p.nombre as proveedor_nombre, " +
            "fp.id_forma_pago, fp.nombre as forma_pago_nombre, " +
            "COALESCE((SELECT SUM(dc.monto_total) FROM DETALLE_COMPRA dc WHERE dc.id_compra_material = cm.id_compra_material), 0) as monto_total " +
            "FROM COMPRA_MATERIAL cm " +
            "INNER JOIN PROVEEDOR p ON cm.id_proveedor = p.id_proveedor " +
            "INNER JOIN FORMA_PAGO fp ON cm.id_forma_pago = fp.id_forma_pago WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        String filtroEstado = cmbFiltroEstado.getValue();
        if (filtroEstado != null && !filtroEstado.equals("Todos")) {
            sql.append(" AND cm.estado = ? ");
            params.add(filtroEstado);
        }
        Proveedor filtroProv = cmbFiltroProveedor.getValue();
        if (filtroProv != null) {
            sql.append(" AND cm.id_proveedor = ? ");
            params.add(filtroProv.getIdProveedor());
        }
        sql.append(" ORDER BY cm.fecha DESC");

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CompraMaterial cm = new CompraMaterial();
                cm.setIdCompraMaterial(rs.getInt("id_compra_material"));
                java.sql.Date fecha = rs.getDate("fecha");
                if (fecha != null) cm.setFecha(fecha.toLocalDate());
                cm.setEstado(rs.getString("estado"));
                cm.setIdProveedor(rs.getInt("id_proveedor"));
                cm.setNombreProveedor(rs.getString("proveedor_nombre"));
                cm.setIdFormaPago(rs.getInt("id_forma_pago"));
                cm.setNombreFormaPago(rs.getString("forma_pago_nombre"));
                cm.setMontoTotal(rs.getDouble("monto_total"));
                listaCompras.add(cm);
            }
        } catch (Exception e) { mostrarError("Error cargando compras: " + e.getMessage()); }
        tablaCompras.setItems(listaCompras);
        lblTotalRegistros.setText("Total: " + listaCompras.size() + " compra" + (listaCompras.size() != 1 ? "s" : ""));
    }

    public void fnFiltrar(ActionEvent event) { cargarDatos(); }

    @FXML
    public void fnEntregar(ActionEvent event) {
        CompraMaterial compra = tablaCompras.getSelectionModel().getSelectedItem();
        if (compra == null) { mostrarAdvertencia("Seleccione una compra."); return; }
        if (!"PENDIENTE".equals(compra.getEstado())) {
            mostrarAdvertencia("Solo se puede entregar compras en estado PENDIENTE.");
            return;
        }

        boolean confirm = mostrarConfirmacion("¿Registrar entrada de inventario para la compra #" + compra.getIdCompraMaterial()
                + "?\nLos productos serán añadidos al stock automáticamente.");
        if (!confirm) return;

        try (Connection c = conexion.establecerconexio()) {
            c.setAutoCommit(false);
            try {
                cambiarEstadoCompra(c, compra.getIdCompraMaterial(), "ENTREGADA");
                registrarMovimientosStock(c, compra.getIdCompraMaterial());
                c.commit();
                mostrarInfo("Compra #" + compra.getIdCompraMaterial() + " entregada y stock actualizado.");
                cargarDatos();
            } catch (SQLException e) {
                c.rollback();
                mostrarError("Error al registrar entrada: " + e.getMessage());
            }
        } catch (Exception e) {
            mostrarError("Error de conexión: " + e.getMessage());
        }
    }

    @FXML
    public void fnCancelar(ActionEvent event) {
        CompraMaterial compra = tablaCompras.getSelectionModel().getSelectedItem();
        if (compra == null) { mostrarAdvertencia("Seleccione una compra."); return; }
        if ("CANCELADA".equals(compra.getEstado())) {
            mostrarAdvertencia("La compra ya está cancelada.");
            return;
        }
        boolean confirm = mostrarConfirmacion("¿Cancelar la compra #" + compra.getIdCompraMaterial() + "?");
        if (!confirm) return;

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement("UPDATE COMPRA_MATERIAL SET estado = 'CANCELADA' WHERE id_compra_material = ?")) {
            ps.setInt(1, compra.getIdCompraMaterial());
            ps.executeUpdate();
            mostrarInfo("Compra #" + compra.getIdCompraMaterial() + " cancelada.");
            cargarDatos();
        } catch (SQLException e) {
            mostrarError("Error al cancelar compra: " + e.getMessage());
        }
    }

    private void cambiarEstadoCompra(Connection c, int idCompra, String nuevoEstado) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE COMPRA_MATERIAL SET estado = ? WHERE id_compra_material = ?")) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idCompra);
            ps.executeUpdate();
        }
    }

    private void registrarMovimientosStock(Connection c, int idCompra) throws SQLException {
        int idTipoCompra = obtenerTipoMovimientoCompra(c);
        int idUsuario = SesionUsuario.getIdUsuario();

        String sqlDetalles = "SELECT dc.id_producto, p.nombre, dc.cantidad " +
                           "FROM DETALLE_COMPRA dc " +
                           "INNER JOIN PRODUCTO p ON dc.id_producto = p.id_producto " +
                           "WHERE dc.id_compra_material = ?";
        try (PreparedStatement psDet = c.prepareStatement(sqlDetalles)) {
            psDet.setInt(1, idCompra);
            ResultSet rs = psDet.executeQuery();
            while (rs.next()) {
                int idProducto = rs.getInt("id_producto");
                double cantidad = rs.getDouble("cantidad");

                try (PreparedStatement psMov = c.prepareStatement(
                    "INSERT INTO MOVIMIENTO_INVENTARIO (id_producto, cantidad, id_usuario, fecha, id_tipo_movimiento) VALUES (?, ?, ?, GETDATE(), ?)")) {
                    psMov.setInt(1, idProducto);
                    psMov.setDouble(2, cantidad);
                    psMov.setInt(3, idUsuario);
                    psMov.setInt(4, idTipoCompra);
                    psMov.executeUpdate();
                }

                try (PreparedStatement psInv = c.prepareStatement(
                    "MERGE INTO INVENTARIO AS target " +
                    "USING (SELECT ? AS id_producto, ? AS cantidad) AS source " +
                    "ON target.id_producto = source.id_producto " +
                    "WHEN MATCHED THEN UPDATE SET stock_actual = stock_actual + source.cantidad, fecha_actualizacion = GETDATE() " +
                    "WHEN NOT MATCHED THEN INSERT (id_producto, stock_actual, fecha_actualizacion) VALUES (source.id_producto, source.cantidad, GETDATE());")) {
                    psInv.setInt(1, idProducto);
                    psInv.setDouble(2, cantidad);
                    psInv.executeUpdate();
                }
            }
        }
    }

    private int obtenerTipoMovimientoCompra(Connection c) throws SQLException {
        String sql = "SELECT id_tipo FROM TIPO_MOVIMIENTO WHERE nombre = 'ENTRADA'";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("id_tipo");
        }
        String sqlInsert = "INSERT INTO TIPO_MOVIMIENTO (nombre, naturaleza) VALUES ('ENTRADA', 'ENTRADA')";
        try (PreparedStatement ps = c.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("No se pudo obtener/crear tipo de movimiento 'ENTRADA'");
    }

    public void fnVolverMenu(ActionEvent event) { AppNavigator.cargarDashboard(); }

    private boolean mostrarConfirmacion(String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait().filter(r -> r == javafx.scene.control.ButtonType.OK).isPresent();
    }
}
