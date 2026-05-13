package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import model.Producto;
import model.RecetaDetalle;
import model.Unidad;
import utils.AppNavigator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Registro_recetas_controller {
    AppNavigator appNavigator = new AppNavigator();
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private ComboBox<Producto> cmbIngrediente;
    @FXML private ComboBox<Unidad> cmbUnidad;
    @FXML private TextField txtCantidadIngrediente;
    @FXML private TableView<RecetaDetalle> tblRecetaDetalle;
    @FXML private TableColumn<RecetaDetalle, Integer> colIdIngrediente;
    @FXML private TableColumn<RecetaDetalle, String> colNombreIngrediente;
    @FXML private TableColumn<RecetaDetalle, Double> colCantidad;
    @FXML private TableColumn<RecetaDetalle, String> colUnidad;
    @FXML private TableColumn<RecetaDetalle, Void> colAccion;
    @FXML private Button btnAgregarALista;
    @FXML private Button btnGuardarReceta;

    private final CONEXION conexion = new CONEXION();
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<Producto> listaIngredientes = FXCollections.observableArrayList();
    private final ObservableList<Unidad> listaUnidades = FXCollections.observableArrayList();
    private final ObservableList<RecetaDetalle> detalleReceta = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[]{
                    item.idProductoIngredienteProperty(),
                    item.nombreIngredienteProperty(),
                    item.cantidadProperty(),
                    item.unidadMedidaProperty()
            }
    );

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarProductos();
        cargarIngredientes();
        cargarUnidades();
        tblRecetaDetalle.setItems(detalleReceta);
    }

    private void configurarColumnas() {
        colIdIngrediente.setCellValueFactory(cellData -> cellData.getValue().idProductoIngredienteProperty().asObject());
        colNombreIngrediente.setCellValueFactory(cellData -> cellData.getValue().nombreIngredienteProperty());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        colUnidad.setCellValueFactory(cellData -> cellData.getValue().unidadMedidaProperty());

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");

            {
                btnEliminar.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;"
                );
                btnEliminar.setOnAction(e -> {
                    RecetaDetalle item = getTableView().getItems().get(getIndex());
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
        cmbProducto.setConverter(new StringConverter<>() {
            @Override public String toString(Producto p) { return p == null ? "" : p.getNombre(); }
            @Override public Producto fromString(String s) { return null; }
        });
    }

    private void cargarIngredientes() {
        listaIngredientes.clear();
        String sql = "SELECT id_producto, nombre FROM PRODUCTO ORDER BY nombre";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaIngredientes.add(new Producto(rs.getInt(1), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando ingredientes: " + e.getMessage());
        }
        cmbIngrediente.setItems(listaIngredientes);
        cmbIngrediente.setConverter(new StringConverter<>() {
            @Override public String toString(Producto p) { return p == null ? "" : p.getNombre(); }
            @Override public Producto fromString(String s) { return null; }
        });
    }

    private void cargarUnidades() {
        listaUnidades.clear();
        String sql = "SELECT id_unidad, nombre FROM UNIDAD ORDER BY nombre";
        try (Connection con = conexion.establecerconexio();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listaUnidades.add(new Unidad(rs.getInt("id_unidad"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando unidades: " + e.getMessage());
        }
        cmbUnidad.setItems(listaUnidades);
    }

    @FXML
    public void fnAgregarALista(ActionEvent event) {
        Producto seleccionado = cmbIngrediente.getValue();
        Unidad unidadSeleccionada = cmbUnidad.getValue();
        String cantidadTexto = txtCantidadIngrediente.getText().trim();

        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un ingrediente.");
            return;
        }
        if (cantidadTexto.isEmpty()) {
            mostrarAdvertencia("Ingrese la cantidad del ingrediente.");
            return;
        }
        if (unidadSeleccionada == null) {
            mostrarAdvertencia("Seleccione la unidad de medida.");
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

        boolean yaExiste = detalleReceta.stream()
                .anyMatch(i -> i.getIdProductoIngrediente() == seleccionado.getIdProducto());
        if (yaExiste) {
            mostrarAdvertencia("Este ingrediente ya fue añadido. Elimínelo primero si desea cambiar la cantidad.");
            return;
        }

        RecetaDetalle entrada = new RecetaDetalle(
                seleccionado.getIdProducto(),
                seleccionado.getNombre(),
                cantidad,
                unidadSeleccionada.getNombre()
        );

        detalleReceta.add(entrada);

        cmbIngrediente.setValue(null);
        cmbUnidad.setValue(null);
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
        String sqlDelete = "DELETE FROM RECETA_DETALLE WHERE id_producto_final = ?";
        String sqlInsert = "INSERT INTO RECETA_DETALLE (id_producto_final, id_producto_ingrediente, cantidad, unidad_medida) VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.establecerconexio()) {
            con.setAutoCommit(false);

            try (PreparedStatement psDel = con.prepareStatement(sqlDelete)) {
                psDel.setInt(1, producto.getIdProducto());
                psDel.executeUpdate();
            }

            try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                for (RecetaDetalle ing : detalleReceta) {
                    psIns.setInt(1, producto.getIdProducto());
                    psIns.setInt(2, ing.getIdProductoIngrediente());
                    psIns.setBigDecimal(3, BigDecimal.valueOf(ing.getCantidad()));
                    psIns.setString(4, ing.getUnidadMedida());
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
        cmbUnidad.setValue(null);
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
    }
}