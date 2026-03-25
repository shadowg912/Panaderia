package controllers;

import Data_base.CONEXION;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import model.Empresa_cliente;
import utils.AppNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Regisro_empresa_controller {
    @FXML
    private Button btnLimpiar;
    @FXML private Button btnVolverMenu;
    AppNavigator appNavigator = new AppNavigator();
    CONEXION conexion = new CONEXION();
    @FXML
    private TextField txtNombreEmpresa;
    @FXML
    private TextField txtRNC;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtCorreo;

    @FXML
    private Button btnGuardar;

    public void Ejecutarsql(String sql) {
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void limipar(){
        this.txtNombreEmpresa.setText("");
        this.txtRNC.setText("");
        this.txtTelefono.setText("");
        this.txtCorreo.setText("");
    }
    @FXML
    public void Guardar(String nombre,String rnc,String telefono, String correo,String id_direccion) {
        String sql = "INSERT INTO EMPRESA_CLIENTE(razon_social,rnc,telefono,correo_electronico,id_direccion) VALUES(?,?,?,?,?)";
        try (Connection connection = conexion.establecerconexio(); PreparedStatement ps=connection.prepareStatement(sql)) {
            ps.setString(1,nombre);
            ps.setString(2, rnc);
            ps.setString(3,telefono);
            ps.setString(4,correo);
            ps.setString(5,id_direccion);

            int filasAfectadas = ps.executeUpdate();

        } catch (SQLException e) {
            System.err.printf("Error al guardar: %s%n", e.getMessage());

        }
        limipar();
    }

    public void fnVolvermenu(javafx.event.ActionEvent actionEvent) {
        appNavigator.volverMenu();
    }


    public void fnGuardar(javafx.event.ActionEvent actionEvent) {
        String Nombre = this.txtNombreEmpresa.getText().trim();
        String rnc = this.txtRNC.getText().trim();
        String telefono = this.txtTelefono.getText().trim();
        String correo = this.txtCorreo.getText().trim();

        Guardar(Nombre,rnc,telefono,correo,"1");
        limipar();
    }

    public void fnLimpiar(ActionEvent actionEvent) {
        limipar();
    }
}
