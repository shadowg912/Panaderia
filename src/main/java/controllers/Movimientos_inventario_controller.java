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

public class Movimientos_inventario_controller implements Initializable {

    @FXML private TableView<MovimientoInventario> tablaMovimientos;
    @FXML private TableColumn<MovimientoInventario, String> colMovFecha;
    @FXML private TableColumn<MovimientoInventario, String> colMovProducto;
    @FXML private TableColumn<MovimientoInventario, String> colMovTipo;
    @FXML private TableColumn<MovimientoInventario, Double> colMovCantidad;
    @FXML private TableColumn<MovimientoInventario, Double> colMovStock;
    @FXML private TableColumn<MovimientoInventario, String> colMovUsuario;
    @FXML private ComboBox<Producto> cmbFiltroMovimiento;
    @FXML private ComboBox<TipoMovimiento> cmbTipoMovimiento;
    @FXML private Label lblTotalRegistros;

    private CONEXION conexion = new CONEXION();
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
        configurarColumnas();
        cargarProductosFiltro();
        cargarTiposMovimiento();
        cargarMovimientos();
    }

    private void configurarColumnas() {
        colMovFecha.setCellValueFactory(cd -> cd.getValue().fechaDisplayProperty());
        colMovProducto.setCellValueFactory(cd -> cd.getValue().nombreProductoProperty());
        colMovTipo.setCellValueFactory(cd -> cd.getValue().tipoNaturalezaProperty());
        colMovCantidad.setCellValueFactory(cd -> cd.getValue().cantidadProperty().asObject());
        colMovStock.setCellValueFactory(cd -> cd.getValue().stockResultanteProperty().asObject());
        colMovUsuario.setCellValueFactory(cd -> cd.getValue().usuarioNombreProperty());
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

        Producto prodFiltro = cmbFiltroMovimiento.getValue();
        if (prodFiltro != null && prodFiltro.getIdProducto() > 0) {
            sql.append(" AND m.id_producto = ? ");
            params.add(prodFiltro.getIdProducto());
        }
        TipoMovimiento tipoFiltro = cmbTipoMovimiento.getValue();
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
        lblTotalRegistros.setText("Total: " + listaMovimientos.size() + " movimiento" + (listaMovimientos.size() != 1 ? "s" : ""));
    }

    public void fnFiltrar(ActionEvent event) { cargarMovimientos(); }

    public void fnVolverMenu(ActionEvent event) { AppNavigator.cargarDashboard(); }
}
