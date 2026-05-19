package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Empleado;
import model.Producto;
import utils.AppNavigator;

import java.math.BigDecimal;
import java.sql.*;
import static utils.AlertHelper.*;

public class Orden_produccion_controller {

    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtIdEmpleado;
    @FXML private TextField txtNombreEmpleado;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Button btnBuscarEmpleado;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();

    ObservableList<Producto> Productos = FXCollections.observableArrayList();
    ObservableList<String> Estados = FXCollections.observableArrayList("Pendiente", "En Proceso", "Completada", "Cancelada");
    private Empleado empleadoSeleccionado;

    @FXML
    public void initialize() {
        cmbProducto.setItems(cargarProductos());
        cmbEstado.setItems(Estados);
        cmbEstado.setValue("Pendiente");
    }

    public ObservableList<Producto> cargarProductos() {
        String sql = "SELECT id_producto, nombre FROM PRODUCTO WHERE tipo_producto = 'PRODUCTO_TERMINADO' ORDER BY nombre";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Productos.add(new Producto(rs.getInt(1), rs.getString("nombre"), null, null, null));
            }
        } catch (Exception e) {
            mostrarError("Error cargando productos: " + e.getMessage());
        }
        return Productos;
    }

    @FXML
    public void fnBuscarEmpleado(ActionEvent event) {
        String idTexto = txtIdEmpleado.getText().trim();

        if (idTexto.isEmpty()) {
            mostrarAdvertencia("Ingrese el ID del empleado a buscar.");
            return;
        }

        int idEmpleado;
        try {
            idEmpleado = Integer.parseInt(idTexto);
        } catch (NumberFormatException e) {
            mostrarAdvertencia("El ID debe ser un número válido.");
            return;
        }

        Empleado emp = buscarEmpleadoPorId(idEmpleado);
        if (emp != null) {
            empleadoSeleccionado = emp;
            txtNombreEmpleado.setText(emp.getNombreCompleto());
        } else {
            empleadoSeleccionado = null;
            txtNombreEmpleado.setText("");
            mostrarAdvertencia("No se encontró ningún empleado con ese ID.");
        }
    }

    private Empleado buscarEmpleadoPorId(int idEmpleado) {
        String sql = "SELECT id_empleado, nombre, apellido1 FROM EMPLEADO WHERE id_empleado = ?";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Empleado(
                        rs.getInt("id_empleado"),
                        rs.getString("nombre"),
                        rs.getString("apellido1"));
            }
        } catch (SQLException e) {
            mostrarError("Error buscando empleado: " + e.getMessage());
        }
        return null;
    }

    @FXML
    public void fnGuardarOrden(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        Producto producto = cmbProducto.getValue();
        BigDecimal cantidad = new BigDecimal(txtCantidad.getText().trim());
        String estado = cmbEstado.getValue();

        int idOrden = insertarOrden(producto.getIdProducto(), empleadoSeleccionado.getIdEmpleado(), cantidad, estado);

        if (idOrden > 0) {
            mostrarInfo("Orden de producción creada exitosamente.\nID: " + idOrden);
            fnLimpiar(event);
        } else {
            mostrarError("Error al crear la orden");
        }
    }

    private int insertarOrden(int idProducto, int idEmpleado, BigDecimal cantidad, String estado) {
        String sql = "INSERT INTO ORDEN_PRODUCCION (id_producto, id_empleado, cantidad_planificada, estado, fecha_registro, fecha_produccion) VALUES (?, ?, ?, ?, GETDATE(), GETDATE())";

        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idProducto);
            ps.setInt(2, idEmpleado);
            ps.setBigDecimal(3, cantidad);
            ps.setString(4, estado);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            mostrarError("Error insertando orden: " + e.getMessage());
        }
        return 0;
    }

    private boolean validarCampos() {
        if (cmbProducto.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar un producto.");
            return false;
        }

        if (empleadoSeleccionado == null) {
            mostrarAdvertencia("Debe seleccionar un empleado válido.");
            return false;
        }

        if (txtCantidad.getText() == null || txtCantidad.getText().trim().isEmpty()) {
            mostrarAdvertencia("La cantidad es obligatoria.");
            return false;
        }

        try {
            BigDecimal cantidad = new BigDecimal(txtCantidad.getText().trim());
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAdvertencia("La cantidad debe ser mayor a cero.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAdvertencia("La cantidad debe ser un número válido.");
            return false;
        }

        if (cmbEstado.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar un estado.");
            return false;
        }

        return true;
    }

    @FXML
    public void fnLimpiar(ActionEvent event) {
        cmbProducto.setValue(null);
        txtIdEmpleado.clear();
        txtNombreEmpleado.clear();
        txtCantidad.clear();
        cmbEstado.setValue("Pendiente");
        empleadoSeleccionado = null;
    }

    @FXML
    public void fnVolverMenu(ActionEvent event) {
        appNavigator.volverMenu();
    }



}