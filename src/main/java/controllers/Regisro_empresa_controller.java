package controllers;

import Data_base.CONEXION;
import javafx.beans.value.ObservableNumberValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import model.Empresa_cliente;
import model.Provincia;
import utils.AppNavigator;

import java.sql.*;

public class Regisro_empresa_controller {

    @FXML
    private ComboBox<Provincia> cmbProvincia;
    @FXML
    private TextField txtCiudad;
    @FXML
    private TextField txtSector;
    @FXML
    private TextField txtCalle;
    @FXML
    private TextField txtNumero;
    @FXML
    private TextField txtReferencia;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnVolverMenu;
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


    Connection connection = null;
    public void Ejecutarsql(String sql) {
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void limipar() {
        this.txtNombreEmpresa.setText("");
        this.txtRNC.setText("");
        this.txtTelefono.setText("");
        this.txtCorreo.setText("");
    }

    @FXML
    public void Guardarempresa(String nombre, String rnc, String telefono, String correo, String id_direccion) {
        String sql = "INSERT INTO EMPRESA_CLIENTE(razon_social,rnc,telefono,correo_electronico,id_direccion) VALUES(?,?,?,?,?)";
        try (Connection connection = conexion.establecerconexio(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, rnc);
            ps.setString(3, telefono);
            ps.setString(4, correo);
            ps.setString(5, id_direccion);

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

        String calle = txtCalle.getText().trim();
        String numero = txtNumero.getText().trim();
        String sector = txtSector.getText().trim();
        String ciudad = txtCiudad.getText().trim();
        int idProvincia = cmbProvincia.getValue().getId_provincia();
        String referencia = txtReferencia.getText().trim();


        int idDireccion = insertarDireccionCompleta(calle, numero, referencia, ciudad, sector, idProvincia);

        if (idDireccion == 0) {
            System.out.println(" Error al guardar dirección");
            return;
        }
        Guardarempresa(Nombre, rnc, telefono, correo, "1");
        limipar();
    }

    public void fnLimpiar(ActionEvent actionEvent) {
        limipar();
    }

    public ObservableList cargarprovincias() {
        String sql = "SELECT id_provincia, nombre FROM PROVINCIA ORDER BY nombre";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Provincias.add(new Provincia(rs.getInt(1), rs.getString("nombre")));
            }
        } catch (Exception e) {

        }
        return Provincias;
    }
    ObservableList<Provincia> Provincias = FXCollections.observableArrayList();
    @FXML
    public void initialize(){
        cmbProvincia.setItems(cargarprovincias());

    }

    private int insertarDireccionCompleta(String calle, String numero, String referencia,
                                          String nombreCiudad, String nombreSector,
                                          int idProvincia) {

        int idCiudad = buscarOCrearCiudad(nombreCiudad, idProvincia);
        if (idCiudad == 0) return 0;

        int idSector = buscarOCrearSector(nombreSector, idCiudad);
        if (idSector == 0) return 0;

        return insertarDireccion(calle, numero, referencia, idSector);
    }

    private int buscarOCrearCiudad(String nombreCiudad, int idProvincia) {
        String sqlBuscar = "SELECT id_ciudad FROM CIUDAD WHERE nombre = ? AND id_provincia = ?";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sqlBuscar)) {

            ps.setString(1, nombreCiudad);
            ps.setInt(2, idProvincia);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_ciudad");
            }
        } catch (Exception e) {
            System.out.println("Error buscando ciudad: " + e.getMessage());
        }

        String sqlInsertar = "INSERT INTO CIUDAD (nombre, id_provincia) VALUES (?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombreCiudad);
            ps.setInt(2, idProvincia);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                System.out.println("Ciudad creada: " + nombreCiudad);
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("Error creando ciudad: " + e.getMessage());
        }
        return 0;
    }

    private int buscarOCrearSector(String nombreSector, int idCiudad) {
        String sqlBuscar = "SELECT id_sector FROM SECTOR WHERE nombre = ? AND id_ciudad = ?";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sqlBuscar)) {

            ps.setString(1, nombreSector);
            ps.setInt(2, idCiudad);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_sector");
            }
        } catch (Exception e) {
            System.out.println("Error buscando sector: " + e.getMessage());
        }

        String sqlInsertar = "INSERT INTO SECTOR (nombre, id_ciudad) VALUES (?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombreSector);
            ps.setInt(2, idCiudad);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                System.out.println(" Sector creado: " + nombreSector);
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("Error creando sector: " + e.getMessage());
        }
        return 0;
    }

    private int insertarDireccion(String calle, String numero, String referencia, int idSector) {
        String sql = "INSERT INTO DIRECCION (calle, numero, referencia, id_sector) VALUES (?, ?, ?, ?)";

        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, calle);

            if (numero != null && !numero.isEmpty()) {
                try {
                    ps.setInt(2, Integer.parseInt(numero));
                } catch (NumberFormatException e) {
                    ps.setNull(2, Types.INTEGER);
                }
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            if (referencia != null && !referencia.isEmpty()) {
                ps.setString(3, referencia);
            } else {
                ps.setNull(3, Types.VARCHAR);
            }

            ps.setInt(4, idSector);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            System.out.println("❌ Error insertando dirección: " + e.getMessage());
        }
        return 0;
    }

}
