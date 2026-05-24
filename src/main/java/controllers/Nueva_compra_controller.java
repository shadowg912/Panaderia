package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.*;
import utils.AppNavigator;
import java.sql.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import static utils.AlertHelper.*;

public class Nueva_compra_controller {

    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private ComboBox<FormaPago> cmbFormaPago;
    @FXML private Spinner<Integer> spDia;
    @FXML private Spinner<Integer> spMes;
    @FXML private Label lblAnio;
    @FXML private TextField txtEstado;
    @FXML private Button btnVolver;
    @FXML private Button btnSiguiente;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<Proveedor> proveedores = FXCollections.observableArrayList();
    ObservableList<FormaPago> formasPago = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cmbProveedor.setItems(cargarProveedores());
        cmbFormaPago.setItems(cargarFormasPago());
        txtEstado.setText("PENDIENTE");
        configurarSpinners();
    }

    private void configurarSpinners() {
        LocalDate hoy = LocalDate.now();
        spDia.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, hoy.getDayOfMonth()));
        spDia.setEditable(true);
        spMes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, hoy.getMonthValue()));
        spMes.setEditable(true);
        lblAnio.setText(String.valueOf(hoy.getYear()));
    }

    private LocalDate obtenerFechaSpinners() {
        try {
            return LocalDate.of(LocalDate.now().getYear(), spMes.getValue(), spDia.getValue());
        } catch (DateTimeException e) {
            return null;
        }
    }

    private ObservableList<Proveedor> cargarProveedores() {
        String sql = "SELECT id_proveedor, nombre FROM PROVEEDOR ORDER BY nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setIdProveedor(rs.getInt("id_proveedor"));
                p.setNombre(rs.getString("nombre"));
                proveedores.add(p);
            }
        } catch (Exception e) {
            mostrarError("Error cargando proveedores: " + e.getMessage());
        }
        return proveedores;
    }

    private ObservableList<FormaPago> cargarFormasPago() {
        String sql = "SELECT id_forma_pago, nombre FROM FORMA_PAGO ORDER BY nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                formasPago.add(new FormaPago(rs.getInt(1), rs.getString("nombre")));
            }
        } catch (Exception e) {
            mostrarError("Error cargando formas de pago: " + e.getMessage());
        }
        return formasPago;
    }

    @FXML
    public void fnSiguiente(ActionEvent event) {
        if (!validarCampos()) return;

        int idProveedor = cmbProveedor.getValue().getIdProveedor();
        int idFormaPago = cmbFormaPago.getValue().getIdFormaPago();
        LocalDate fecha = obtenerFechaSpinners();
        if (fecha == null) {
            mostrarAdvertencia("La fecha no es válida.");
            return;
        }
        if (fecha.isBefore(LocalDate.now())) {
            mostrarAdvertencia("La fecha no puede ser anterior a hoy.");
            return;
        }

        int idCompra = insertarCompra(idProveedor, idFormaPago, java.sql.Date.valueOf(fecha));

        if (idCompra > 0) {
            CompraEstado.idCompraMaterial = idCompra;
            CompraEstado.idProveedor = idProveedor;
            CompraEstado.nombreProveedor = cmbProveedor.getValue().getNombre();
            CompraEstado.idFormaPago = idFormaPago;
            CompraEstado.nombreFormaPago = cmbFormaPago.getValue().getNombre();
            appNavigator.load("/view/Detalle_compra.fxml");
        } else {
            mostrarError("Error al crear la compra.");
        }
    }

    private int insertarCompra(int idProveedor, int idFormaPago, java.sql.Date fecha) {
        String sql = "INSERT INTO COMPRA_MATERIAL (fecha, estado, id_forma_pago, id_proveedor) VALUES (?, 'PENDIENTE', ?, ?)";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, fecha);
            ps.setInt(2, idFormaPago);
            ps.setInt(3, idProveedor);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            mostrarError("Error insertando compra: " + e.getMessage());
        }
        return 0;
    }

    private boolean validarCampos() {
        if (cmbProveedor.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar un proveedor.");
            return false;
        }
        if (cmbFormaPago.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar una forma de pago.");
            return false;
        }
        if (obtenerFechaSpinners() == null) {
            mostrarAdvertencia("La fecha ingresada no es válida.");
            return false;
        }
        return true;
    }

    @FXML
    public void fnVolver(ActionEvent event) {
        appNavigator.volverMenu();
    }

}
