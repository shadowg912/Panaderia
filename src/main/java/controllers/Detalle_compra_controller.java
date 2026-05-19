package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.*;
import utils.AppNavigator;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.*;
import static utils.AlertHelper.*;

public class Detalle_compra_controller implements Initializable {

    @FXML private Label lblInfoCompra;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtCostoUnitario;
    @FXML private Button btnAgregar;
    @FXML private TableView<DetalleCompra> tblDetalle;
    @FXML private TableColumn<DetalleCompra, String> colProducto;
    @FXML private TableColumn<DetalleCompra, Double> colCantidad;
    @FXML private TableColumn<DetalleCompra, Double> colCosto;
    @FXML private TableColumn<DetalleCompra, Double> colTotal;
    @FXML private Label lblTotal;
    @FXML private Button btnVolver;
    @FXML private Button btnConfirmar;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<Producto> productos = FXCollections.observableArrayList();
    ObservableList<DetalleCompra> detalles = FXCollections.observableArrayList(
            d -> new javafx.beans.Observable[]{
                    d.nombreProductoProperty(),
                    d.cantidadProperty(),
                    d.costoUnitarioProperty(),
                    d.montoTotalProperty()
            }
    );

    private int idCompraActual;
    private double totalCompra = 0;

    public static void setIdCompra(int id) {
        idCompraEstatico = id;
    }
    private static int idCompraEstatico = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCompraActual = idCompraEstatico;
        if (idCompraActual == 0) idCompraActual = CompraEstado.idCompraMaterial;
        lblInfoCompra.setText("Compra #" + idCompraActual + " — " + CompraEstado.nombreProveedor);

        cmbProducto.setItems(cargarProductos());

        colProducto.setCellValueFactory(cd -> cd.getValue().nombreProductoProperty());
        colCantidad.setCellValueFactory(cd -> cd.getValue().cantidadProperty().asObject());
        colCosto.setCellValueFactory(cd -> cd.getValue().costoUnitarioProperty().asObject());
        colTotal.setCellValueFactory(cd -> cd.getValue().montoTotalProperty().asObject());

        tblDetalle.setItems(detalles);

        // Cargar detalles existentes si los hay
        cargarDetallesExistentes();
    }

    private ObservableList<Producto> cargarProductos() {
        String sql = "SELECT id_producto, nombre, precio_unitario FROM PRODUCTO WHERE tipo_producto = 'MATERIA_PRIMA' ORDER BY nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                productos.add(p);
            }
        } catch (Exception e) {
            mostrarError("Error cargando productos: " + e.getMessage());
        }
        return productos;
    }

    private void cargarDetallesExistentes() {
        String sql = "SELECT dc.id_producto, p.nombre, dc.cantidad, dc.costo_unitario, dc.monto_total " +
                   "FROM DETALLE_COMPRA dc " +
                   "INNER JOIN PRODUCTO p ON dc.id_producto = p.id_producto " +
                   "WHERE dc.id_compra_material = ?";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCompraActual);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DetalleCompra d = new DetalleCompra();
                d.setIdCompraMaterial(idCompraActual);
                d.setIdProducto(rs.getInt("id_producto"));
                d.setNombreProducto(rs.getString("nombre"));
                d.setCantidad(rs.getDouble("cantidad"));
                d.setCostoUnitario(rs.getDouble("costo_unitario"));
                d.setMontoTotal(rs.getDouble("monto_total"));
                detalles.add(d);
                totalCompra += d.getMontoTotal();
            }
        } catch (SQLException e) {
            mostrarError("Error cargando detalles: " + e.getMessage());
        }
        actualizarTotal();
    }

    @FXML
    public void fnAgregar(ActionEvent event) {
        if (!validarCampos()) return;

        Producto producto = cmbProducto.getValue();
        int idProducto = producto.getIdProducto();

        // Check duplicate
        for (DetalleCompra d : detalles) {
            if (d.getIdProducto() == idProducto) {
                mostrarAdvertencia("Este producto ya fue agregado.");
                return;
            }
        }

        double cantidad = Double.parseDouble(txtCantidad.getText().trim());
        double costo = Double.parseDouble(txtCostoUnitario.getText().trim());
        double monto = cantidad * costo;

        // Get or create the INGREDIENTE for this product
        int idIngrediente = obtenerOCrearIngrediente(producto);
        if (idIngrediente == 0) {
            mostrarError("Error al procesar el ingrediente.");
            return;
        }

        DetalleCompra detalle = new DetalleCompra();
        detalle.setIdCompraMaterial(idCompraActual);
        detalle.setIdProducto(idProducto);
        detalle.setNombreProducto(producto.getNombre());
        detalle.setCantidad(cantidad);
        detalle.setCostoUnitario(costo);
        detalle.setMontoTotal(monto);

        if (insertarDetalle(idIngrediente, detalle)) {
            detalles.add(detalle);
            totalCompra += monto;
            actualizarTotal();
            limpiarCampos();
            mostrarInfo("Producto agregado: " + producto.getNombre());
        }
    }

    private int obtenerOCrearIngrediente(Producto producto) {
        String sqlBuscar = "SELECT id_ingrediente FROM INGREDIENTE WHERE nombre = ?";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sqlBuscar)) {
            ps.setString(1, producto.getNombre());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_ingrediente");
        } catch (SQLException e) {
            mostrarError("Error buscando ingrediente: " + e.getMessage());
            return 0;
        }

        String sqlCrear = "INSERT INTO INGREDIENTE (nombre, unidad_medida, id_categoria_ingrediente) VALUES (?, 'unidad', 1)";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sqlCrear, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, producto.getNombre());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            mostrarError("Error creando ingrediente: " + e.getMessage());
        }
        return 0;
    }

    private boolean insertarDetalle(int idIngrediente, DetalleCompra detalle) {
        String sql = "INSERT INTO DETALLE_COMPRA (id_compra_material, id_ingrediente, id_producto, cantidad, costo_unitario, monto_total) " +
                   "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdCompraMaterial());
            ps.setInt(2, idIngrediente);
            ps.setInt(3, detalle.getIdProducto());
            ps.setDouble(4, detalle.getCantidad());
            ps.setDouble(5, detalle.getCostoUnitario());
            ps.setDouble(6, detalle.getMontoTotal());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            mostrarError("Error agregando producto: " + e.getMessage());
            return false;
        }
    }

    private void actualizarTotal() {
        lblTotal.setText(String.format("RD$ %.2f", totalCompra));
    }

    private void limpiarCampos() {
        cmbProducto.setValue(null);
        txtCantidad.clear();
        txtCostoUnitario.clear();
    }

    private boolean validarCampos() {
        if (cmbProducto.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar un producto.");
            return false;
        }
        if (txtCantidad.getText() == null || txtCantidad.getText().trim().isEmpty()) {
            mostrarAdvertencia("Debe ingresar una cantidad.");
            return false;
        }
        try {
            double cant = Double.parseDouble(txtCantidad.getText().trim());
            if (cant <= 0) { mostrarAdvertencia("La cantidad debe ser mayor a 0."); return false; }
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Cantidad inválida."); return false;
        }
        if (txtCostoUnitario.getText() == null || txtCostoUnitario.getText().trim().isEmpty()) {
            mostrarAdvertencia("Debe ingresar el costo unitario.");
            return false;
        }
        try {
            double costo = Double.parseDouble(txtCostoUnitario.getText().trim());
            if (costo < 0) { mostrarAdvertencia("El costo no puede ser negativo."); return false; }
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Costo inválido."); return false;
        }
        return true;
    }

    @FXML
    public void fnConfirmar(ActionEvent event) {
        if (detalles.isEmpty()) {
            mostrarAdvertencia("Debe agregar al menos un producto.");
            return;
        }
        mostrarInfo("Compra #" + idCompraActual + " registrada exitosamente.");
        CompraEstado.limpiar();
        appNavigator.volverMenu();
    }

    @FXML
    public void fnVolver(ActionEvent event) {
        appNavigator.volverMenu();
    }

}
