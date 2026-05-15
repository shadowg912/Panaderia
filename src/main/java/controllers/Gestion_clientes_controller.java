package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.ClienteResumen;
import model.Provincia;
import utils.AppNavigator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Gestion_clientes_controller {

    @FXML private TextField txtIdCliente;
    @FXML private TextField txtNombreCliente;
    @FXML private Button btnBuscarCliente;

    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtRnc;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;

    @FXML private ComboBox<Provincia> cmbProvincia;
    @FXML private ComboBox<Provincia> cmbFiltroProvincia;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtSector;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;
    @FXML private TextField txtReferencia;

    @FXML private TableView<ClienteResumen> tablaClientes;
    @FXML private TableColumn<ClienteResumen, Integer> colId;
    @FXML private TableColumn<ClienteResumen, String> colRazonSocial;
    @FXML private TableColumn<ClienteResumen, String> colRnc;
    @FXML private TableColumn<ClienteResumen, String> colTelefono;
    @FXML private TableColumn<ClienteResumen, String> colProvincia;

    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnVolver;

    private CONEXION conexion = new CONEXION();
    private AppNavigator appNavigator = new AppNavigator();

    private int idClienteActual;
    private int idDireccionActual;

    private ObservableList<ClienteResumen> listaClientes = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[]{
                    item.idClienteProperty(), item.razonSocialProperty(),
                    item.rncProperty(), item.telefonoProperty(), item.provinciaProperty()
            }
    );

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarProvincias();
        cargarTablaClientes();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(c -> c.getValue().idClienteProperty().asObject());
        colRazonSocial.setCellValueFactory(c -> c.getValue().razonSocialProperty());
        colRnc.setCellValueFactory(c -> c.getValue().rncProperty());
        colTelefono.setCellValueFactory(c -> c.getValue().telefonoProperty());
        colProvincia.setCellValueFactory(c -> c.getValue().provinciaProperty());
        tablaClientes.setItems(listaClientes);

        tablaClientes.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                ClienteResumen sel = tablaClientes.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    txtIdCliente.setText(String.valueOf(sel.getIdCliente()));
                    fnBuscarCliente(null);
                }
            }
        });
    }

    private void cargarProvincias() {
        ObservableList<Provincia> provincias = FXCollections.observableArrayList();
        ObservableList<Provincia> provinciasFiltro = FXCollections.observableArrayList();
        provinciasFiltro.add(new Provincia(0, "Todas las provincias"));

        String sql = "SELECT id_provincia, nombre FROM PROVINCIA ORDER BY nombre";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Provincia p = new Provincia(rs.getInt("id_provincia"), rs.getString("nombre"));
                provincias.add(p);
                provinciasFiltro.add(p);
            }
        } catch (SQLException e) {
            mostrarError("Error cargando provincias: " + e.getMessage());
        }
        cmbProvincia.setItems(provincias);
        cmbFiltroProvincia.setItems(provinciasFiltro);
        cmbFiltroProvincia.getSelectionModel().selectFirst();
    }

    private void cargarTablaClientes() {
        listaClientes.clear();
        Provincia filtroProv = cmbFiltroProvincia.getValue();

        StringBuilder sql = new StringBuilder(
            "SELECT c.id_cliente, c.razon_social, c.rnc, c.telefono, p.nombre as provincia " +
            "FROM CLIENTE c " +
            "LEFT JOIN DIRECCION d ON c.id_direccion = d.id_direccion " +
            "LEFT JOIN SECTOR s ON d.id_sector = s.id_sector " +
            "LEFT JOIN CIUDAD cd ON s.id_ciudad = cd.id_ciudad " +
            "LEFT JOIN PROVINCIA p ON cd.id_provincia = p.id_provincia WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (filtroProv != null && filtroProv.getId_provincia() > 0) {
            sql.append(" AND p.id_provincia = ? ");
            params.add(filtroProv.getId_provincia());
        }
        sql.append(" ORDER BY c.razon_social");

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaClientes.add(new ClienteResumen(
                    rs.getInt("id_cliente"),
                    rs.getString("razon_social"),
                    rs.getString("rnc"),
                    rs.getString("telefono"),
                    rs.getString("provincia")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void fnFiltrarTabla(ActionEvent event) {
        cargarTablaClientes();
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
    public void fnCambioProvincia(ActionEvent event) {}

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
                        try { ps.setInt(2, Integer.parseInt(numero)); }
                        catch (NumberFormatException e) { ps.setNull(2, Types.INTEGER); }
                    } else { ps.setNull(2, Types.INTEGER); }
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
                        try { ps.setInt(2, Integer.parseInt(numero)); }
                        catch (NumberFormatException e) { ps.setNull(2, Types.INTEGER); }
                    } else { ps.setNull(2, Types.INTEGER); }
                    ps.setString(3, referencia.isEmpty() ? null : referencia);
                    ps.setInt(4, idSector);
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) idDireccionActual = rs.getInt(1);
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
            cargarTablaClientes();
        } catch (SQLException e) {
            mostrarError("Error al guardar cambios: " + e.getMessage());
        }
    }

    private int buscarOCrearCiudad(Connection conn, String nombreCiudad, int idProvincia) throws SQLException {
        String sqlBuscar = "SELECT id_ciudad FROM CIUDAD WHERE nombre = ? AND id_provincia = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombreCiudad); ps.setInt(2, idProvincia);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_ciudad");
        }
        String sqlInsertar = "INSERT INTO CIUDAD (nombre, id_provincia) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreCiudad); ps.setInt(2, idProvincia);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private int buscarOCrearSector(Connection conn, String nombreSector, int idCiudad) throws SQLException {
        String sqlBuscar = "SELECT id_sector FROM SECTOR WHERE nombre = ? AND id_ciudad = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombreSector); ps.setInt(2, idCiudad);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_sector");
        }
        String sqlInsertar = "INSERT INTO SECTOR (nombre, id_ciudad) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreSector); ps.setInt(2, idCiudad);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private boolean validarCampos() {
        if (txtRazonSocial.getText() == null || txtRazonSocial.getText().trim().isEmpty()) {
            mostrarAdvertencia("La razón social es obligatoria."); return false;
        }
        if (txtRnc.getText() == null || txtRnc.getText().trim().isEmpty()) {
            mostrarAdvertencia("El RNC es obligatorio."); return false;
        }
        if (cmbProvincia.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar una provincia."); return false;
        }
        if (txtCiudad.getText() == null || txtCiudad.getText().trim().isEmpty()) {
            mostrarAdvertencia("La ciudad es obligatoria."); return false;
        }
        if (txtSector.getText() == null || txtSector.getText().trim().isEmpty()) {
            mostrarAdvertencia("El sector es obligatorio."); return false;
        }
        return true;
    }

    @FXML
    public void fnLimpiar() {
        txtIdCliente.clear(); txtNombreCliente.clear();
        txtRazonSocial.clear(); txtRnc.clear(); txtTelefono.clear(); txtCorreo.clear();
        txtCiudad.clear(); txtSector.clear(); txtCalle.clear(); txtNumero.clear(); txtReferencia.clear();
        cmbProvincia.setValue(null);
        idClienteActual = 0; idDireccionActual = 0;
    }

    @FXML
    public void fnVolverMenu(ActionEvent event) { appNavigator.volverMenu(); }

    private void mostrarAdvertencia(String m) { Alert a = new Alert(Alert.AlertType.WARNING, m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
    private void mostrarError(String m) { Alert a = new Alert(Alert.AlertType.ERROR, m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
    private void mostrarInfo(String m) { Alert a = new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
}