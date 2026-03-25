package controllers;

import Data_base.CONEXION;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import model.Empresa_cliente;

import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class Regisro_empresa_controller {
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
    public void Guardar(String nombre,String rnc,String telefono, String correo) {
        String sql = "INSERT INTO EMPRESA VALUES(?,?,?,?)";
        try (Connection connection = conexion.establecerconexio(); PreparedStatement ps=connection.prepareStatement(sql)) {
            ps.setString(1,nombre);
            ps.setString(2, rnc);
            ps.setString(3,telefono);
            ps.setString(4,correo);
            }catch (Exception e){

        }
        limipar();
    }
    public void fnGuardar(java.awt.event.ActionEvent actionEvent){
        String Nombre = this.txtNombreEmpresa.getText().trim();
        String rnc = this.txtRNC.getText().trim();
        String telefono = this.txtTelefono.getText().trim();
        String correo = this.txtCorreo.getText().trim();

        Guardar(Nombre,rnc,telefono,correo);

    }

}