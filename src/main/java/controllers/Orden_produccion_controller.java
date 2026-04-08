package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import model.Empleado;
import model.Producto;
import utils.AppNavigator;

import java.math.BigDecimal;
import java.sql.*;

public class Orden_produccion_controller {

    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private ComboBox<Empleado> cmbEmpleado;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<String> cmbEstado;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();

    ObservableList<Producto> Productos = FXCollections.observableArrayList();
    ObservableList<Empleado> Empleados = FXCollections.observableArrayList();
    ObservableList<String> Estados = FXCollections.observableArrayList("Pendiente", "En Proceso", "Completada", "Cancelada");



    @FXML
    public void initialize() {
        cmbProducto.setItems(cargarProductos());
        cmbEmpleado.setItems(cargarEmpleados());
        cmbEstado.setItems(Estados);
        cmbEstado.setValue("Pendiente"); // Valor por defecto
    }



    public ObservableList cargarProductos() {
        String sql = "SELECT id_producto, nombre FROM PRODUCTO ORDER BY nombre";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Productos.add(new Producto(rs.getInt(1), rs.getString("nombre"), null, null, null));
            }
        } catch (Exception e) {
            System.out.println("Error cargando productos: " + e.getMessage());
        }
        return Productos;
    }

    public ObservableList cargarEmpleados() {
        String sql = "SELECT id_empleado, nombre, apellido1 FROM EMPLEADO ORDER BY nombre";
        try (Connection connection = conexion.establecerconexio();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Empleados.add(new Empleado(rs.getInt(1), rs.getString("nombre"), rs.getString("apellido1")));
            }
        } catch (Exception e) {
            System.out.println("Error cargando empleados: " + e.getMessage());
        }
        return Empleados;
    }



    @FXML
    public void fnGuardarOrden(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        Producto producto = cmbProducto.getValue();
        Empleado empleado = cmbEmpleado.getValue();
        BigDecimal cantidad = new BigDecimal(txtCantidad.getText().trim());
        String estado = cmbEstado.getValue();

        int idOrden = insertarOrden(producto.getIdProducto(), empleado.getIdEmpleado(), cantidad, estado);

        if (idOrden > 0) {
            System.out.println("Orden de producción creada con ID: " + idOrden);
            fnLimpiar(event);
        } else {
            System.out.println("Error al crear la orden");
        }
    }

    private int insertarOrden(int idProducto, int idEmpleado, BigDecimal cantidad, String estado) {
        String sql = "INSERT INTO ORDEN_PRODUCCION (id_producto, id_empleado, cantidad_planificada, estado) VALUES (?, ?, ?, ?)";

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
            System.out.println("❌ Error insertando orden: " + e.getMessage());
        }
        return 0;
    }

    // ============================================
    // VALIDACIONES
    // ============================================

    private boolean validarCampos() {
        if (cmbProducto.getValue() == null) {
            System.out.println("❌ Debe seleccionar un producto");
            return false;
        }

        if (cmbEmpleado.getValue() == null) {
            System.out.println("❌ Debe seleccionar un empleado responsable");
            return false;
        }

        if (txtCantidad.getText() == null || txtCantidad.getText().trim().isEmpty()) {
            System.out.println("❌ La cantidad es obligatoria");
            return false;
        }

        try {
            BigDecimal cantidad = new BigDecimal(txtCantidad.getText().trim());
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("❌ La cantidad debe ser mayor a cero");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ La cantidad debe ser un número válido");
            return false;
        }

        if (cmbEstado.getValue() == null) {
            System.out.println("❌ Debe seleccionar un estado");
            return false;
        }

        return true;
    }

    // ============================================
    // BOTONES
    // ============================================

    @FXML
    public void fnLimpiar(ActionEvent event) {
        cmbProducto.setValue(null);
        cmbEmpleado.setValue(null);
        txtCantidad.clear();
        cmbEstado.setValue("Pendiente");
        System.out.println("🧹 Formulario limpiado");
    }

    @FXML
    public void fnVolverMenu(ActionEvent event) {
        appNavigator.volverMenu();
    }
}
