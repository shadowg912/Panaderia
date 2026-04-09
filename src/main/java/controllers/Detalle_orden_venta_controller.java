package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.*;
import utils.AppNavigator;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Detalle_orden_venta_controller implements Initializable {

    @FXML private Label lblInfoOrden;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtPrecioUnitario;
    @FXML private Button btnAgregarProducto;
    @FXML private TableView<DetalleOrdenVenta> tblDetalle;
    @FXML private TableColumn<DetalleOrdenVenta, String> colProducto;
    @FXML private TableColumn<DetalleOrdenVenta, BigDecimal> colCantidad;
    @FXML private TableColumn<DetalleOrdenVenta, Double> colPrecio;
    @FXML private TableColumn<DetalleOrdenVenta, Double> colSubtotal;
    @FXML private Label lblSubtotal;
    @FXML private Label lblItbis;
    @FXML private Label lblTotal;
    @FXML private Button btnVolver;
    @FXML private Button btnConfirmarOrden;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<Producto> Productos = FXCollections.observableArrayList();
    ObservableList<DetalleOrdenVenta> Detalles = FXCollections.observableArrayList();
    Map<Integer, DetalleOrdenVenta> productosAgregados = new HashMap<>();
    int idOrdenActual = 0;
    double subtotalAcumulado = 0;
    double itbisAcumulado = 0;
    double totalAcumulado = 0;

    public static void setIdOrden(int idOrden) {
        idOrdenEstatico = idOrden;
    }

    private static int idOrdenEstatico = 0;

    public ObservableList cargarProductos() {
        String sql = "SELECT id_producto, nombre, precio_unitario FROM PRODUCTO ORDER BY nombre";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                Productos.add(p);
            }
        } catch (Exception e) {
            System.out.println("Error cargando productos: " + e.getMessage());
        }
        return Productos;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idOrdenActual = idOrdenEstatico;
        lblInfoOrden.setText("Orden #" + idOrdenActual);
        cmbProducto.setItems(cargarProductos());

        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        tblDetalle.setItems(Detalles);

        cmbProducto.setOnAction(e -> cargarPrecioProducto());
    }

    private void cargarPrecioProducto() {
        Producto p = cmbProducto.getValue();
        if (p != null) {
            txtPrecioUnitario.setText("RD$ " + p.getPrecioUnitario());
        }
    }

    @FXML
    public void fnAgregarProducto(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        Producto producto = cmbProducto.getValue();
        int idProducto = producto.getIdProducto();

        if (productosAgregados.containsKey(idProducto)) {
            System.out.println("Este producto ya fue agregado a la orden");
            return;
        }

        BigDecimal cantidad = new BigDecimal(txtCantidad.getText().trim());
        double precioUnitario = producto.getPrecioUnitario().doubleValue();
        double subtotal = cantidad.doubleValue() * precioUnitario;

        DetalleOrdenVenta detalle = new DetalleOrdenVenta();
        detalle.setIdOrdenVenta(idOrdenActual);
        detalle.setIdProducto(idProducto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setSubtotal(subtotal);
        detalle.setNombreProducto(producto.getNombre());

        if (insertarDetalle(detalle)) {
            Detalles.add(detalle);
            productosAgregados.put(idProducto, detalle);
            actualizarTotales();
            limpiarCampos();
            System.out.println("Producto agregado: " + producto.getNombre());
        }
    }

    private boolean insertarDetalle(DetalleOrdenVenta detalle) {
        String sql = "INSERT INTO DETALLE_ORDEN_VENTA (id_orden_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdOrdenVenta());
            ps.setInt(2, detalle.getIdProducto());
            ps.setBigDecimal(3, detalle.getCantidad());
            ps.setDouble(4, detalle.getPrecioUnitario());
            ps.setDouble(5, detalle.getSubtotal());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error insertando detalle: " + e.getMessage());
            return false;
        }
    }

    private void actualizarTotales() {
        subtotalAcumulado = 0;
        for (DetalleOrdenVenta d : Detalles) {
            subtotalAcumulado += d.getSubtotal();
        }
        itbisAcumulado = subtotalAcumulado * 0.18;
        totalAcumulado = subtotalAcumulado + itbisAcumulado;

        lblSubtotal.setText(String.format("RD$ %.2f", subtotalAcumulado));
        lblItbis.setText(String.format("RD$ %.2f", itbisAcumulado));
        lblTotal.setText(String.format("RD$ %.2f", totalAcumulado));
    }

    private boolean validarCampos() {
        if (cmbProducto.getValue() == null) {
            System.out.println("Debe seleccionar un producto");
            return false;
        }
        if (txtCantidad.getText() == null || txtCantidad.getText().trim().isEmpty()) {
            System.out.println("Debe ingresar una cantidad");
            return false;
        }
        try {
            BigDecimal cantidad = new BigDecimal(txtCantidad.getText().trim());
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("La cantidad debe ser mayor a 0");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("La cantidad debe ser un número válido");
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        cmbProducto.setValue(null);
        txtCantidad.clear();
        txtPrecioUnitario.clear();
    }

    @FXML
    public void fnVolver(ActionEvent event) {
        appNavigator.volverMenu();
    }


    @FXML
    public void fnConfirmarOrden(ActionEvent event) {
        if (Detalles.isEmpty()) {
            System.out.println("Debe agregar al menos un producto");
            return;
        }

        actualizarTotalesOrden();

        // Llenar el estado global antes de navegar
        OrdenVenta ordenEstado = new OrdenVenta();
        ordenEstado.setIdOrdenVenta(idOrdenActual);
        ordenEstado.setSubtotal(subtotalAcumulado);
        ordenEstado.setItbis(itbisAcumulado);
        ordenEstado.setMontoTotal(totalAcumulado);

        // Recuperar cliente y forma de pago para el header
        Empresa_cliente cliente = /* la variable donde guardaste el cliente seleccionado */ null;
        FormaPago formaPago     = /* la variable donde guardaste la forma de pago */       null;

        OrdenVentaEstado.ordenActual     = ordenEstado;
        OrdenVentaEstado.detalles        = new java.util.ArrayList<>(Detalles);
        OrdenVentaEstado.nombreCliente   = cliente   != null ? cliente.getNombre()  : "—";
        OrdenVentaEstado.nombreFormaPago = formaPago != null ? formaPago.getNombre()     : "—";

        AppNavigator.irAConfirmarOrden();
    }

    private void actualizarTotalesOrden() {
        String sql = "UPDATE ORDEN_VENTA SET subtotal = ?, itbis = ?, monto_total = ? WHERE id_orden_venta = ?";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, subtotalAcumulado);
            ps.setDouble(2, itbisAcumulado);
            ps.setDouble(3, totalAcumulado);
            ps.setInt(4, idOrdenActual);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error actualizando totales: " + e.getMessage());
        }
    }
}