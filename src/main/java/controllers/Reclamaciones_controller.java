package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.AppNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.AlertHelper.*;

public class Reclamaciones_controller {

    @FXML private TextField txtIdOrden;
    @FXML private TextField txtDatosOrden;
    @FXML private Button btnBuscarOrden;

    @FXML private TextField txtIdCliente;
    @FXML private TextField txtNombreCliente;
    @FXML private Button btnBuscarCliente;

    @FXML private TextField txtIdEmpleado;
    @FXML private TextField txtNombreEmpleado;
    @FXML private Button btnBuscarEmpleado;

    @FXML private ComboBox<String> cmbTipo;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbPrioridad;
    @FXML private TextArea txtMotivo;

    private CONEXION conexion = new CONEXION();
    private AppNavigator appNavigator = new AppNavigator();

    private int idOrdenSeleccionada;
    private int idClienteSeleccionado;
    private int idEmpleadoSeleccionado;

    @FXML
    public void initialize() {
        cmbTipo.setItems(FXCollections.observableArrayList("Devolución", "Cambio", "Queja", "Garantía"));
        cmbEstado.setItems(FXCollections.observableArrayList("ABIERTA", "EN_PROCESO", "RESUELTA", "CERRADA"));
        cmbPrioridad.setItems(FXCollections.observableArrayList("BAJA", "MEDIA", "ALTA"));

        cmbEstado.setValue("ABIERTA");
        cmbPrioridad.setValue("MEDIA");
    }

    @FXML
    public void fnBuscarOrden(ActionEvent event) {
        String idTexto = txtIdOrden.getText().trim();
        if (idTexto.isEmpty()) {
            mostrarAdvertencia("Ingrese el ID de la orden.");
            return;
        }

        int idOrden;
        try {
            idOrden = Integer.parseInt(idTexto);
        } catch (NumberFormatException e) {
            mostrarAdvertencia("El ID debe ser un número válido.");
            return;
        }

        String sql = "SELECT ov.id_orden_venta, c.razon_social, ov.monto_total, ov.estado, ov.id_cliente " +
                   "FROM ORDEN_VENTA ov " +
                   "INNER JOIN CLIENTE c ON ov.id_cliente = c.id_cliente " +
                   "WHERE ov.id_orden_venta = ?";

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                idOrdenSeleccionada = rs.getInt("id_orden_venta");
                idClienteSeleccionado = rs.getInt("id_cliente");
                String cliente = rs.getString("razon_social");
                double total = rs.getDouble("monto_total");
                String estado = rs.getString("estado");
                txtDatosOrden.setText("Cliente: " + cliente + " | Total: RD$ " + String.format("%.2f", total) + " | Estado: " + estado);
                txtIdCliente.setText(String.valueOf(idClienteSeleccionado));
                txtNombreCliente.setText(cliente);
            } else {
                mostrarAdvertencia("No se encontró ninguna orden con ese ID.");
                limpiarOrden();
            }
        } catch (SQLException e) {
            mostrarError("Error al buscar orden: " + e.getMessage());
        }
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

        String sql = "SELECT id_cliente, razon_social FROM CLIENTE WHERE id_cliente = ?";

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                idClienteSeleccionado = rs.getInt("id_cliente");
                txtNombreCliente.setText(rs.getString("razon_social"));
            } else {
                mostrarAdvertencia("No se encontró ningún cliente con ese ID.");
                txtNombreCliente.clear();
                idClienteSeleccionado = 0;
            }
        } catch (SQLException e) {
            mostrarError("Error al buscar cliente: " + e.getMessage());
        }
    }

    @FXML
    public void fnBuscarEmpleado(ActionEvent event) {
        String idTexto = txtIdEmpleado.getText().trim();
        if (idTexto.isEmpty()) {
            mostrarAdvertencia("Ingrese el ID del empleado.");
            return;
        }

        int idEmpleado;
        try {
            idEmpleado = Integer.parseInt(idTexto);
        } catch (NumberFormatException e) {
            mostrarAdvertencia("El ID debe ser un número válido.");
            return;
        }

        String sql = "SELECT id_empleado, nombre + ' ' + apellido1 as nombre_completo FROM EMPLEADO WHERE id_empleado = ?";

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                idEmpleadoSeleccionado = rs.getInt("id_empleado");
                txtNombreEmpleado.setText(rs.getString("nombre_completo"));
            } else {
                mostrarAdvertencia("No se encontró ningún empleado con ese ID.");
                txtNombreEmpleado.clear();
                idEmpleadoSeleccionado = 0;
            }
        } catch (SQLException e) {
            mostrarError("Error al buscar empleado: " + e.getMessage());
        }
    }

    @FXML
    public void fnGuardarReclamacion(ActionEvent event) {
        if (!validarCampos()) return;

        String tipo = cmbTipo.getValue();
        String estado = cmbEstado.getValue();
        String prioridad = cmbPrioridad.getValue();
        String motivo = txtMotivo.getText().trim();

        int idReclamacion = insertarReclamacion(motivo, estado, tipo, prioridad, idOrdenSeleccionada, idClienteSeleccionado, idEmpleadoSeleccionado > 0 ? idEmpleadoSeleccionado : null);

        if (idReclamacion > 0) {
            mostrarInfo("Reclamación guardada exitosamente.\nID: " + idReclamacion);
            fnLimpiar();
        } else {
            mostrarError("Error al guardar la reclamación.");
        }
    }

    private int insertarReclamacion(String motivo, String estado, String tipo,
                                    String prioridad, int idOrden, int idCliente,
                                    Integer idEmpleado) {
        String sql = "INSERT INTO RECLAMACION_VENTA (motivo, estado_actual, tipo_reclamacion, prioridad, id_orden_venta, id_empresa_cliente, id_empleado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, motivo);
            ps.setString(2, estado);
            ps.setString(3, tipo);
            ps.setString(4, prioridad);
            ps.setInt(5, idOrden);
            ps.setInt(6, idCliente);
            if (idEmpleado != null) {
                ps.setInt(7, idEmpleado);
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            mostrarError("Error al guardar reclamación: " + e.getMessage());
        }
        return 0;
    }

    private boolean validarCampos() {
        if (idOrdenSeleccionada == 0) {
            mostrarAdvertencia("Debe seleccionar una orden de venta válida.");
            return false;
        }
        if (idClienteSeleccionado == 0) {
            mostrarAdvertencia("Debe seleccionar un cliente válido.");
            return false;
        }
        if (cmbTipo.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar un tipo de reclamación.");
            return false;
        }
        if (txtMotivo.getText() == null || txtMotivo.getText().trim().isEmpty()) {
            mostrarAdvertencia("El motivo es obligatorio.");
            return false;
        }
        return true;
    }

    @FXML
    public void fnLimpiar() {
        limpiarOrden();
        limpiarCliente();
        limpiarEmpleado();
        cmbTipo.setValue(null);
        cmbEstado.setValue("ABIERTA");
        cmbPrioridad.setValue("MEDIA");
        txtMotivo.clear();
    }

    private void limpiarOrden() {
        txtIdOrden.clear();
        txtDatosOrden.clear();
        idOrdenSeleccionada = 0;
    }

    private void limpiarCliente() {
        txtIdCliente.clear();
        txtNombreCliente.clear();
        idClienteSeleccionado = 0;
    }

    private void limpiarEmpleado() {
        txtIdEmpleado.clear();
        txtNombreEmpleado.clear();
        idEmpleadoSeleccionado = 0;
    }

    @FXML
    public void fnVolverMenu(ActionEvent event) {
        appNavigator.volverMenu();
    }



}