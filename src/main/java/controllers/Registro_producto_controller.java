package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.CategoriaProducto;
import model.Producto;
import model.Unidad;
import model.ValorNutricional;
import utils.AppNavigator;

import java.math.BigDecimal;
import java.sql.*;

public class Registro_producto_controller {
    @FXML private TextField txtEnergia, txtProteinas, txtCarbohidratos, txtGrasas, txtFibra, txtSodio;
    @FXML private TextField txtAzucares, txtGrasasSaturadas, txtGrasasTrans;
    @FXML private TextField txtPrecio, txtNombre;
    @FXML private ComboBox<CategoriaProducto> cmbCategoria;
    @FXML private ComboBox<Unidad> cmbUnidad;
    @FXML private ComboBox<String> cmbTipoProducto;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<CategoriaProducto> Categoria = FXCollections.observableArrayList();
    ObservableList<Unidad> Unidades = FXCollections.observableArrayList();

    public void fnlimpiar() {
        txtEnergia.clear(); txtProteinas.clear(); txtCarbohidratos.clear();
        txtGrasas.clear(); txtFibra.clear(); txtSodio.clear();
        txtAzucares.clear(); txtGrasasSaturadas.clear(); txtGrasasTrans.clear();
        txtPrecio.clear(); txtPrecio.setEditable(true); txtNombre.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbUnidad.getSelectionModel().clearSelection();
        cmbTipoProducto.getSelectionModel().clearSelection();
    }

    public void fnVolerMenu(ActionEvent actionEvent) { appNavigator.volverMenu(); }

    public ObservableList cargarUnidad() {
        String sql = "SELECT id_unidad,nombre FROM UNIDAD";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) Unidades.add(new Unidad(rs.getInt(1), rs.getString("nombre")));
        } catch (Exception e) {
            mostrarError("Error cargando unidades: " + e.getMessage());
        }
        return Unidades;
    }

    public ObservableList CargarCategoria() {
        String sql = "SELECT id_categoria_producto,nombre FROM CATEGORIA_PRODUCTO";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) Categoria.add(new CategoriaProducto(rs.getInt(1), rs.getString("nombre")));
        } catch (Exception e) {
            mostrarError("Error cargando categorías: " + e.getMessage());
        }
        return Categoria;
    }

    @FXML
    public void initialize() {
        cmbUnidad.setItems(cargarUnidad());
        cmbCategoria.setItems(CargarCategoria());
        cmbTipoProducto.setItems(FXCollections.observableArrayList("PRODUCTO_TERMINADO", "MATERIA_PRIMA", "MATERIAL_EMPAQUE"));

        cmbTipoProducto.setOnAction(e -> {
            String tipo = cmbTipoProducto.getValue();
            if ("MATERIA_PRIMA".equals(tipo) || "MATERIAL_EMPAQUE".equals(tipo)) {
                txtPrecio.setText("0"); txtPrecio.setEditable(false);
            } else {
                txtPrecio.setText(""); txtPrecio.setEditable(true);
            }
        });
    }

    public void InsertarProducto(String nombre, Unidad unidad, float precio, CategoriaProducto categoria, String tipoProducto) {
        String sql = "INSERT INTO PRODUCTO(nombre,id_categoria_producto, precio_unitario, id_unidad, tipo_producto)values(?,?,?,?,?)";
        try (Connection connection = conexion.establecerconexio(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nombre); ps.setInt(2, categoria.getIdCategoriaProducto());
            ps.setBigDecimal(3, new BigDecimal(precio)); ps.setInt(4, unidad.getIdUnidad());
            ps.setString(5, tipoProducto);
            ps.executeUpdate();
            mostrarInfo("Producto insertado correctamente.");
        } catch (SQLException e) {
            mostrarError("Error insertando producto: " + e.getMessage());
        }
    }

    public void fnGuardarProducto(ActionEvent actionEvent) {
        try {
            String nombre = txtNombre.getText();
            Unidad unidad = cmbUnidad.getValue();
            CategoriaProducto categoria = cmbCategoria.getValue();
            String tipoProducto = cmbTipoProducto.getValue();

            int idProducto = obtenerUltimoIdProducto();
            float precio = Float.parseFloat(txtPrecio.getText());
            Producto producto = new Producto();
            producto.setIdProducto(idProducto);
            producto.setNombre(nombre);

            ValorNutricional vn = new ValorNutricional();
            vn.setProducto(producto);
            vn.setCalorias(txtEnergia.getText()); vn.setProteinas(txtProteinas.getText());
            vn.setCarbohidratos(txtCarbohidratos.getText()); vn.setGrasasTotales(txtGrasas.getText());
            vn.setFibraDietetica(txtFibra.getText()); vn.setSodio(txtSodio.getText());
            vn.setAzucares(txtAzucares.getText()); vn.setGrasasSaturadas(txtGrasasSaturadas.getText());
            vn.setGrasasTrans(txtGrasasTrans.getText());

            insertarValorNutricional(vn);
            InsertarProducto(nombre, unidad, precio, categoria, tipoProducto);
            fnlimpiar();
        } catch (NumberFormatException e) {
            mostrarAdvertencia("El precio debe ser un número válido.");
        }
    }

    public void insertarValorNutricional(ValorNutricional vn) {
        String sql = "INSERT INTO VALOR_NUTRICIONAL (id_producto, calorias, grasas_totales, grasas_saturadas, grasas_trans, carbohidratos, azucares, proteinas, fibra_dietetica, sodio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, vn.getIdProducto()); ps.setString(2, vn.getCalorias());
            ps.setString(3, vn.getGrasasTotales()); ps.setString(4, vn.getGrasasSaturadas());
            ps.setString(5, vn.getGrasasTrans()); ps.setString(6, vn.getCarbohidratos());
            ps.setString(7, vn.getAzucares()); ps.setString(8, vn.getProteinas());
            ps.setString(9, vn.getFibraDietetica()); ps.setString(10, vn.getSodio());
            ps.executeUpdate();
        } catch (SQLException e) {
            mostrarError("Error insertando valor nutricional: " + e.getMessage());
        }
    }

    private int obtenerUltimoIdProducto() {
        String sql = "SELECT MAX(id_producto) FROM PRODUCTO";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            mostrarError("Error al obtener ID: " + e.getMessage());
        }
        return 0;
    }

    private void mostrarInfo(String m) { Alert a = new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
    private void mostrarError(String m) { Alert a = new Alert(Alert.AlertType.ERROR, m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
    private void mostrarAdvertencia(String m) { Alert a = new Alert(Alert.AlertType.WARNING, m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
}