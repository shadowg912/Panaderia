package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.CategoriaProducto;
import model.Producto;
import model.Unidad;
import utils.AppNavigator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Inventario_controller {
    @FXML
    private TableView<Producto> tablaProductos;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, String> colCategoria;
    @FXML
    private TableColumn<Producto, String> colPrecio;
    @FXML
    private TableColumn<Producto, String> colUnidad;
    @FXML
    private ComboBox<CategoriaProducto> cmbCategoria;
    @FXML
    private TextField txtBuscar;
    @FXML
    private Label lblTotalRegistros;

    private CONEXION conexion = new CONEXION();
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[]{
                    item.nombreDisplayProperty(),
                    item.categoriaDisplayProperty(),
                    item.precioDisplayProperty(),
                    item.unidadDisplayProperty()
            }
    );
    private ObservableList<CategoriaProducto> listaCategorias = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarCategorias();
        cargarProductos();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreDisplayProperty());
        colCategoria.setCellValueFactory(cellData -> cellData.getValue().categoriaDisplayProperty());
        colPrecio.setCellValueFactory(cellData -> cellData.getValue().precioDisplayProperty());
        colUnidad.setCellValueFactory(cellData -> cellData.getValue().unidadDisplayProperty());
    }

    private void cargarCategorias() {
        listaCategorias.clear();
        listaCategorias.add(new CategoriaProducto(0, "Todas las categorías"));

        String sql = "SELECT id_categoria_producto, nombre FROM CATEGORIA_PRODUCTO ORDER BY nombre";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaCategorias.add(new CategoriaProducto(rs.getInt(1), rs.getString("nombre")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cmbCategoria.setItems(listaCategorias);
        cmbCategoria.getSelectionModel().selectFirst();
    }

    private void cargarProductos() {
        listaProductos.clear();

        String sql = "SELECT p.id_producto, p.nombre, p.precio_unitario, " +
                   "cp.id_categoria_producto, cp.nombre as cat_nombre, " +
                   "u.id_unidad, u.nombre as und_nombre " +
                   "FROM PRODUCTO p " +
                   "LEFT JOIN CATEGORIA_PRODUCTO cp ON p.id_categoria_producto = cp.id_categoria_producto " +
                   "LEFT JOIN UNIDAD u ON p.id_unidad = u.id_unidad " +
                   "ORDER BY p.nombre";

        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CategoriaProducto categoria = new CategoriaProducto(
                    rs.getInt("id_categoria_producto"),
                    rs.getString("cat_nombre")
                );
                Unidad unidad = new Unidad(
                    rs.getInt("id_unidad"),
                    rs.getString("und_nombre")
                );
                Producto producto = new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    categoria,
                    rs.getBigDecimal("precio_unitario"),
                    unidad
                );
                listaProductos.add(producto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tablaProductos.setItems(listaProductos);
        actualizarTotal();
    }

    private void actualizarTotal() {
        int total = listaProductos.size();
        lblTotalRegistros.setText("Total: " + total + " producto" + (total != 1 ? "s" : ""));
    }

    public void fnVolverMenu(ActionEvent actionEvent) {
        AppNavigator.load("/view/Menu.fxml");
    }

    public void fnFiltrarCategoria(ActionEvent actionEvent) {
        CategoriaProducto categoriaSeleccionada = cmbCategoria.getValue();

        if (categoriaSeleccionada == null || categoriaSeleccionada.getIdCategoriaProducto() == 0) {
            cargarProductos();
            return;
        }

        filtrarPorCategoriaYBusqueda(categoriaSeleccionada.getIdCategoriaProducto(), txtBuscar.getText());
    }

    public void fnBuscar(ActionEvent actionEvent) {
        CategoriaProducto categoriaSeleccionada = cmbCategoria.getValue();
        int idCategoria = (categoriaSeleccionada != null) ? categoriaSeleccionada.getIdCategoriaProducto() : 0;

        filtrarPorCategoriaYBusqueda(idCategoria, txtBuscar.getText());
    }

    private void filtrarPorCategoriaYBusqueda(int idCategoria, String textoBusqueda) {
        listaProductos.clear();

        StringBuilder sql = new StringBuilder(
            "SELECT p.id_producto, p.nombre, p.precio_unitario, " +
            "cp.id_categoria_producto, cp.nombre as cat_nombre, " +
            "u.id_unidad, u.nombre as und_nombre " +
            "FROM PRODUCTO p " +
            "LEFT JOIN CATEGORIA_PRODUCTO cp ON p.id_categoria_producto = cp.id_categoria_producto " +
            "LEFT JOIN UNIDAD u ON p.id_unidad = u.id_unidad " +
            "WHERE 1=1 "
        );

        List<Object> parametros = new ArrayList<>();

        if (idCategoria > 0) {
            sql.append(" AND p.id_categoria_producto = ? ");
            parametros.add(idCategoria);
        }

        if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
            sql.append(" AND LOWER(p.nombre) LIKE ? ");
            parametros.add("%" + textoBusqueda.toLowerCase().trim() + "%");
        }

        sql.append(" ORDER BY p.nombre");

        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CategoriaProducto categoria = new CategoriaProducto(
                    rs.getInt("id_categoria_producto"),
                    rs.getString("cat_nombre")
                );
                Unidad unidad = new Unidad(
                    rs.getInt("id_unidad"),
                    rs.getString("und_nombre")
                );
                Producto producto = new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    categoria,
                    rs.getBigDecimal("precio_unitario"),
                    unidad
                );
                listaProductos.add(producto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tablaProductos.setItems(listaProductos);
        actualizarTotal();
    }
}