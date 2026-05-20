package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.DetalleOrdenVenta;
import model.OrdenVenta;
import model.OrdenVentaEstado;
import utils.AppNavigator;
import model.OrdenVentaEstado;

import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import static utils.AlertHelper.*;

public class Confirmar_orden_controller {

    // ─── FXML ────────────────────────────────────────────────────────────────
    @FXML private Label lblInfoOrden;
    @FXML private TableView<DetalleOrdenVenta>          tblProductos;
    @FXML private TableColumn<DetalleOrdenVenta, String>     colProducto;
    @FXML private TableColumn<DetalleOrdenVenta, BigDecimal> colCantidad;
    @FXML private TableColumn<DetalleOrdenVenta, Double>     colPrecio;
    @FXML private TableColumn<DetalleOrdenVenta, Double>     colTotal;
    @FXML private Label lblSubtotal;
    @FXML private Label lblItbis;
    @FXML private Label lblTotal;
    @FXML private Button btnEditar;
    @FXML private Button btnCancelar;
    @FXML private Button btnEmitir;


    private final CONEXION     conexion     = new CONEXION();
    private final AppNavigator appNavigator = new AppNavigator();
    private static final NumberFormat FMT  =
            NumberFormat.getCurrencyInstance(new Locale("es", "DO"));



    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatosDeEstado();
    }

    private void configurarColumnas() {
        colProducto.setCellValueFactory(cellData -> cellData.getValue().nombreProductoProperty());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty());
        colPrecio.setCellValueFactory(cellData -> cellData.getValue().precioUnitarioProperty().asObject());
        colTotal.setCellValueFactory(cellData -> cellData.getValue().subtotalProperty().asObject());

        // Formatear precio unitario → RD$ 0,000.00
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : FMT.format(val));
                setStyle("-fx-alignment: CENTER_RIGHT;");
            }
        });

        // Formatear subtotal por línea
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : FMT.format(val));
                setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");
            }
        });

        // Cantidad centrada
        colCantidad.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : val.stripTrailingZeros().toPlainString());
                setStyle("-fx-alignment: CENTER;");
            }
        });
    }

    private void cargarDatosDeEstado() {
        OrdenVenta orden         = OrdenVentaEstado.ordenActual;
        List<DetalleOrdenVenta> detalles = OrdenVentaEstado.detalles;

        if (orden == null || detalles == null) {
            mostrarError("No hay datos de orden disponibles.");
            return;
        }

        // Header
        String nombreCliente   = OrdenVentaEstado.nombreCliente   != null ? OrdenVentaEstado.nombreCliente   : "—";
        String nombreFormaPago = OrdenVentaEstado.nombreFormaPago != null ? OrdenVentaEstado.nombreFormaPago : "—";
        lblInfoOrden.setText("Cliente: " + nombreCliente + "  ·  Forma de Pago: " + nombreFormaPago);

        // Tabla
        ObservableList<DetalleOrdenVenta> lista = FXCollections.observableArrayList(detalles);
        tblProductos.setItems(lista);

        // Totales
        double subtotal = orden.getSubtotal()   != null ? orden.getSubtotal()   : 0.0;
        double itbis    = orden.getItbis()       != null ? orden.getItbis()      : 0.0;
        double total    = orden.getMontoTotal()  != null ? orden.getMontoTotal() : 0.0;

        lblSubtotal.setText(FMT.format(subtotal));
        lblItbis.setText(FMT.format(itbis));
        lblTotal.setText(FMT.format(total));
    }



    @FXML
    public void fnEditarOrden(ActionEvent event) {
        Detalle_orden_venta_controller.setIdOrden(OrdenVentaEstado.idOrdenVenta);
        appNavigator.load("/view/Detalle_Venta.fxml");
    }

    @FXML
    public void fnCancelarOrden(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que desea cancelar la orden? Se perderán los datos ingresados.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Cancelar Orden");
        confirm.setHeaderText(null);

        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            OrdenVentaEstado.limpiar();
            appNavigator.volverMenu();
        }
    }


    @FXML
    public void fnEmitirOrden(ActionEvent event) {
        OrdenVenta orden         = OrdenVentaEstado.ordenActual;
        List<DetalleOrdenVenta> detalles = OrdenVentaEstado.detalles;

        if (orden == null || detalles == null || detalles.isEmpty()) {
            mostrarError("No hay datos válidos para emitir.");
            return;
        }

        String sinStock = verificarStockSuficiente(detalles);
        if (sinStock != null) {
            mostrarAdvertencia("No se puede emitir la orden. Los siguientes productos no tienen stock suficiente:\n\n" + sinStock);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Confirmar y registrar la orden de venta?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Emitir Orden");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        int idOrden = emitirEnBD(orden, detalles);

        if (idOrden > 0) {
            OrdenVentaEstado.limpiar();
            mostrarInfo("Orden #" + idOrden + " registrada exitosamente con estado PENDIENTE.");
            appNavigator.volverMenu();
        }
    }

    private int emitirEnBD(OrdenVenta orden, List<DetalleOrdenVenta> detalles) {
        Integer idEmpleado = OrdenVentaEstado.idEmpleado;

        String sqlOrden =
                "INSERT INTO ORDEN_VENTA " +
                        "(id_cliente, id_empleado, estado, fecha_orden, id_forma_pago, subtotal, itbis, monto_total, fecha_entrega) " +
                        "VALUES (?, ?, 'PENDIENTE', ?, ?, ?, ?, ?, ?)";

        String sqlDetalle =
                "INSERT INTO DETALLE_ORDEN_VENTA " +
                        "(id_orden_venta, id_producto, cantidad, precio_unitario, subtotal) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = conexion.establecerconexio()) {
            con.setAutoCommit(false);

            int idOrdenGenerado;


            try (PreparedStatement psOrden =
                         con.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {

                psOrden.setInt(1, orden.getIdCliente());
                if (idEmpleado != null) {
                    psOrden.setInt(2, idEmpleado);
                } else {
                    psOrden.setNull(2, Types.INTEGER);
                }
                psOrden.setDate(3, Date.valueOf(LocalDate.now()));
            
                if (orden.getIdFormaPago() != null) {
                    psOrden.setInt(4, orden.getIdFormaPago());
                } else {
                    psOrden.setNull(4, Types.INTEGER);
                }

                psOrden.setDouble(5, orden.getSubtotal()  != null ? orden.getSubtotal()  : 0.0);
                psOrden.setDouble(6, orden.getItbis()     != null ? orden.getItbis()     : 0.0);
                psOrden.setDouble(7, orden.getMontoTotal()!= null ? orden.getMontoTotal(): 0.0);

                if (orden.getFechaEntrega() != null) {
                    psOrden.setDate(8, orden.getFechaEntrega());
                } else {
                    psOrden.setNull(8, Types.DATE);
                }

                psOrden.executeUpdate();

                ResultSet rs = psOrden.getGeneratedKeys();
                if (!rs.next()) {
                    con.rollback();
                    mostrarError("No se pudo obtener el ID de la orden generada.");
                    return 0;
                }
                idOrdenGenerado = rs.getInt(1);
            }

            // 2. Insertar líneas de detalle en batch
            try (PreparedStatement psDet = con.prepareStatement(sqlDetalle)) {
                for (DetalleOrdenVenta d : detalles) {
                    psDet.setInt(1, idOrdenGenerado);
                    psDet.setInt(2, d.getIdProducto());
                    psDet.setBigDecimal(3, d.getCantidad());
                    psDet.setDouble(4, d.getPrecioUnitario());
                    psDet.setDouble(5, d.getSubtotal());
                    psDet.addBatch();
                }
                psDet.executeBatch();
            }

            String estadoEnvio = idEmpleado != null ? "ASIGNADO" : "PENDIENTE";

            Integer idDireccionCliente = null;
            String sqlDir = "SELECT id_direccion FROM CLIENTE WHERE id_cliente = ?";
            try (PreparedStatement psDir = con.prepareStatement(sqlDir)) {
                psDir.setInt(1, orden.getIdCliente());
                ResultSet rsDir = psDir.executeQuery();
                if (rsDir.next()) idDireccionCliente = rsDir.getInt("id_direccion");
            }

            String sqlEnvio = "INSERT INTO ENVIO (id_orden_venta, id_empleado_transportista, id_estado_envio, id_usuario_creacion, id_direccion_entrega) " +
                            "VALUES (?, ?, (SELECT id_estado_envio FROM ESTADO_ENVIO WHERE nombre = ?), ?, ?)";
            try (PreparedStatement psEnv = con.prepareStatement(sqlEnvio)) {
                psEnv.setInt(1, idOrdenGenerado);
                if (idEmpleado != null) {
                    psEnv.setInt(2, idEmpleado);
                } else {
                    psEnv.setNull(2, Types.INTEGER);
                }
                psEnv.setString(3, estadoEnvio);
                psEnv.setInt(4, utils.SesionUsuario.getIdUsuario());
                if (idDireccionCliente != null) {
                    psEnv.setInt(5, idDireccionCliente);
                } else {
                    psEnv.setNull(5, Types.INTEGER);
                }
                psEnv.executeUpdate();
            }

            con.commit();
            return idOrdenGenerado;

        } catch (SQLException e) {
            mostrarError("Error al registrar la orden: " + e.getMessage());
            return 0;
        }
    }

    private String verificarStockSuficiente(List<DetalleOrdenVenta> detalles) {
        StringBuilder faltantes = new StringBuilder();
        String sql = "SELECT COALESCE(stock_actual, 0) FROM INVENTARIO WHERE id_producto = ?";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (DetalleOrdenVenta d : detalles) {
                ps.setInt(1, d.getIdProducto());
                ResultSet rs = ps.executeQuery();
                double stock = rs.next() ? rs.getDouble(1) : 0;
                double solicitado = d.getCantidad().doubleValue();
                if (solicitado > stock) {
                    String nombre = d.getNombreProducto() != null ? d.getNombreProducto() : "Producto #" + d.getIdProducto();
                    faltantes.append("• ").append(nombre)
                            .append(" — disponible: ").append((int) stock)
                            .append(", solicitado: ").append(d.getCantidad().stripTrailingZeros().toPlainString())
                            .append("\n");
                }
            }
        } catch (SQLException e) {
            mostrarError("Error verificando stock: " + e.getMessage());
            return "Error al verificar inventario.";
        }
        return faltantes.length() > 0 ? faltantes.toString() : null;
    }

    public void fnVolvermenu() {
        appNavigator.volverMenu();
    }
}