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
import model.Unidad;
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

    public void GuardarProducto(ActionEvent actionEvent) {
        try {
            String nombre = txtNombre.getText();
            Unidad unidad = cmbUnidad.getValue();
            CategoriaProducto categoria = cmbCategoria.getValue();

            // Convertir String del TextField a float
            float precio = Float.parseFloat(txtPrecio.getText());

            InsertarProducto(nombre, unidad, precio, categoria);

        } catch (NumberFormatException e) {
            System.out.println("❌ El precio debe ser un número válido");
        }
    }
}
