package controllers;

import Data_base.CONEXION;
import javafx.beans.value.ObservableNumberValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import model.CategoriaProducto;
import model.Producto;
import model.Unidad;
import model.ValorNutricional;
import utils.AppNavigator;

import java.math.BigDecimal;
import java.sql.*;

public class Registro_producto_controller {
    @FXML
    private TextField txtEnergia;
    @FXML
    private TextField txtProteinas;
    @FXML
    private TextField txtCarbohidratos;
    @FXML
    private TextField txtGrasas;
    @FXML
    private TextField txtFibra;
    @FXML
    private TextField txtSodio;
    @FXML private TextField txtAzucares;
    @FXML private TextField txtGrasasSaturadas;
    @FXML private TextField txtGrasasTrans;
    @FXML

    private TextField txtPrecio;
    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<CategoriaProducto> cmbCategoria;
    @FXML
    private ComboBox<Unidad> cmbUnidad;
    CONEXION conexion = new CONEXION();
    Connection connection = null;
    AppNavigator appNavigator = new AppNavigator();



    public void fnlimpiar() {
        txtEnergia.clear();
        txtProteinas.clear();
        txtCarbohidratos.clear();
        txtGrasas.clear();
        txtFibra.clear();
        txtSodio.clear();
        txtAzucares.clear();
        txtGrasasSaturadas.clear();
        txtGrasasTrans.clear();
        txtPrecio.clear();
        txtNombre.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbUnidad.getSelectionModel().clearSelection();


    }
    public void Ejecutarsql(String sql) {
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void fnVolerMenu(ActionEvent actionEvent) {
        appNavigator.volverMenu();
    }

    public ObservableList cargarUnidad(){
        String sql= "Select id_unidad,nombre from UNIDAD";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)){
            ResultSet rs=ps.executeQuery();
            while (rs.next()){
                Unidades.add(new Unidad(rs.getInt(1),rs.getString("nombre")));
            }
        }catch (Exception e) {

        }
        return Unidades;
    }

    public ObservableList CargarCategoria(){
        String sql= "select id_categoria_producto,nombre from CATEGORIA_PRODUCTO";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                Categoria.add(new CategoriaProducto(rs.getInt(1), rs.getString("nombre")));
        }catch (Exception e){

        }
        return Categoria;
    }
    ObservableList<CategoriaProducto> Categoria = FXCollections.observableArrayList();
    ObservableList<Unidad> Unidades= FXCollections.observableArrayList();
    @FXML
    public void initialize(){
        cmbUnidad.setItems(cargarUnidad());
        cmbCategoria.setItems(CargarCategoria());

    }

    public void InsertarProducto(String nombre, Unidad unidad, float precio, CategoriaProducto categoria){
        String sql="INSERT INTO PRODUCTO(nombre,id_categoria_producto, precio_unitario, id_unidad)values(?,?,?,?) ";
        try (Connection connection = conexion.establecerconexio(); PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, nombre);
            ps.setInt(2,categoria.getIdCategoriaProducto());
            ps.setBigDecimal(3, new BigDecimal(precio));
            ps.setInt(4,unidad.getIdUnidad());
            ps.executeUpdate();
            System.out.println("Producto insertado correctamente");
        }catch (SQLException e){
            System.out.println(e.getErrorCode());
        }

    }

    public void fnGuardarProducto(ActionEvent actionEvent) {
        try {
            String nombre = txtNombre.getText();
            Unidad unidad = cmbUnidad.getValue();
            CategoriaProducto categoria = cmbCategoria.getValue();

            int idProducto = obtenerUltimoIdProducto();
            float precio = Float.parseFloat(txtPrecio.getText());
            Producto producto = new Producto();
            producto.setIdProducto(idProducto);
            producto.setNombre(nombre);


            ValorNutricional vn = new ValorNutricional();
            vn.setProducto(producto);
            vn.setCalorias(txtEnergia.getText());
            vn.setProteinas(txtProteinas.getText());
            vn.setCarbohidratos(txtCarbohidratos.getText());
            vn.setGrasasTotales(txtGrasas.getText());
            vn.setFibraDietetica(txtFibra.getText());
            vn.setSodio(txtSodio.getText());
            vn.setAzucares(txtAzucares.getText());
            vn.setGrasasSaturadas(txtGrasasSaturadas.getText());
            vn.setGrasasTrans(txtGrasasTrans.getText());

            insertarValorNutricional(vn);

            InsertarProducto(nombre, unidad, precio, categoria);
            fnlimpiar();

        } catch (NumberFormatException e) {
            System.out.println(" El precio debe ser un número válido");
        }
    }

    public void insertarValorNutricional(ValorNutricional vn) {
        String sql = "INSERT INTO VALOR_NUTRICIONAL (" +
                "id_producto, calorias, grasas_totales, grasas_saturadas, " +
                "grasas_trans, carbohidratos, azucares, proteinas, fibra_dietetica, sodio) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, vn.getIdProducto());
            ps.setString(2, vn.getCalorias());
            ps.setString(3, vn.getGrasasTotales());
            ps.setString(4, vn.getGrasasSaturadas());
            ps.setString(5, vn.getGrasasTrans());
            ps.setString(6, vn.getCarbohidratos());
            ps.setString(7, vn.getAzucares());
            ps.setString(8, vn.getProteinas());
            ps.setString(9, vn.getFibraDietetica());
            ps.setString(10, vn.getSodio());

            ps.executeUpdate();
            System.out.println(" Valor nutricional insertado correctamente");

        } catch (SQLException e) {
            System.out.println(" Error al insertar: " + e.getMessage());
            System.out.println("Código de error: " + e.getErrorCode());
        }
    }
    private int obtenerUltimoIdProducto() {
        String sql = "SELECT MAX(id_producto) FROM PRODUCTO";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener ID: " + e.getMessage());
        }
        return 0;
    }
}
