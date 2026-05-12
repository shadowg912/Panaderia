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

public class Gestion_clientes_controller {

    @FXML private TextField txtIdCliente;
    @FXML private TextField txtNombreCliente;
    @FXML private Button btnBuscarCliente;

    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtRnc;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;

    @FXML private ComboBox<Provincia> cmbProvincia;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtSector;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;
    @FXML private TextField txtReferencia;

    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnVolver;

    private CONEXION conexion = new CONEXION();
    private AppNavigator appNavigator = new AppNavigator();

    private int idClienteActual;
    private int idDireccionActual;

    @FXML
    public void initialize() {
        cargarProvincias();
    }

    private void cargarProvincias() {
        ObservableList<Provincia> provincias = FXCollections.observableArrayList();
        String sql = "SELECT id_provincia, nombre FROM PROVINCIA ORDER BY nombre";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                provincias.add(new Provincia(rs.getInt("id_provincia"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            System.out.println("Error cargando provincias: " + e.getMessage());
        }
        cmbProvincia.setItems(provincias);
    }

    @FXML
    public void fnBuscarCliente(ActionEvent event) {
        String idTexto = txtIdCliente.getText().trim();
        if (idTexto.isEmpty()) {
            mostrarAdvertencia("Ingrese el ID del cliente.");
            return;
        }

        int idCliente;
        try {
            idCliente = Integer.parseInt(idTexto);
        } catch (NumberFormatException e) {
            mostrarAdvertencia("El ID debe ser un número válido.");
            return;
        }

        String sql = "SELECT c.id_cliente, c.razon_social, c.rnc, c.telefono, c.correo_electronico, " +
                "d.id_direccion, d.calle, d.numero, d.referencia, " +
                "s.nombre as sector_nombre, cd.nombre as ciudad_nombre, " +
                "p.id_provincia " +
                "FROM CLIENTE c " +
                "LEFT JOIN DIRECCION d ON c.id_direccion = d.id_direccion " +
                "LEFT JOIN SECTOR s ON d.id_sector = s.id_sector " +
                "LEFT JOIN CIUDAD cd ON s.id_ciudad = cd.id_ciudad " +
                "LEFT JOIN PROVINCIA p ON cd.id_provincia = p.id_provincia " +
                "WHERE c.id_cliente = ?";

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                idClienteActual = rs.getInt("id_cliente");
                txtNombreCliente.setText(rs.getString("razon_social"));
                txtRazonSocial.setText(rs.getString("razon_social"));
                txtRnc.setText(rs.getString("rnc") != null ? rs.getString("rnc") : "");
                txtTelefono.setText(rs.getString("telefono") != null ? rs.getString("telefono") : "");
                txtCorreo.setText(rs.getString("correo_electronico") != null ? rs.getString("correo_electronico") : "");

                idDireccionActual = rs.getInt("id_direccion");
                txtCalle.setText(rs.getString("calle") != null ? rs.getString("calle") : "");
                rs.getInt("numero");
                txtNumero.setText(rs.wasNull() ? "" : String.valueOf(rs.getInt("numero")));
                txtReferencia.setText(rs.getString("referencia") != null ? rs.getString("referencia") : "");

                txtCiudad.setText(rs.getString("ciudad_nombre") != null ? rs.getString("ciudad_nombre") : "");
                txtSector.setText(rs.getString("sector_nombre") != null ? rs.getString("sector_nombre") : "");

                int idProvincia = rs.getInt("id_provincia");
                if (!rs.wasNull() && idProvincia > 0) {
                    for (Provincia p : cmbProvincia.getItems()) {
                        if (p.getId_provincia() == idProvincia) {
                            cmbProvincia.setValue(p);
                            break;
                        }
                    }
                }
            } else {
                mostrarAdvertencia("No se encontró ningún cliente con ese ID.");
                fnLimpiar();
            }
        } catch (SQLException e) {
            mostrarError("Error al buscar cliente: " + e.getMessage());
        }
    }

    @FXML
    public void fnCambioProvincia(ActionEvent event) {
        // No se necesita lógica adicional, ciudad/sector son TextFields
    }

    @FXML
    public void fnGuardarCambios(ActionEvent event) {
        if (idClienteActual == 0) {
            mostrarAdvertencia("Debe buscar un cliente antes de guardar cambios.");
            return;
        }
        if (!validarCampos()) return;

        String razonSocial = txtRazonSocial.getText().trim();
        String rnc = txtRnc.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String ciudadNombre = txtCiudad.getText().trim();
        String sectorNombre = txtSector.getText().trim();
        String calle = txtCalle.getText().trim();
        String numero = txtNumero.getText().trim();
        String referencia = txtReferencia.getText().trim();

        try (Connection conn = conexion.establecerconexio()) {
            conn.setAutoCommit(false);

            Provincia provincia = cmbProvincia.getValue();
            int idCiudad = buscarOCrearCiudad(conn, ciudadNombre, provincia.getId_provincia());
            int idSector = buscarOCrearSector(conn, sectorNombre, idCiudad);

            if (idDireccionActual > 0) {
                String sqlDir = "UPDATE DIRECCION SET calle=?, numero=?, referencia=?, id_sector=? WHERE id_direccion=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlDir)) {
                    ps.setString(1, calle);
                    if (!numero.isEmpty()) {
                        try {
                            ps.setInt(2, Integer.parseInt(numero));
                        } catch (NumberFormatException e) {
                            ps.setNull(2, Types.INTEGER);
                        }
                    } else {
                        ps.setNull(2, Types.INTEGER);
                    }
                    ps.setString(3, referencia.isEmpty() ? null : referencia);
                    ps.setInt(4, idSector);
                    ps.setInt(5, idDireccionActual);
                    ps.executeUpdate();
                }
            } else {
                String sqlDir = "INSERT INTO DIRECCION (calle, numero, referencia, id_sector) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlDir, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, calle);
                    if (!numero.isEmpty()) {
                        try {
                            ps.setInt(2, Integer.parseInt(numero));
                        } catch (NumberFormatException e) {
                            ps.setNull(2, Types.INTEGER);
                        }
                    } else {
                        ps.setNull(2, Types.INTEGER);
                    }
                    ps.setString(3, referencia.isEmpty() ? null : referencia);
                    ps.setInt(4, idSector);
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        idDireccionActual = rs.getInt(1);
                    }
                }
            }

            String sqlCli = "UPDATE CLIENTE SET razon_social=?, rnc=?, telefono=?, correo_electronico=?, id_direccion=? WHERE id_cliente=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlCli)) {
                ps.setString(1, razonSocial);
                ps.setString(2, rnc.isEmpty() ? null : rnc);
                ps.setString(3, telefono.isEmpty() ? null : telefono);
                ps.setString(4, correo.isEmpty() ? null : correo);
                ps.setInt(5, idDireccionActual);
                ps.setInt(6, idClienteActual);
                ps.executeUpdate();
            }

            conn.commit();
            txtNombreCliente.setText(razonSocial);
            mostrarInfo("Cliente actualizado exitosamente.");
        } catch (SQLException e) {
            mostrarError("Error al guardar cambios: " + e.getMessage());
        }
    }

    private int buscarOCrearCiudad(Connection conn, String nombreCiudad, int idProvincia) throws SQLException {
        String sqlBuscar = "SELECT id_ciudad FROM CIUDAD WHERE nombre = ? AND id_provincia = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombreCiudad);
            ps.setInt(2, idProvincia);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_ciudad");
            }
        }

        String sqlInsertar = "INSERT INTO CIUDAD (nombre, id_provincia) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreCiudad);
            ps.setInt(2, idProvincia);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private int buscarOCrearSector(Connection conn, String nombreSector, int idCiudad) throws SQLException {
        String sqlBuscar = "SELECT id_sector FROM SECTOR WHERE nombre = ? AND id_ciudad = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombreSector);
            ps.setInt(2, idCiudad);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_sector");
            }
        }

        String sqlInsertar = "INSERT INTO SECTOR (nombre, id_ciudad) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreSector);
            ps.setInt(2, idCiudad);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private boolean validarCampos() {
        if (txtRazonSocial.getText() == null || txtRazonSocial.getText().trim().isEmpty()) {
            mostrarAdvertencia("La razón social es obligatoria.");
            return false;
        }
        if (txtRnc.getText() == null || txtRnc.getText().trim().isEmpty()) {
            mostrarAdvertencia("El RNC es obligatorio.");
            return false;
        }
        if (cmbProvincia.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar una provincia.");
            return false;
        }
        if (txtCiudad.getText() == null || txtCiudad.getText().trim().isEmpty()) {
            mostrarAdvertencia("La ciudad es obligatoria.");
            return false;
        }
        if (txtSector.getText() == null || txtSector.getText().trim().isEmpty()) {
            mostrarAdvertencia("El sector es obligatorio.");
            return false;
        }
        return true;
    }

    @FXML
    public void fnLimpiar() {
        txtIdCliente.clear();
        txtNombreCliente.clear();
        txtRazonSocial.clear();
        txtRnc.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        txtCiudad.clear();
        txtSector.clear();
        txtCalle.clear();
        txtNumero.clear();
        txtReferencia.clear();
        cmbProvincia.setValue(null);
        idClienteActual = 0;
        idDireccionActual = 0;
    }

    @FXML
    public void fnVolverMenu(ActionEvent event) {
        appNavigator.volverMenu();
    }

    private void mostrarAdvertencia(String mensaje) {
        Alert a = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}