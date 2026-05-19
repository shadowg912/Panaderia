package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Provincia;
import utils.AppNavigator;

import java.sql.*;
import static utils.AlertHelper.*;

public class Guardar_direccion_controller {

    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;
    @FXML private TextField txtSector;
    @FXML private TextField txtCiudad;
    @FXML private ComboBox<Provincia> cmbProvincia;
    @FXML private TextArea txtReferencia;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<Provincia> Provincias = FXCollections.observableArrayList();

    public ObservableList cargarProvincias() {
        String sql = "SELECT id_provincia,nombre FROM PROVINCIA ";
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

    @FXML
    public void initialize() {
        cmbProvincia.setItems(cargarProvincias());
    }

    @FXML
    public void fnGuardarDireccion(ActionEvent event) {
        if (!validarCampos()) return;

        String calle = txtCalle.getText().trim();
        String numero = txtNumero.getText().trim();
        String sector = txtSector.getText().trim();
        String ciudad = txtCiudad.getText().trim();
        Provincia provincia = cmbProvincia.getValue();
        String referencia = txtReferencia.getText().trim();

        int idDireccion = insertarDireccionCompleta(calle, numero, referencia, ciudad, sector, provincia.getId_provincia());

        if (idDireccion > 0) {
            mostrarInfo("Dirección guardada exitosamente.\nID: " + idDireccion);
        } else {
            mostrarError("Error al guardar la dirección.");
        }
    }

    public int insertarDireccionCompleta(String calle, String numero, String referencia,
                                         String nombreCiudad, String nombreSector, int idProvincia) {
        int idCiudad = buscarOCrearCiudad(nombreCiudad, idProvincia);
        if (idCiudad == 0) {
            mostrarError("Error al procesar la ciudad.");
            return 0;
        }
        int idSector = buscarOCrearSector(nombreSector, idCiudad);
        if (idSector == 0) {
            mostrarError("Error al procesar el sector.");
            return 0;
        }
        return insertarDireccion(calle, numero, referencia, idSector);
    }

    private int buscarOCrearCiudad(String nombreCiudad, int idProvincia) {
        String sqlBuscar = "SELECT id_ciudad FROM CIUDAD WHERE nombre = ? AND id_provincia = ?";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombreCiudad); ps.setInt(2, idProvincia);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_ciudad");
        } catch (SQLException e) {
            mostrarError("Error buscando ciudad: " + e.getMessage());
        }
        String sqlInsertar = "INSERT INTO CIUDAD (nombre, id_provincia) VALUES (?, ?)";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreCiudad); ps.setInt(2, idProvincia);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            mostrarError("Error creando ciudad: " + e.getMessage());
        }
        return 0;
    }

    private int buscarOCrearSector(String nombreSector, int idCiudad) {
        String sqlBuscar = "SELECT id_sector FROM SECTOR WHERE nombre = ? AND id_ciudad = ?";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombreSector); ps.setInt(2, idCiudad);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_sector");
        } catch (SQLException e) {
            mostrarError("Error buscando sector: " + e.getMessage());
        }
        String sqlInsertar = "INSERT INTO SECTOR (nombre, id_ciudad) VALUES (?, ?)";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreSector); ps.setInt(2, idCiudad);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            mostrarError("Error creando sector: " + e.getMessage());
        }
        return 0;
    }

    private int insertarDireccion(String calle, String numero, String referencia, int idSector) {
        String sql = "INSERT INTO DIRECCION (calle, numero, referencia, id_sector) VALUES (?, ?, ?, ?)";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (SQLException e) {
            mostrarError("Error insertando dirección: " + e.getMessage());
        }
        return 0;
    }

    private boolean validarCampos() {
        if (txtCalle.getText() == null || txtCalle.getText().trim().isEmpty()) {
            mostrarAdvertencia("La calle es obligatoria."); return false;
        }
        if (txtNumero.getText() == null || txtNumero.getText().trim().isEmpty()) {
            mostrarAdvertencia("El número es obligatorio."); return false;
        }
        if (txtCiudad.getText() == null || txtCiudad.getText().trim().isEmpty()) {
            mostrarAdvertencia("La ciudad es obligatoria."); return false;
        }
        if (txtSector.getText() == null || txtSector.getText().trim().isEmpty()) {
            mostrarAdvertencia("El sector es obligatorio."); return false;
        }
        if (cmbProvincia.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar una provincia."); return false;
        }
        return true;
    }

    @FXML
    public void fnLimpiarFormulario(ActionEvent event) {
        txtCalle.clear(); txtNumero.clear(); txtSector.clear();
        txtCiudad.clear(); cmbProvincia.setValue(null); txtReferencia.clear();
    }

    @FXML
    public void fnVolverMenu(ActionEvent event) { appNavigator.volverMenu(); }

}