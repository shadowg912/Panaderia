package controllers;

import Data_base.CONEXION;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import utils.AppNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Reclamaciones_controller {

    @FXML private TextField txtIdFactura;
    @FXML private TextField txtIdCliente;
    @FXML private TextField txtIdEmpleado;
    @FXML private TextField txtTipoReclamacion;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbPrioridad;
    @FXML private TextArea txtMotivo;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();

    @FXML
    public void initialize() {
        cmbEstado.setValue("ABIERTA");
        cmbPrioridad.setValue("MEDIA");
    }

    @FXML
    public void fnGuardarReclamacion(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        int idFactura = Integer.parseInt(txtIdFactura.getText().trim());
        int idCliente = Integer.parseInt(txtIdCliente.getText().trim());
        String tipo = txtTipoReclamacion.getText().trim();
        String estado = cmbEstado.getValue();
        String prioridad = cmbPrioridad.getValue();
        String motivo = txtMotivo.getText().trim();

        Integer idEmpleado = null;
        if (txtIdEmpleado.getText() != null && !txtIdEmpleado.getText().trim().isEmpty()) {
            idEmpleado = Integer.parseInt(txtIdEmpleado.getText().trim());
        }

        int idReclamacion = insertarReclamacion(motivo, estado, tipo, prioridad, idFactura, idCliente, idEmpleado);

        if (idReclamacion > 0) {
            System.out.println("Reclamacion guardada exitosamente con ID: " + idReclamacion);
            fnLimpiar();
        } else {
            System.out.println("Error al guardar reclamacion");
        }
    }

    private int insertarReclamacion(String motivo, String estado, String tipo,
                                    String prioridad, int idFactura, int idCliente,
                                    Integer idEmpleado) {
        String sql = "INSERT INTO RECLAMACION_VENTA (motivo, estado_actual, tipo_reclamacion, prioridad, id_factura_venta, id_empresa_cliente, id_empleado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, motivo);
            ps.setString(2, estado);
            ps.setString(3, tipo);
            ps.setString(4, prioridad);
            ps.setInt(5, idFactura);
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
            System.out.println("Error insertando reclamacion: " + e.getMessage());
        }
        return 0;
    }

    private boolean validarCampos() {
        if (txtIdFactura.getText() == null || txtIdFactura.getText().trim().isEmpty()) {
            System.out.println("El ID de factura es obligatorio");
            return false;
        }
        try {
            Integer.parseInt(txtIdFactura.getText().trim());
        } catch (NumberFormatException e) {
            System.out.println("El ID de factura debe ser un numero valido");
            return false;
        }

        if (txtIdCliente.getText() == null || txtIdCliente.getText().trim().isEmpty()) {
            System.out.println("El ID de cliente es obligatorio");
            return false;
        }
        try {
            Integer.parseInt(txtIdCliente.getText().trim());
        } catch (NumberFormatException e) {
            System.out.println("El ID de cliente debe ser un numero valido");
            return false;
        }

        if (txtTipoReclamacion.getText() == null || txtTipoReclamacion.getText().trim().isEmpty()) {
            System.out.println("El tipo de reclamacion es obligatorio");
            return false;
        }

        if (txtMotivo.getText() == null || txtMotivo.getText().trim().isEmpty()) {
            System.out.println("El motivo es obligatorio");
            return false;
        }

        if (cmbEstado.getValue() == null) {
            System.out.println("Debe seleccionar un estado");
            return false;
        }

        if (cmbPrioridad.getValue() == null) {
            System.out.println("Debe seleccionar una prioridad");
            return false;
        }

        if (txtIdEmpleado.getText() != null && !txtIdEmpleado.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(txtIdEmpleado.getText().trim());
            } catch (NumberFormatException e) {
                System.out.println("El ID de empleado debe ser un numero valido");
                return false;
            }
        }

        return true;
    }

    @FXML
    public void fnLimpiar() {
        txtIdFactura.clear();
        txtIdCliente.clear();
        txtIdEmpleado.clear();
        txtTipoReclamacion.clear();
        cmbEstado.setValue("ABIERTA");
        cmbPrioridad.setValue("MEDIA");
        txtMotivo.clear();
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