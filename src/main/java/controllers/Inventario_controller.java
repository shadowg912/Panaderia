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
    @FXML private TableColumn<Producto, Void> colAccion;
    @FXML private ComboBox<CategoriaProducto> cmbCategoria;
    @FXML private ComboBox<String> cmbTipoProducto;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotalRegistros;
    @FXML private Label lblStockBajo;
    @FXML private TabPane tabPane;
    @FXML private Tab tabActivos;
    @FXML private Tab tabInactivos;

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
        cargarTiposProducto();
        cargarProductos(1);
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, old, tab) -> cargarProductos(tab == tabActivos ? 1 : 0));
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

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 4 12; -fx-font-weight: bold;");
                btn.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    toggleEstado(p);
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Producto p = getTableView().getItems().get(getIndex());
                    if (p.isActivo()) {
                        btn.setText("Desactivar");
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-border-color: #ef4444; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 12; -fx-font-weight: bold;");
                    } else {
                        btn.setText("Activar");
                        btn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 4 12; -fx-font-weight: bold;");
                    }
                    setGraphic(btn);
                }
            }
        });

        tablaProductos.setRowFactory(tv -> new TableRow<Producto>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (tabActivos.isSelected() && item.getStockActual() < STOCK_BAJO_UMBRAL) {
                    setStyle("-fx-background-color: #fef2f2;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void toggleEstado(Producto p) {
        String accion = p.isActivo() ? "desactivar" : "reactivar";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Está seguro de " + accion + " \"" + p.getNombre() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        int nuevoEstado = p.isActivo() ? 0 : 1;
        String sql = "UPDATE PRODUCTO SET estado = ? WHERE id_producto = ?";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, nuevoEstado);
            ps.setInt(2, p.getIdProducto());
            ps.executeUpdate();
            mostrarInfo("Producto \"" + p.getNombre() + "\" " + (p.isActivo() ? "desactivado" : "reactivado") + ".");
            cargarProductos(tabActivos.isSelected() ? 1 : 0);
        } catch (Exception e) { mostrarError("Error: " + e.getMessage()); }
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

    private void cargarTiposProducto() {
        cmbTipoProducto.setItems(FXCollections.observableArrayList(
            "Todos los tipos", "PRODUCTO_TERMINADO", "MATERIA_PRIMA", "MATERIAL_EMPAQUE"
        ));
        cmbTipoProducto.getSelectionModel().selectFirst();
    }

    private void cargarProductos(int estadoVal) {
        listaProductos.clear();
        String sql = "SELECT p.id_producto, p.nombre, p.precio_unitario, p.tipo_producto, p.estado, " +
                    "cp.id_categoria_producto, cp.nombre as cat_nombre, " +
                    "u.id_unidad, u.nombre as und_nombre, " +
                    "COALESCE(i.stock_actual, 0) as stock_actual " +
                    "FROM PRODUCTO p " +
                    "LEFT JOIN CATEGORIA_PRODUCTO cp ON p.id_categoria_producto = cp.id_categoria_producto " +
                    "LEFT JOIN UNIDAD u ON p.id_unidad = u.id_unidad " +
                    "LEFT JOIN INVENTARIO i ON p.id_producto = i.id_producto " +
                    "WHERE p.estado = ? ORDER BY p.nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, estadoVal);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Producto p = new Producto(
                    rs.getInt("id_producto"), rs.getString("nombre"),
                    new CategoriaProducto(rs.getInt("id_categoria_producto"), rs.getString("cat_nombre")),
                    rs.getBigDecimal("precio_unitario"),
                    new Unidad(rs.getInt("id_unidad"), rs.getString("und_nombre")),
                    rs.getDouble("stock_actual")
                );
                p.setTipoProducto(rs.getString("tipo_producto"));
                p.setActivo(rs.getBoolean("estado"));
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

    public void fnFiltrarCategoria(ActionEvent event) { aplicarFiltros(); }

    public void fnFiltrarTipoProducto(ActionEvent event) { aplicarFiltros(); }

    public void fnBuscar(ActionEvent event) { aplicarFiltros(); }

    private void aplicarFiltros() {
        CategoriaProducto cat = cmbCategoria.getValue();
        String tipo = cmbTipoProducto.getValue();
        String texto = txtBuscar.getText().trim();

        int idCat = cat != null ? cat.getIdCategoriaProducto() : 0;
        boolean filtrarTipo = tipo != null && !"Todos los tipos".equals(tipo);
        boolean filtrarCat = idCat > 0;
        boolean filtrarTexto = !texto.isEmpty();

        int estadoVal = tabActivos.isSelected() ? 1 : 0;
        listaProductos.clear();
        StringBuilder sql = new StringBuilder(
            "SELECT p.id_producto, p.nombre, p.precio_unitario, p.tipo_producto, p.estado, " +
                    "cp.id_categoria_producto, cp.nombre as cat_nombre, " +
                    "u.id_unidad, u.nombre as und_nombre, " +
                    "COALESCE(i.stock_actual, 0) as stock_actual " +
                    "FROM PRODUCTO p " +
                    "LEFT JOIN CATEGORIA_PRODUCTO cp ON p.id_categoria_producto = cp.id_categoria_producto " +
                    "LEFT JOIN UNIDAD u ON p.id_unidad = u.id_unidad " +
                    "LEFT JOIN INVENTARIO i ON p.id_producto = i.id_producto " +
                    "WHERE p.estado = ?"
        );
        List<Object> params = new ArrayList<>();
        params.add(estadoVal);
        if (filtrarCat) { sql.append(" AND p.id_categoria_producto = ?"); params.add(idCat); }
        if (filtrarTipo) { sql.append(" AND p.tipo_producto = ?"); params.add(tipo); }
        if (filtrarTexto) { sql.append(" AND LOWER(p.nombre) LIKE ?"); params.add("%" + texto.toLowerCase() + "%"); }
        sql.append(" ORDER BY p.nombre");

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Producto p = new Producto(
                    rs.getInt("id_producto"), rs.getString("nombre"),
                    new CategoriaProducto(rs.getInt("id_categoria_producto"), rs.getString("cat_nombre")),
                    rs.getBigDecimal("precio_unitario"),
                    new Unidad(rs.getInt("id_unidad"), rs.getString("und_nombre")),
                    rs.getDouble("stock_actual")
                );
                p.setTipoProducto(rs.getString("tipo_producto"));
                p.setActivo(rs.getBoolean("estado"));
                listaProductos.add(p);
            }
        } catch (Exception e) { mostrarError("Error al filtrar: " + e.getMessage()); }
        tablaProductos.setItems(listaProductos);
        actualizarTotales();
    }

    public void fnVolverMenu(ActionEvent event) { AppNavigator.cargarDashboard(); }
}
