package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Cliente;
import model.Provincia;
import utils.AppNavigator;

import java.sql.*;

public class Regisro_empresa_controller {

    @FXML private ComboBox<Provincia> cmbProvincia;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtSector;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;
    @FXML private TextField txtReferencia;
    @FXML private Button btnLimpiar;
    @FXML private Button btnVolverMenu;
    @FXML private TextField txtNombreEmpresa;
    @FXML private TextField txtRNC;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private Button btnGuardar;

    AppNavigator appNavigator = new AppNavigator();
    CONEXION conexion = new CONEXION();
    ObservableList<Provincia> Provincias = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cmbProvincia.setItems(cargarprovincias());
    }

    public void limipar() {
        txtNombreEmpresa.setText(""); txtRNC.setText(""); txtTelefono.setText(""); txtCorreo.setText("");
        txtCalle.setText(""); txtNumero.setText(""); txtSector.setText("");
        txtCiudad.setText(""); txtReferencia.setText("");
        cmbProvincia.setValue(null);
    }

    @FXML
    public void Guardarempresa(String nombre, String rnc, String telefono, String correo, int idDireccion) {
        String sql = "INSERT INTO CLIENTE(razon_social, rnc, telefono, correo_electronico, id_direccion) VALUES(?,?,?,?,?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nombre); ps.setString(2, rnc); ps.setString(3, telefono); ps.setString(4, correo); ps.setInt(5, idDireccion);
            ps.executeUpdate();
            mostrarInfo("Cliente guardado exitosamente.");
        } catch (SQLException e) {
            mostrarError("Error al guardar cliente: " + e.getMessage());
        }
    }

    public void fnVolvermenu(ActionEvent actionEvent) { appNavigator.volverMenu(); }

    public void fnGuardar(ActionEvent actionEvent) {
        String nombre = txtNombreEmpresa.getText().trim();
        String rnc = txtRNC.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String calle = txtCalle.getText().trim();
        String numero = txtNumero.getText().trim();
        String sector = txtSector.getText().trim();
        String ciudad = txtCiudad.getText().trim();
        String referencia = txtReferencia.getText().trim();

        if (cmbProvincia.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar una provincia.");
            return;
        }

        int idProvincia = cmbProvincia.getValue().getId_provincia();
        int idDireccion = insertarDireccionCompleta(calle, numero, referencia, ciudad, sector, idProvincia);

        if (idDireccion == 0) {
            mostrarError("Error al guardar la dirección.");
            return;
        }

        Guardarempresa(nombre, rnc, telefono, correo, idDireccion);
        limipar();
    }

    public void fnLimpiar(ActionEvent actionEvent) { limipar(); }

    public ObservableList cargarprovincias() {
        String sql = "SELECT id_provincia, nombre FROM PROVINCIA ORDER BY nombre";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Provincias.add(new Provincia(rs.getInt(1), rs.getString("nombre")));
            }
        } catch (Exception e) {
            mostrarError("Error cargando provincias: " + e.getMessage());
        }
        return Provincias;
    }

    private int insertarDireccionCompleta(String calle, String numero, String referencia,
                                          String nombreCiudad, String nombreSector, int idProvincia) {
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
            ps.setString(1, nombreCiudad); ps.setInt(2, idProvincia);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_ciudad");
        } catch (Exception e) {
            mostrarError("Error buscando ciudad: " + e.getMessage());
        }
        String sqlInsertar = "INSERT INTO CIUDAD (nombre, id_provincia) VALUES (?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreCiudad); ps.setInt(2, idProvincia);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            mostrarError("Error creando ciudad: " + e.getMessage());
        }
        return 0;
    }

    private int buscarOCrearSector(String nombreSector, int idCiudad) {
        String sqlBuscar = "SELECT id_sector FROM SECTOR WHERE nombre = ? AND id_ciudad = ?";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombreSector); ps.setInt(2, idCiudad);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_sector");
        } catch (Exception e) {
            mostrarError("Error buscando sector: " + e.getMessage());
        }
        String sqlInsertar = "INSERT INTO SECTOR (nombre, id_ciudad) VALUES (?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreSector); ps.setInt(2, idCiudad);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            mostrarError("Error creando sector: " + e.getMessage());
        }
        return 0;
    }

    private int insertarDireccion(String calle, String numero, String referencia, int idSector) {
        String sql = "INSERT INTO DIRECCION (calle, numero, referencia, id_sector) VALUES (?, ?, ?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, calle);
            if (numero != null && !numero.isEmpty()) {
                try { ps.setInt(2, Integer.parseInt(numero)); }
                catch (NumberFormatException e) { ps.setNull(2, Types.INTEGER); }
            } else { ps.setNull(2, Types.INTEGER); }
            ps.setString(3, referencia != null && !referencia.isEmpty() ? referencia : null);
            ps.setInt(4, idSector);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            mostrarError("Error insertando dirección: " + e.getMessage());
        }
        return 0;
    }

    private void mostrarInfo(String m) { Alert a = new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
    private void mostrarError(String m) { Alert a = new Alert(Alert.AlertType.ERROR, m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
    private void mostrarAdvertencia(String m) { Alert a = new Alert(Alert.AlertType.WARNING, m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
}