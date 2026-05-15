package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import model.Cliente;
import model.Empleado;
import model.FormaPago;
import model.OrdenVentaEstado;
import utils.AppNavigator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class Crear_ordenventa_controller {

    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private ComboBox<FormaPago> cmbFormaPago;
    @FXML private ComboBox<Empleado> cmbEmpleado;
    @FXML private DatePicker dpFechaEntrega;
    @FXML private TextField txtEstado;
    @FXML private Button btnCancelar;
    @FXML private Button btnCrearOrden;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<Cliente> Clientes = FXCollections.observableArrayList();
    ObservableList<FormaPago> FormasPago = FXCollections.observableArrayList();
    ObservableList<Empleado> Empleados = FXCollections.observableArrayList();

    public ObservableList cargarClientes() {
        String sql = "SELECT id_cliente, razon_social FROM CLIENTE ORDER BY razon_social";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Clientes.add(new Cliente(rs.getInt(1), rs.getString("razon_social")));
            }
        } catch (Exception e) {
            System.out.println("Error cargando clientes: " + e.getMessage());
        }
        return Clientes;
    }

    public ObservableList cargarFormasPago() {
        String sql = "SELECT id_forma_pago, nombre FROM FORMA_PAGO ORDER BY nombre";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FormasPago.add(new FormaPago(rs.getInt(1), rs.getString("nombre")));
            }
        } catch (Exception e) {
            System.out.println("Error cargando formas de pago: " + e.getMessage());
        }
        return FormasPago;
    }

    private void cargarEmpleados() {
        String sql = "SELECT e.id_empleado, e.nombre, e.apellido1 FROM EMPLEADO e " +
                   "INNER JOIN PUESTO p ON e.id_puesto = p.id_puesto " +
                   "WHERE LOWER(p.nombre) LIKE '%repartidor%' ORDER BY e.nombre";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Empleados.add(new Empleado(rs.getInt("id_empleado"), rs.getString("nombre"), rs.getString("apellido1")));
            }
        } catch (Exception e) {
            System.out.println("Error cargando empleados: " + e.getMessage());
        }
        cmbEmpleado.setItems(Empleados);
    }

    @FXML
    public void initialize() {
        cmbCliente.setItems(cargarClientes());
        cmbFormaPago.setItems(cargarFormasPago());
        cargarEmpleados();
        txtEstado.setText("PENDIENTE");
    }

    @FXML
    public void fnCrearOrden(ActionEvent event) {
        if (!validarCampos()) return;

        int idCliente = cmbCliente.getValue().getIdCliente();
        int idFormaPago = cmbFormaPago.getValue().getIdFormaPago();
        Date fechaEntrega = dpFechaEntrega.getValue() != null ? Date.valueOf(dpFechaEntrega.getValue()) : null;
        Integer idEmpleado = cmbEmpleado.getValue() != null ? cmbEmpleado.getValue().getIdEmpleado() : null;

        int idOrden = insertarOrden(idCliente, idFormaPago, fechaEntrega, idEmpleado);

        if (idOrden > 0) {

            OrdenVentaEstado.idOrdenVenta = idOrden;
            OrdenVentaEstado.idCliente = idCliente;
            OrdenVentaEstado.idEmpleado = idEmpleado;
            OrdenVentaEstado.nombreCliente = cmbCliente.getValue().getNombre();
            OrdenVentaEstado.idFormaPago = idFormaPago;
            OrdenVentaEstado.nombreFormaPago = cmbFormaPago.getValue().getNombre();

            Detalle_orden_venta_controller.setIdOrden(idOrden);
            appNavigator.load("/view/Detalle_Venta.fxml");
        } else {
            System.out.printf("Error al crear la orden");
        }
    }
    private int insertarOrden(int idCliente, int idFormaPago, Date fechaEntrega, Integer idEmpleado) {
        String sql = "INSERT INTO ORDEN_VENTA (id_cliente, id_empleado, estado, fecha_orden, id_forma_pago, fecha_entrega) VALUES (?, ?, 'PENDIENTE', GETDATE(), ?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idCliente);
            if (idEmpleado != null) {
                ps.setInt(2, idEmpleado);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, idFormaPago);
            if (fechaEntrega != null) {
                ps.setDate(4, fechaEntrega);
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error insertando orden: " + e.getMessage());
        }
        return 0;
    }

    private boolean validarCampos() {
        if (cmbCliente.getValue() == null) {
            System.out.println("Debe seleccionar un cliente");
            return false;
        }
        if (cmbFormaPago.getValue() == null) {
            System.out.println("Debe seleccionar una forma de pago");
            return false;
        }
        return true;
    }

    @FXML
    public void fnCancelar(ActionEvent event) {
        appNavigator.volverMenu();
    }
}