package controllers;

import Data_base.CONEXION;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.DetalleOrdenProduccion;
import model.OrdenProduccionEstado;
import model.Producto;
import utils.AppNavigator;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import static utils.AlertHelper.*;

public class Seleccionar_productos_produccion_controller {

    @FXML private Label lblInfo;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private Spinner<Integer> spCantidad;
    @FXML private TableView<DetalleOrdenProduccion> tablaProductos;
    @FXML private TableColumn<DetalleOrdenProduccion, String> colProducto;
    @FXML private TableColumn<DetalleOrdenProduccion, BigDecimal> colCantidad;
    @FXML private TableColumn<DetalleOrdenProduccion, Void> colAccion;
    @FXML private Label lblTotal;

    private CONEXION conexion = new CONEXION();
    private ObservableList<DetalleOrdenProduccion> listaDetalles = FXCollections.observableArrayList();
    private Set<Integer> productosAgregados = new HashSet<>();

    @FXML
    public void initialize() {
        if (OrdenProduccionEstado.idOrdenProduccion == 0) {
            mostrarError("No hay orden activa.");
            AppNavigator.cargarDashboard();
            return;
        }
        lblInfo.setText("Orden #" + OrdenProduccionEstado.idOrdenProduccion + " — " + OrdenProduccionEstado.nombreEmpleado + " — " + OrdenProduccionEstado.fecha);

        cmbProducto.setItems(cargarProductos());
        spCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99999, 1));

        colProducto.setCellValueFactory(cd -> cd.getValue().nombreProductoProperty());
        colCantidad.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCantidadPlanificada()));

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");
            {
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-border-color: #ef4444; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 12;");
                btnEliminar.setOnAction(e -> {
                    DetalleOrdenProduccion dp = getTableView().getItems().get(getIndex());
                    eliminarDetalle(dp);
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });

        tablaProductos.setItems(listaDetalles);
        cargarDetallesGuardados();
    }

    private ObservableList<Producto> cargarProductos() {
        ObservableList<Producto> lista = FXCollections.observableArrayList();
        String sql = "SELECT id_producto, nombre FROM PRODUCTO WHERE tipo_producto = 'PRODUCTO_TERMINADO' ORDER BY nombre";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Producto(rs.getInt("id_producto"), rs.getString("nombre"), null, null, null));
        } catch (SQLException e) { mostrarError("Error: " + e.getMessage()); }
        return lista;
    }

    private void cargarDetallesGuardados() {
        String sql = "SELECT d.id_detalle, d.id_producto, p.nombre, d.cantidad_planificada " +
                "FROM ORDEN_PRODUCCION_DETALLE d INNER JOIN PRODUCTO p ON d.id_producto = p.id_producto " +
                "WHERE d.id_orden_produccion = ? ORDER BY p.nombre";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, OrdenProduccionEstado.idOrdenProduccion);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DetalleOrdenProduccion dp = new DetalleOrdenProduccion();
                dp.setIdDetalle(rs.getInt("id_detalle"));
                dp.setIdProducto(rs.getInt("id_producto"));
                dp.setNombreProducto(rs.getString("nombre"));
                dp.setCantidadPlanificada(rs.getBigDecimal("cantidad_planificada"));
                listaDetalles.add(dp);
                productosAgregados.add(dp.getIdProducto());
            }
        } catch (SQLException e) { mostrarError("Error: " + e.getMessage()); }
        actualizarTotal();
    }

    @FXML
    public void fnAgregar() {
        Producto prod = cmbProducto.getValue();
        if (prod == null) { mostrarAdvertencia("Seleccione un producto."); return; }
        Integer val = spCantidad.getValue();
        if (val == null || val <= 0) { mostrarAdvertencia("Cantidad inválida."); return; }
        BigDecimal cant = BigDecimal.valueOf(val);

        if (productosAgregados.contains(prod.getIdProducto())) {
            mostrarAdvertencia("El producto \"" + prod.getNombre() + "\" ya fue agregado.");
            return;
        }

        String sql = "INSERT INTO ORDEN_PRODUCCION_DETALLE (id_orden_produccion, id_producto, cantidad_planificada) VALUES (?, ?, ?)";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, OrdenProduccionEstado.idOrdenProduccion);
            ps.setInt(2, prod.getIdProducto());
            ps.setBigDecimal(3, cant);
            ps.executeUpdate();

            DetalleOrdenProduccion dp = new DetalleOrdenProduccion(prod.getIdProducto(), prod.getNombre(), cant);
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) dp.setIdDetalle(rs.getInt(1));
            dp.setIdOrdenProduccion(OrdenProduccionEstado.idOrdenProduccion);
            listaDetalles.add(dp);
            productosAgregados.add(prod.getIdProducto());
            cmbProducto.setValue(null);
            spCantidad.getValueFactory().setValue(1);
            actualizarTotal();
        } catch (SQLException e) { mostrarError("Error: " + e.getMessage()); }
    }

    private void eliminarDetalle(DetalleOrdenProduccion dp) {
        String sql = "DELETE FROM ORDEN_PRODUCCION_DETALLE WHERE id_detalle = ?";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dp.getIdDetalle());
            ps.executeUpdate();
            listaDetalles.remove(dp);
            productosAgregados.remove(dp.getIdProducto());
            actualizarTotal();
        } catch (SQLException e) { mostrarError("Error: " + e.getMessage()); }
    }

    private void actualizarTotal() {
        lblTotal.setText("Total: " + listaDetalles.size() + " productos");
    }

    @FXML
    public void fnFinalizar() {
        if (listaDetalles.isEmpty()) { mostrarAdvertencia("Debe agregar al menos un producto."); return; }
        mostrarInfo("Orden #" + OrdenProduccionEstado.idOrdenProduccion + " creada con " + listaDetalles.size() + " producto(s).\n\n"
                + "Podrá marcarla como completada desde la pantalla 'Ver Órdenes'.");
        OrdenProduccionEstado.limpiar();
        AppNavigator.cargarDashboard();
    }

    @FXML
    public void fnVolver() {
        AppNavigator.load("/view/Orden_produccion_simple.fxml");
    }
}
