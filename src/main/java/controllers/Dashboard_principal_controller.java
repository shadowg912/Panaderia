package controllers;

import Data_base.CONEXION;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import utils.AppNavigator;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import static utils.AlertHelper.*;

public class Dashboard_principal_controller implements Initializable {

    @FXML private Label lblVentasHoy;
    @FXML private Label lblVentasCount;
    @FXML private Label lblStockBajo;
    @FXML private Label lblProdPendiente;
    @FXML private Label lblEnviosPendientes;
    @FXML private TableView<ProductoResumen> tablaStockBajo;
    @FXML private TableColumn<ProductoResumen, Integer> colProdId;
    @FXML private TableColumn<ProductoResumen, String> colProdNombre;
    @FXML private TableColumn<ProductoResumen, Double> colProdStock;
    @FXML private TableColumn<ProductoResumen, String> colProdTipo;
    @FXML private VBox cardVentas;

    private CONEXION conexion = new CONEXION();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colProdId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colProdNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre()));
        colProdStock.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getStock()));
        colProdTipo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTipo()));
        cargarKPIs();
    }

    private void cargarKPIs() {
        cargarVentasHoy();
        cargarStockBajo();
        cargarProdPendiente();
        cargarEnviosPendientes();
        cargarTablaStockBajo();
    }

    private void cargarVentasHoy() {
        String sql = "SELECT COUNT(*) AS cnt, COALESCE(SUM(monto_total), 0) AS total " +
                     "FROM ORDEN_VENTA WHERE CAST(fecha_orden AS DATE) = CAST(GETDATE() AS DATE) " +
                     "AND estado != 'CANCELADA'";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("cnt");
                double total = rs.getDouble("total");
                lblVentasHoy.setText(String.format("RD$ %,.2f", total));
                lblVentasCount.setText(count + " órdenes");
            }
        } catch (SQLException e) {
            lblVentasHoy.setText("RD$ 0.00");
        }
    }

    private void cargarStockBajo() {
        String sql = "SELECT COUNT(*) FROM PRODUCTO p " +
                     "LEFT JOIN INVENTARIO i ON p.id_producto = i.id_producto " +
                     "WHERE p.estado = 1 AND COALESCE(i.stock_actual, 0) < 5";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) lblStockBajo.setText(String.valueOf(rs.getInt(1)));
        } catch (SQLException e) { lblStockBajo.setText("0"); }
    }

    private void cargarProdPendiente() {
        String sql = "SELECT COUNT(*) FROM ORDEN_PRODUCCION WHERE estado = 'Pendiente'";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) lblProdPendiente.setText(String.valueOf(rs.getInt(1)));
        } catch (SQLException e) { lblProdPendiente.setText("0"); }
    }

    private void cargarEnviosPendientes() {
        String sql = "SELECT COUNT(*) FROM ENVIO WHERE estado NOT IN ('ENTREGADO', 'CANCELADO')";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) lblEnviosPendientes.setText(String.valueOf(rs.getInt(1)));
        } catch (SQLException e) { lblEnviosPendientes.setText("0"); }
    }

    private void cargarTablaStockBajo() {
        ObservableList<ProductoResumen> lista = FXCollections.observableArrayList();
        String sql = "SELECT p.id_producto, p.nombre, COALESCE(i.stock_actual, 0) AS stock, p.tipo_producto " +
                     "FROM PRODUCTO p " +
                     "LEFT JOIN INVENTARIO i ON p.id_producto = i.id_producto " +
                     "WHERE p.estado = 1 AND COALESCE(i.stock_actual, 0) < 5 " +
                     "ORDER BY stock ASC";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new ProductoResumen(
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getDouble("stock"),
                        rs.getString("tipo_producto")
                ));
            }
        } catch (SQLException e) { /* ignore */ }
        tablaStockBajo.setItems(lista);
    }

    public static class ProductoResumen {
        private final int id;
        private final String nombre;
        private final double stock;
        private final String tipo;

        public ProductoResumen(int id, String nombre, double stock, String tipo) {
            this.id = id; this.nombre = nombre; this.stock = stock; this.tipo = tipo;
        }
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public double getStock() { return stock; }
        public String getTipo() { return tipo; }
    }

    @FXML public void irAVentas()     { AppNavigator.navigateTo("/view/Dashboard_ventas.fxml"); }
    @FXML public void irAInventario() { AppNavigator.navigateTo("/view/Dashboard_inventario.fxml"); }
    @FXML public void irAProduccion() { AppNavigator.navigateTo("/view/Dashboard_produccion.fxml"); }
    @FXML public void irACompras()    { AppNavigator.navigateTo("/view/Dashboard_compras.fxml"); }
}
