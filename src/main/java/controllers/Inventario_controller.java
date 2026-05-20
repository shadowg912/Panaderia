package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

    @FXML private TableView<MovimientoInventario> tablaMovimientos;
    @FXML private TableColumn<MovimientoInventario, String> colMovFecha;
    @FXML private TableColumn<MovimientoInventario, String> colMovProducto;
    @FXML private TableColumn<MovimientoInventario, String> colMovTipo;
    @FXML private TableColumn<MovimientoInventario, Double> colMovCantidad;
    @FXML private TableColumn<MovimientoInventario, Double> colMovStock;
    @FXML private TableColumn<MovimientoInventario, String> colMovUsuario;
    @FXML private ComboBox<Producto> cmbFiltroMovimiento;
    @FXML private ComboBox<TipoMovimiento> cmbTipoMovimiento;

    private CONEXION conexion = new CONEXION();
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[]{
                    item.nombreDisplayProperty(), item.categoriaDisplayProperty(),
                    item.stockDisplayProperty(), item.precioDisplayProperty(),
                    item.unidadDisplayProperty()
            }
    );
    private ObservableList<CategoriaProducto> listaCategorias = FXCollections.observableArrayList();
    private ObservableList<MovimientoInventario> listaMovimientos = FXCollections.observableArrayList(
            m -> new javafx.beans.Observable[]{
                    m.fechaDisplayProperty(), m.nombreProductoProperty(),
                    m.tipoNaturalezaProperty(), m.cantidadProperty(),
                    m.stockResultanteProperty(), m.usuarioNombreProperty()
            }
    );
    private ObservableList<Producto> listaProductosFiltro = FXCollections.observableArrayList();
    private ObservableList<TipoMovimiento> listaTiposMovimiento = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnasProductos();
        configurarColumnasMovimientos();
        cargarCategorias();
        cargarProductos();
        cargarProductosFiltro();
        cargarTiposMovimiento();
        cargarMovimientos();
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

    private void configurarColumnasMovimientos() {
        colMovFecha.setCellValueFactory(cd -> cd.getValue().fechaDisplayProperty());
        colMovProducto.setCellValueFactory(cd -> cd.getValue().nombreProductoProperty());
        colMovTipo.setCellValueFactory(cd -> cd.getValue().tipoNaturalezaProperty());
        colMovCantidad.setCellValueFactory(cd -> cd.getValue().cantidadProperty().asObject());
        colMovStock.setCellValueFactory(cd -> cd.getValue().stockResultanteProperty().asObject());
        colMovUsuario.setCellValueFactory(cd -> cd.getValue().usuarioNombreProperty());
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

    private void cargarProductosFiltro() {
        listaProductosFiltro.clear();
        String sql = "SELECT id_producto, nombre FROM PRODUCTO WHERE estado = 1 ORDER BY nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                listaProductosFiltro.add(new Producto(rs.getInt("id_producto"), rs.getString("nombre")));
        } catch (Exception e) { mostrarError("Error cargando productos: " + e.getMessage()); }
        listaProductosFiltro.add(0, new Producto(0, "Todos los productos"));
        cmbFiltroMovimiento.setItems(listaProductosFiltro);
        cmbFiltroMovimiento.getSelectionModel().selectFirst();
    }

    private void cargarTiposMovimiento() {
        listaTiposMovimiento.clear();
        String sql = "SELECT id_tipo, nombre, naturaleza FROM TIPO_MOVIMIENTO ORDER BY nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                listaTiposMovimiento.add(new TipoMovimiento(rs.getInt("id_tipo"), rs.getString("nombre"), rs.getString("naturaleza")));
        } catch (Exception e) { mostrarError("Error cargando tipos: " + e.getMessage()); }
        listaTiposMovimiento.add(0, new TipoMovimiento(0, "Todos", null));
        cmbTipoMovimiento.setItems(listaTiposMovimiento);
        cmbTipoMovimiento.getSelectionModel().selectFirst();
    }

    private void cargarMovimientos() {
        listaMovimientos.clear();
        StringBuilder sql = new StringBuilder(
            "SELECT m.id_movimiento, m.id_producto, p.nombre as producto_nombre, " +
            "m.cantidad, tm.nombre as tipo_nombre, tm.naturaleza as tipo_naturaleza, " +
            "m.fecha, u.nombre_usuario, " +
            "(SELECT SUM(CASE WHEN tm2.naturaleza = 'ENTRADA' THEN mi2.cantidad ELSE -mi2.cantidad END) " +
            " FROM MOVIMIENTO_INVENTARIO mi2 " +
            " LEFT JOIN TIPO_MOVIMIENTO tm2 ON mi2.id_tipo_movimiento = tm2.id_tipo " +
            " WHERE mi2.id_producto = m.id_producto AND mi2.fecha <= m.fecha) as stock_resultante " +
            "FROM MOVIMIENTO_INVENTARIO m " +
            "LEFT JOIN PRODUCTO p ON m.id_producto = p.id_producto " +
            "LEFT JOIN TIPO_MOVIMIENTO tm ON m.id_tipo_movimiento = tm.id_tipo " +
            "LEFT JOIN USUARIO u ON m.id_usuario = u.id_usuario WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        Producto prodFiltro = cmbFiltroMovimiento != null ? cmbFiltroMovimiento.getValue() : null;
        if (prodFiltro != null && prodFiltro.getIdProducto() > 0) {
            sql.append(" AND m.id_producto = ? ");
            params.add(prodFiltro.getIdProducto());
        }
        TipoMovimiento tipoFiltro = cmbTipoMovimiento != null ? cmbTipoMovimiento.getValue() : null;
        if (tipoFiltro != null && tipoFiltro.getIdTipo() > 0) {
            sql.append(" AND m.id_tipo_movimiento = ? ");
            params.add(tipoFiltro.getIdTipo());
        }

        sql.append(" ORDER BY m.fecha DESC");

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MovimientoInventario m = new MovimientoInventario();
                m.setIdMovimiento(rs.getInt("id_movimiento"));
                m.setIdProducto(rs.getInt("id_producto"));
                m.setNombreProducto(rs.getString("producto_nombre"));
                m.setCantidad(rs.getDouble("cantidad"));
                String nat = rs.getString("tipo_naturaleza");
                m.setTipoNaturaleza(nat != null ? nat : "");
                m.setTipoNombre(rs.getString("tipo_nombre"));
                Timestamp ts = rs.getTimestamp("fecha");
                m.setFechaDisplay(ts != null ? ts.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
                m.setUsuarioNombre(rs.getString("nombre_usuario"));
                m.setStockResultante(rs.getDouble("stock_resultante"));
                listaMovimientos.add(m);
            }
        } catch (Exception e) { mostrarError("Error al cargar movimientos: " + e.getMessage()); }
        tablaMovimientos.setItems(listaMovimientos);
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

    public void fnFiltrarMovimientos(ActionEvent event) { cargarMovimientos(); }

    public void fnVolverMenu(ActionEvent event) { AppNavigator.cargarDashboard(); }
}
