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

import java.net.URL;
import java.sql.*;
import java.util.*;

import static utils.AlertHelper.*;

public class Inventario_controller implements Initializable {

    private static final double STOCK_BAJO_UMBRAL = 5;

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colStock;
    @FXML private TableColumn<Producto, String> colPrecio;
    @FXML private TableColumn<Producto, String> colUnidad;
    @FXML private TableColumn<Producto, String> colTipo;
    @FXML private ComboBox<CategoriaProducto> cmbCategoria;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotalRegistros;
    @FXML private Label lblStockBajo;

    private CONEXION conexion = new CONEXION();
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[]{
                    item.nombreDisplayProperty(), item.categoriaDisplayProperty(),
                    item.stockDisplayProperty(), item.precioDisplayProperty(),
                    item.unidadDisplayProperty()
            }
    );
    private ObservableList<CategoriaProducto> listaCategorias = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnasProductos();
        cargarCategorias();
        cargarProductos();
    }

    private void configurarColumnasProductos() {
        colNombre.setCellValueFactory(cd -> cd.getValue().nombreDisplayProperty());
        colCategoria.setCellValueFactory(cd -> cd.getValue().categoriaDisplayProperty());
        colStock.setCellValueFactory(cd -> cd.getValue().stockDisplayProperty());
        colPrecio.setCellValueFactory(cd -> cd.getValue().precioDisplayProperty());
        colUnidad.setCellValueFactory(cd -> cd.getValue().unidadDisplayProperty());
        colTipo.setCellValueFactory(cd -> {
            Producto p = cd.getValue();
            String t = p.getTipoProducto() != null ? p.getTipoProducto() : "";
            return new javafx.beans.property.SimpleStringProperty(t);
        });

        tablaProductos.setRowFactory(tv -> new TableRow<Producto>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.getStockActual() < STOCK_BAJO_UMBRAL) {
                    setStyle("-fx-background-color: #fef2f2;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void cargarCategorias() {
        listaCategorias.clear();
        listaCategorias.add(new CategoriaProducto(0, "Todas las categorías"));
        String sql = "SELECT id_categoria_producto, nombre FROM CATEGORIA_PRODUCTO WHERE estado = 1 ORDER BY nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                listaCategorias.add(new CategoriaProducto(rs.getInt(1), rs.getString("nombre")));
        } catch (Exception e) { mostrarError("Error cargando categorías: " + e.getMessage()); }
        cmbCategoria.setItems(listaCategorias);
        cmbCategoria.getSelectionModel().selectFirst();
    }

    private void cargarProductos() {
        listaProductos.clear();
        String sql = "SELECT p.id_producto, p.nombre, p.precio_unitario, p.tipo_producto, " +
                   "cp.id_categoria_producto, cp.nombre as cat_nombre, " +
                   "u.id_unidad, u.nombre as und_nombre, " +
                   "COALESCE(i.stock_actual, 0) as stock_actual " +
                   "FROM PRODUCTO p " +
                   "LEFT JOIN CATEGORIA_PRODUCTO cp ON p.id_categoria_producto = cp.id_categoria_producto " +
                   "LEFT JOIN UNIDAD u ON p.id_unidad = u.id_unidad " +
                   "LEFT JOIN INVENTARIO i ON p.id_producto = i.id_producto " +
                   "WHERE p.estado = 1 ORDER BY p.nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto(
                    rs.getInt("id_producto"), rs.getString("nombre"),
                    new CategoriaProducto(rs.getInt("id_categoria_producto"), rs.getString("cat_nombre")),
                    rs.getBigDecimal("precio_unitario"),
                    new Unidad(rs.getInt("id_unidad"), rs.getString("und_nombre")),
                    rs.getDouble("stock_actual")
                );
                p.setTipoProducto(rs.getString("tipo_producto"));
                listaProductos.add(p);
            }
        } catch (Exception e) { mostrarError("Error al cargar productos: " + e.getMessage()); }
        tablaProductos.setItems(listaProductos);
        actualizarTotales();
    }

    private void actualizarTotales() {
        int total = listaProductos.size();
        long bajoStock = listaProductos.stream().filter(p -> p.getStockActual() < STOCK_BAJO_UMBRAL).count();
        lblTotalRegistros.setText("Total: " + total + " producto" + (total != 1 ? "s" : ""));
        if (bajoStock > 0) {
            lblStockBajo.setText("⚠ " + bajoStock + " producto" + (bajoStock != 1 ? "s" : "") + " con stock bajo");
            lblStockBajo.setVisible(true);
        } else {
            lblStockBajo.setVisible(false);
        }
    }

    public void fnFiltrarCategoria(ActionEvent event) {
        CategoriaProducto cat = cmbCategoria.getValue();
        if (cat == null || cat.getIdCategoriaProducto() == 0) {
            cargarProductos();
            return;
        }
        String texto = txtBuscar.getText().trim();
        listaProductos.clear();
        StringBuilder sql = new StringBuilder(
            "SELECT p.id_producto, p.nombre, p.precio_unitario, p.tipo_producto, " +
            "cp.id_categoria_producto, cp.nombre as cat_nombre, " +
            "u.id_unidad, u.nombre as und_nombre, " +
            "COALESCE(i.stock_actual, 0) as stock_actual " +
            "FROM PRODUCTO p " +
            "LEFT JOIN CATEGORIA_PRODUCTO cp ON p.id_categoria_producto = cp.id_categoria_producto " +
            "LEFT JOIN UNIDAD u ON p.id_unidad = u.id_unidad " +
            "LEFT JOIN INVENTARIO i ON p.id_producto = i.id_producto " +
            "WHERE p.estado = 1 AND p.id_categoria_producto = ?"
        );
        List<Object> params = new ArrayList<>();
        params.add(cat.getIdCategoriaProducto());
        if (!texto.isEmpty()) {
            sql.append(" AND LOWER(p.nombre) LIKE ? ");
            params.add("%" + texto.toLowerCase() + "%");
        }
        sql.append(" ORDER BY p.nombre");
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaProductos.add(new Producto(
                    rs.getInt("id_producto"), rs.getString("nombre"),
                    new CategoriaProducto(rs.getInt("id_categoria_producto"), rs.getString("cat_nombre")),
                    rs.getBigDecimal("precio_unitario"),
                    new Unidad(rs.getInt("id_unidad"), rs.getString("und_nombre")),
                    rs.getDouble("stock_actual")
                ));
            }
        } catch (Exception e) { mostrarError("Error al filtrar: " + e.getMessage()); }
        tablaProductos.setItems(listaProductos);
        actualizarTotales();
    }

    public void fnBuscar(ActionEvent event) {
        CategoriaProducto cat = cmbCategoria.getValue();
        int idCat = cat != null ? cat.getIdCategoriaProducto() : 0;
        String texto = txtBuscar.getText().trim();
        listaProductos.clear();
        StringBuilder sql = new StringBuilder(
            "SELECT p.id_producto, p.nombre, p.precio_unitario, p.tipo_producto, " +
            "cp.id_categoria_producto, cp.nombre as cat_nombre, " +
            "u.id_unidad, u.nombre as und_nombre, " +
            "COALESCE(i.stock_actual, 0) as stock_actual " +
            "FROM PRODUCTO p " +
            "LEFT JOIN CATEGORIA_PRODUCTO cp ON p.id_categoria_producto = cp.id_categoria_producto " +
            "LEFT JOIN UNIDAD u ON p.id_unidad = u.id_unidad " +
            "LEFT JOIN INVENTARIO i ON p.id_producto = i.id_producto " +
            "WHERE p.estado = 1"
        );
        List<Object> params = new ArrayList<>();
        if (idCat > 0) { sql.append(" AND p.id_categoria_producto = ? "); params.add(idCat); }
        if (!texto.isEmpty()) { sql.append(" AND LOWER(p.nombre) LIKE ? "); params.add("%" + texto.toLowerCase() + "%"); }
        sql.append(" ORDER BY p.nombre");
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaProductos.add(new Producto(
                    rs.getInt("id_producto"), rs.getString("nombre"),
                    new CategoriaProducto(rs.getInt("id_categoria_producto"), rs.getString("cat_nombre")),
                    rs.getBigDecimal("precio_unitario"),
                    new Unidad(rs.getInt("id_unidad"), rs.getString("und_nombre")),
                    rs.getDouble("stock_actual")
                ));
            }
        } catch (Exception e) { mostrarError("Error al buscar: " + e.getMessage()); }
        tablaProductos.setItems(listaProductos);
        actualizarTotales();
    }

    public void fnVolverMenu(ActionEvent event) { AppNavigator.cargarDashboard(); }
}
