package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.Ingrediente;
import model.Producto;
import model.RecetaProducto;
import utils.AppNavigator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Registro_recetas_controller {
    AppNavigator appNavigator = new AppNavigator();
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private ComboBox<Ingrediente> cmbIngrediente;
    @FXML private TextField txtCantidadIngrediente;
    @FXML private TableView<Ingrediente> tblRecetaDetalle;
    @FXML private TableColumn<Ingrediente, Integer> colIdIngrediente;
    @FXML private TableColumn<Ingrediente, String>  colNombreIngrediente;
    @FXML private TableColumn<Ingrediente, Double>  colCantidad;
    @FXML private TableColumn<Ingrediente, String>  colUnidad;
    @FXML private TableColumn<Ingrediente, Void>    colAccion;
    @FXML private Button btnAgregarALista;
    @FXML private Button btnGuardarReceta;

    private final CONEXION conexion = new CONEXION();
    private final ObservableList<Producto>    listaProductos    = FXCollections.observableArrayList();
    private final ObservableList<Ingrediente> listaIngredientes = FXCollections.observableArrayList();

    // Lista temporal de ingredientes agregados a la receta actual (en memoria)
    private final ObservableList<Ingrediente> detalleReceta = FXCollections.observableArrayList();

    // ─────────────────────────────────────────────────────────────────────────
    //  INICIALIZACIÓN
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarProductos();
        cargarIngredientes();
        tblRecetaDetalle.setItems(detalleReceta);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CONFIGURACIÓN DE TABLA
    // ─────────────────────────────────────────────────────────────────────────

    private void configurarColumnas() {
        colIdIngrediente.setCellValueFactory(new PropertyValueFactory<>("idIngrediente"));
        colNombreIngrediente.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        // Columna con botón "Eliminar"
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");

            {
                btnEliminar.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;"
                );
                btnEliminar.setOnAction(e -> {
                    Ingrediente item = getTableView().getItems().get(getIndex());
                    detalleReceta.remove(item);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CARGA DE DATOS DESDE BD
    // ─────────────────────────────────────────────────────────────────────────

    private void cargarProductos() {
        listaProductos.clear();
        String sql = "SELECT id_producto, nombre FROM PRODUCTO ORDER BY nombre";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaProductos.add(new Producto(rs.getInt(1), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando productos: " + e.getMessage());
        }

        cmbProducto.setItems(listaProductos);

        // Muestra el nombre del producto en el ComboBox
        cmbProducto.setConverter(new StringConverter<>() {
            @Override public String toString(Producto p)   { return p == null ? "" : p.getNombre(); }
            @Override public Producto fromString(String s) { return null; }
        });
    }

    private void cargarIngredientes() {
        listaIngredientes.clear();
        // Traemos también la unidad de medida para mostrarla en la tabla
        String sql = "SELECT id_ingrediente, nombre, unidad_medida FROM INGREDIENTE ORDER BY nombre";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // cantidad = 0 por defecto; se asignará al agregarlo a la lista
                listaIngredientes.add(new Ingrediente(
                        rs.getInt("id_ingrediente"),
                        rs.getString("nombre"),
                        0,
                        rs.getString("unidad_medida")
                ));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando ingredientes: " + e.getMessage());
        }

        cmbIngrediente.setItems(listaIngredientes);

        cmbIngrediente.setConverter(new StringConverter<>() {
            @Override public String toString(Ingrediente i)   { return i == null ? "" : i.getNombre(); }
            @Override public Ingrediente fromString(String s) { return null; }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ACCIONES
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    public void fnAgregarALista(ActionEvent event) {
        Ingrediente seleccionado = cmbIngrediente.getValue();
        String cantidadTexto     = txtCantidadIngrediente.getText().trim();

        // Validaciones básicas
        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un ingrediente.");
            return;
        }
        if (cantidadTexto.isEmpty()) {
            mostrarAdvertencia("Ingrese la cantidad del ingrediente.");
            return;
        }

        double cantidad;
        try {
            cantidad = Double.parseDouble(cantidadTexto);
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarAdvertencia("La cantidad debe ser un número mayor a 0.");
            return;
        }

        // Evitar duplicados en la lista temporal
        boolean yaExiste = detalleReceta.stream()
                .anyMatch(i -> i.getIdIngrediente() == seleccionado.getIdIngrediente());
        if (yaExiste) {
            mostrarAdvertencia("Este ingrediente ya fue añadido. Elimínelo primero si desea cambiar la cantidad.");
            return;
        }

        // Crear un nuevo objeto Ingrediente con la cantidad asignada
        Ingrediente entrada = new Ingrediente(
                seleccionado.getIdIngrediente(),
                seleccionado.getNombre(),
                cantidad,
                seleccionado.getUnidad()
        );

        detalleReceta.add(entrada);

        // Limpiar controles del panel de agregar
        cmbIngrediente.setValue(null);
        txtCantidadIngrediente.clear();
    }

    @FXML
    public void fnGuardarReceta(ActionEvent event) {
        Producto producto = cmbProducto.getValue();

        if (producto == null) {
            mostrarAdvertencia("Seleccione el producto a elaborar.");
            return;
        }
        if (detalleReceta.isEmpty()) {
            mostrarAdvertencia("Agregue al menos un ingrediente antes de guardar.");
            return;
        }

        // Confirmación opcional
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Guardar la receta para \"" + producto.getNombre() + "\" con " +
                        detalleReceta.size() + " ingrediente(s)?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar guardado");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        guardarRecetaEnBD(producto);
    }

    private void guardarRecetaEnBD(Producto producto) {
        // Borramos primero las recetas existentes para ese producto (reemplazo completo)
        String sqlDelete = "DELETE FROM RECETA_PRODUCTO WHERE id_producto = ?";
        String sqlInsert = "INSERT INTO RECETA_PRODUCTO (id_producto, id_ingrediente, cantidad_ingrediente) VALUES (?, ?, ?)";

        try (Connection con = conexion.establecerconexio()) {
            con.setAutoCommit(false); // Transacción

            // 1. Eliminar receta previa del mismo producto
            try (PreparedStatement psDel = con.prepareStatement(sqlDelete)) {
                psDel.setInt(1, producto.getIdProducto());
                psDel.executeUpdate();
            }

            // 2. Insertar cada línea de la nueva receta
            try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                for (Ingrediente ing : detalleReceta) {
                    psIns.setInt(1, producto.getIdProducto());
                    psIns.setInt(2, ing.getIdIngrediente());
                    psIns.setBigDecimal(3, BigDecimal.valueOf(ing.getCantidad()));
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            con.commit();
            mostrarInfo("Receta guardada exitosamente para: " + producto.getNombre());
            fnLimpiar();

        } catch (SQLException e) {
            mostrarError("Error al guardar la receta: " + e.getMessage());
        }
    }

    @FXML
    public void fnLimpiar() {
        cmbProducto.setValue(null);
        cmbIngrediente.setValue(null);
        txtCantidadIngrediente.clear();
        detalleReceta.clear();
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
    @FXML
    public void fnVolverMenu(ActionEvent event) {

        appNavigator.volverMenu();
    }}