package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import model.CategoriaProveedor;
import model.Provincia;
import utils.AppNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class Registro_proveedor_controller {

    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<CategoriaProveedor> cmbCategoria;
    @FXML private ComboBox<Provincia> cmbProvincia;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtSector;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;
    @FXML private TextField txtReferencia;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<CategoriaProveedor> Categorias = FXCollections.observableArrayList();
    ObservableList<Provincia> Provincias = FXCollections.observableArrayList();

    public ObservableList cargarCategorias() {
        String sql = "SELECT id_categoria_proveedor, nombre FROM CATEGORIA_PROVEEDOR ORDER BY nombre";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Categorias.add(new CategoriaProveedor(rs.getInt(1), rs.getString("nombre")));
            }
        } catch (Exception e) {
            System.out.println("Error cargando categorias: " + e.getMessage());
        }
        return Categorias;
    }

    public ObservableList cargarProvincias() {
        String sql = "SELECT id_provincia, nombre FROM PROVINCIA ORDER BY nombre";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Provincias.add(new Provincia(rs.getInt(1), rs.getString("nombre")));
            }
        } catch (Exception e) {
            System.out.println("Error cargando provincias: " + e.getMessage());
        }
        return Provincias;
    }

    @FXML
    public void initialize() {
        cmbCategoria.setItems(cargarCategorias());
        cmbProvincia.setItems(cargarProvincias());
    }

    @FXML
    public void fnGuardarProveedor(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        int idCategoria = cmbCategoria.getValue().getIdCategoriaProveedor();

        String calle = txtCalle.getText().trim();
        String numero = txtNumero.getText().trim();
        String sector = txtSector.getText().trim();
        String ciudad = txtCiudad.getText().trim();
        int idProvincia = cmbProvincia.getValue().getId_provincia();
        String referencia = txtReferencia.getText().trim();

        int idDireccion = insertarDireccionCompleta(calle, numero, referencia, ciudad, sector, idProvincia);
        if (idDireccion == 0) {
            System.out.println("Error al guardar direccion");
            return;
        }

        int idProveedor = insertarProveedor(nombre, telefono, correo, idCategoria, idDireccion);
        if (idProveedor > 0) {
            System.out.println("Proveedor guardado exitosamente con ID: " + idProveedor);
            fnLimpiar();
        } else {
            System.out.println("Error al guardar proveedor");
        }
    }

    public int insertarDireccionCompleta(String calle, String numero, String referencia,
                                         String nombreCiudad, String nombreSector,
                                         int idProvincia) {
        int idCiudad = buscarOCrearCiudad(nombreCiudad, idProvincia);
        if (idCiudad == 0) {
            System.out.println("Error al procesar ciudad");
            return 0;
        }

        int idSector = buscarOCrearSector(nombreSector, idCiudad);
        if (idSector == 0) {
            System.out.println("Error al procesar sector");
            return 0;
        }

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
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
                System.out.println("Sector creado: " + nombreSector);
                return rs.getInt(1);
            }
        } catch (SQLException e) {
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
        } catch (SQLException e) {
            System.out.println("Error insertando direccion: " + e.getMessage());
        }
        return 0;
    }

    private int insertarProveedor(String nombre, String telefono, String correo,
                                  int idCategoria, int idDireccion) {
        String sql = "INSERT INTO PROVEEDOR (nombre, numero_telefono, correo_electronico, id_categoria_proveedor, id_direccion) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setString(3, correo);
            ps.setInt(4, idCategoria);
            ps.setInt(5, idDireccion);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error insertando proveedor: " + e.getMessage());
        }
        return 0;
    }

    private boolean validarCampos() {
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            System.out.println("El nombre es obligatorio");
            return false;
        }
        if (txtTelefono.getText() == null || txtTelefono.getText().trim().isEmpty()) {
            System.out.println("El telefono es obligatorio");
            return false;
        }
        if (cmbCategoria.getValue() == null) {
            System.out.println("Debe seleccionar una categoria");
            return false;
        }
        if (cmbProvincia.getValue() == null) {
            System.out.println("Debe seleccionar una provincia");
            return false;
        }
        if (txtCiudad.getText() == null || txtCiudad.getText().trim().isEmpty()) {
            System.out.println("La ciudad es obligatoria");
            return false;
        }
        if (txtSector.getText() == null || txtSector.getText().trim().isEmpty()) {
            System.out.println("El sector es obligatorio");
            return false;
        }
        if (txtCalle.getText() == null || txtCalle.getText().trim().isEmpty()) {
            System.out.println("La calle es obligatoria");
            return false;
        }
        if (txtNumero.getText() == null || txtNumero.getText().trim().isEmpty()) {
            System.out.println("El numero es obligatorio");
            return false;
        }
        return true;
    }

    @FXML
    public void fnLimpiar() {
        txtNombre.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        cmbCategoria.setValue(null);
        cmbProvincia.setValue(null);
        txtCiudad.clear();
        txtSector.clear();
        txtCalle.clear();
        txtNumero.clear();
        txtReferencia.clear();
        System.out.println("Formulario limpiado");
    }

    @FXML
    public void fnLimpiar(ActionEvent event) {
        fnLimpiar();
    }

    @FXML
    public void fnVolverMenu(ActionEvent event) {
        appNavigator.volverMenu();
    }
}