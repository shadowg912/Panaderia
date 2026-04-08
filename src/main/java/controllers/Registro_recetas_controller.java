package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Ingrediente;
import utils.AppNavigator;

import java.sql.*;

public class Registro_recetas_controller {

    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtRendimiento;
    @FXML private TextField txtTiempo;
    @FXML private TextArea txtInstrucciones;
    @FXML private TableView<Ingrediente> tblIngredientes;
    @FXML private TableColumn<Ingrediente, String> colIngrediente;
    @FXML private TableColumn<Ingrediente, Double> colCantidad;
    @FXML private TableColumn<Ingrediente, String> colUnidad;
    @FXML private TableColumn<Ingrediente, Double> colCosto;
    @FXML private Button btnAgregarIngrediente;
    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<Ingrediente> listaIngredientes = FXCollections.observableArrayList();
    ObservableList<String> categorias = FXCollections.observableArrayList("1", "2", "3", "4");

    @FXML
    public void initialize() {
        colIngrediente.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        tblIngredientes.setItems(listaIngredientes);
        cmbCategoria.setItems(categorias);
    }

    @FXML
    public void fnAgregarIngrediente(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Ingrediente");
        dialog.setHeaderText("Ingrese los datos del ingrediente");
        dialog.setContentText("Formato: nombre,cantidad,unidad (Ej: Harina,500,g)");
        dialog.showAndWait().ifPresent(input -> {
            try {
                String[] partes = input.split(",");
                if (partes.length >= 3) {
                    listaIngredientes.add(new Ingrediente(0, partes[0].trim(), Double.parseDouble(partes[1].trim()), partes[2].trim()));
                    tblIngredientes.refresh();
                }
            } catch (Exception e) {
                System.out.println("Error al procesar ingrediente: " + e.getMessage());
            }
        });
    }

    @FXML
    public void fnGuardarReceta(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        int idCategoria = 1;
        double precioUnitario = 0.00;
        int idUnidad = 1;

        if (listaIngredientes.isEmpty()) {
            System.out.println("Debe agregar al menos un ingrediente");
            return;
        }

        Connection conn = null;
        try {
            conn = conexion.establecerconexio();
            conn.setAutoCommit(false);

            String sqlProducto = "INSERT INTO PRODUCTO (nombre, id_categoria_producto, precio_unitario, id_unidad) VALUES (?, ?, ?, ?)";
            int idProducto;
            try (PreparedStatement pst = conn.prepareStatement(sqlProducto, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, nombre);
                pst.setInt(2, idCategoria);
                pst.setDouble(3, precioUnitario);
                pst.setInt(4, idUnidad);
                pst.executeUpdate();
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    idProducto = rs.getInt(1);
                } else {
                    throw new SQLException("No se obtuvo el ID del producto");
                }
            }

            String sqlRecetaIng = "INSERT INTO RECETA_PRODUCTO (id_producto, id_ingrediente, cantidad_ingrediente) VALUES (?, ?, ?)";
            try (PreparedStatement pstIng = conn.prepareStatement(sqlRecetaIng)) {
                for (Ingrediente ing : listaIngredientes) {
                    int idIng = buscarOCrearIngrediente(conn, ing.getNombre(), ing.getUnidad());
                    if (idIng == 0) {
                        throw new SQLException("No se pudo procesar el ingrediente: " + ing.getNombre());
                    }
                    pstIng.setInt(1, idProducto);
                    pstIng.setInt(2, idIng);
                    pstIng.setDouble(3, ing.getCantidad());
                    pstIng.addBatch();
                }
                pstIng.executeBatch();
            }

            conn.commit();
            System.out.println("Receta guardada exitosamente con ID: " + idProducto);
            fnLimpiarFormulario(event);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.out.println("Error rollback: " + ex.getMessage()); }
            }
            System.out.println("Error al guardar receta: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @FXML
    public void fnLimpiarFormulario(ActionEvent event) {
        txtNombre.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        txtRendimiento.clear();
        txtTiempo.clear();
        txtInstrucciones.clear();
        listaIngredientes.clear();
        tblIngredientes.refresh();
    }

    @FXML
    public void fnCancelar(ActionEvent event) {
        fnLimpiarFormulario(event);
    }

    private int buscarOCrearIngrediente(Connection conn, String nombre, String unidad) {
        String sqlBuscar = "SELECT id_ingrediente FROM INGREDIENTE WHERE nombre = ? AND unidad_medida = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
            ps.setString(1, nombre);
            ps.setString(2, unidad != null ? unidad : "und");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_ingrediente");
            }
        } catch (SQLException e) {
            System.out.println("Error buscando ingrediente: " + e.getMessage());
        }

        String sqlInsertar = "INSERT INTO INGREDIENTE (nombre, unidad_medida, id_categoria_ingrediente) VALUES (?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, unidad != null ? unidad : "und");
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error creando ingrediente: " + e.getMessage());
        }
        return 0;
    }
}