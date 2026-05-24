package controllers;

import Data_base.CONEXION;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Empleado;
import model.OrdenProduccionEstado;
import utils.AppNavigator;

import java.sql.*;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import static utils.AlertHelper.*;

public class Orden_produccion_simple_controller {

    @FXML private TextField txtIdEmpleado;
    @FXML private TextField txtNombreEmpleado;
    @FXML private Spinner<Integer> spDia;
    @FXML private Spinner<Integer> spMes;
    @FXML private Label lblAnio;
    private CONEXION conexion = new CONEXION();
    private AppNavigator appNavigator = new AppNavigator();
    private Empleado empleadoSeleccionado;

    @FXML
    public void initialize() {
        spDia.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, LocalDate.now().getDayOfMonth()));
        spMes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, LocalDate.now().getMonthValue()));
        lblAnio.setText(String.valueOf(Year.now().getValue()));
    }

    @FXML
    public void fnBuscarEmpleado() {
        String idTexto = txtIdEmpleado.getText().trim();
        if (idTexto.isEmpty()) { mostrarAdvertencia("Ingrese el ID del empleado."); return; }
        int id;
        try { id = Integer.parseInt(idTexto); }
        catch (NumberFormatException e) { mostrarAdvertencia("ID inválido."); return; }
        String sql = "SELECT id_empleado, nombre, apellido1 FROM EMPLEADO WHERE id_empleado = ?";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                empleadoSeleccionado = new Empleado(rs.getInt("id_empleado"), rs.getString("nombre"), rs.getString("apellido1"));
                txtNombreEmpleado.setText(empleadoSeleccionado.getNombreCompleto());
            } else {
                empleadoSeleccionado = null;
                txtNombreEmpleado.setText("");
                mostrarAdvertencia("Empleado no encontrado.");
            }
        } catch (SQLException e) { mostrarError("Error: " + e.getMessage()); }
    }

    private LocalDate obtenerFecha() {
        try { return LocalDate.of(Year.now().getValue(), spMes.getValue(), spDia.getValue()); }
        catch (Exception e) { return null; }
    }

    @FXML
    public void fnContinuar() {
        if (empleadoSeleccionado == null) { mostrarAdvertencia("Debe seleccionar un empleado."); return; }
        LocalDate fecha = obtenerFecha();
        if (fecha == null) { mostrarAdvertencia("Fecha inválida."); return; }
        if (fecha.isBefore(LocalDate.now())) { mostrarAdvertencia("La fecha no puede ser anterior a hoy."); return; }

        int idOrden = insertarOrden(empleadoSeleccionado.getIdEmpleado(), fecha);
        if (idOrden <= 0) { mostrarError("Error al crear la orden."); return; }

        OrdenProduccionEstado.idOrdenProduccion = idOrden;
        OrdenProduccionEstado.idEmpleado = empleadoSeleccionado.getIdEmpleado();
        OrdenProduccionEstado.nombreEmpleado = empleadoSeleccionado.getNombreCompleto();
        OrdenProduccionEstado.fecha = fecha.format(DateTimeFormatter.ISO_LOCAL_DATE);
        OrdenProduccionEstado.detalles = null;

        appNavigator.load("/view/Seleccionar_productos_produccion.fxml");
    }

    private int insertarOrden(int idEmpleado, LocalDate fecha) {
        String sql = "INSERT INTO ORDEN_PRODUCCION (id_empleado, fecha_produccion, estado, fecha_registro) VALUES (?, ?, 'Pendiente', GETDATE())";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idEmpleado);
            ps.setDate(2, Date.valueOf(fecha));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { mostrarError("Error: " + e.getMessage()); }
        return 0;
    }

    @FXML
    public void fnVolverMenu() { appNavigator.volverMenu(); }
}
