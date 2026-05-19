package controllers;

import Data_base.CONEXION;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.CategoriaProveedor;
import model.Proveedor;
import utils.AppNavigator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Gestion_proveedores_controller {

    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, String> colId;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colCorreo;
    @FXML private TableColumn<Proveedor, String> colCategoria;
    @FXML private TableColumn<Proveedor, Void> colAccion;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<CategoriaProveedor> cmbCategoria;
    @FXML private Label lblTotal;

    CONEXION conexion = new CONEXION();
    AppNavigator appNavigator = new AppNavigator();
    ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarCategorias();
        cargarProveedores();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getIdProveedor())));
        colNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre()));
        colTelefono.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNumeroTelefono() != null ? cd.getValue().getNumeroTelefono() : ""));
        colCorreo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCorreoElectronico() != null ? cd.getValue().getCorreoElectronico() : ""));

        colCategoria.setCellValueFactory(cd -> {
            CategoriaProveedor cat = cd.getValue().getCategoriaProveedor();
            return new SimpleStringProperty(cat != null ? cat.getNombre() : "");
        });

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            {   btnEditar.setStyle("-fx-background-color: #cdb08e; -fx-text-fill: #100e0a; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 6 12;");
                btnEliminar.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 6 12;");
                btnEditar.setOnAction(e -> fnEditar(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> fnEliminar(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : new javafx.scene.layout.HBox(8, btnEditar, btnEliminar));
            }
        });

        tablaProveedores.setItems(listaProveedores);
    }

    private void cargarCategorias() {
        ObservableList<CategoriaProveedor> cats = FXCollections.observableArrayList();
        cats.add(new CategoriaProveedor(0, "Todas las categorías"));
        String sql = "SELECT id_categoria_proveedor, nombre FROM CATEGORIA_PROVEEDOR ORDER BY nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cats.add(new CategoriaProveedor(rs.getInt(1), rs.getString("nombre")));
        } catch (SQLException e) { mostrarError("Error cargando categorías: " + e.getMessage()); }
        cmbCategoria.setItems(cats);
        cmbCategoria.getSelectionModel().selectFirst();
    }

    private void cargarProveedores() {
        listaProveedores.clear();
        String sql = "SELECT p.id_proveedor, p.nombre, p.numero_telefono, p.correo_electronico, " +
                   "cp.id_categoria_proveedor, cp.nombre as cat_nombre " +
                   "FROM PROVEEDOR p " +
                   "LEFT JOIN CATEGORIA_PROVEEDOR cp ON p.id_categoria_proveedor = cp.id_categoria_proveedor " +
                   "ORDER BY p.nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setIdProveedor(rs.getInt("id_proveedor"));
                p.setNombre(rs.getString("nombre"));
                p.setNumeroTelefono(rs.getString("numero_telefono"));
                p.setCorreoElectronico(rs.getString("correo_electronico"));
                p.setCategoriaProveedor(new CategoriaProveedor(rs.getInt("id_categoria_proveedor"), rs.getString("cat_nombre")));
                listaProveedores.add(p);
            }
        } catch (SQLException e) { mostrarError("Error cargando proveedores: " + e.getMessage()); }
        actualizarTotal();
    }

    @FXML
    public void fnBuscar(ActionEvent event) {
        listaProveedores.clear();
        String texto = txtBuscar.getText().trim();
        CategoriaProveedor cat = cmbCategoria.getValue();

        StringBuilder sql = new StringBuilder(
            "SELECT p.id_proveedor, p.nombre, p.numero_telefono, p.correo_electronico, " +
            "cp.id_categoria_proveedor, cp.nombre as cat_nombre " +
            "FROM PROVEEDOR p LEFT JOIN CATEGORIA_PROVEEDOR cp ON p.id_categoria_proveedor = cp.id_categoria_proveedor WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (!texto.isEmpty()) {
            sql.append(" AND (LOWER(p.nombre) LIKE ? OR LOWER(p.numero_telefono) LIKE ?) ");
            String like = "%" + texto.toLowerCase() + "%";
            params.add(like); params.add(like);
        }
        if (cat != null && cat.getIdCategoriaProveedor() > 0) {
            sql.append(" AND p.id_categoria_proveedor = ? ");
            params.add(cat.getIdCategoriaProveedor());
        }
        sql.append(" ORDER BY p.nombre");

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setIdProveedor(rs.getInt("id_proveedor"));
                p.setNombre(rs.getString("nombre"));
                p.setNumeroTelefono(rs.getString("numero_telefono"));
                p.setCorreoElectronico(rs.getString("correo_electronico"));
                p.setCategoriaProveedor(new CategoriaProveedor(rs.getInt("id_categoria_proveedor"), rs.getString("cat_nombre")));
                listaProveedores.add(p);
            }
        } catch (SQLException e) { mostrarError("Error al buscar: " + e.getMessage()); }
        actualizarTotal();
    }

    private void fnEditar(Proveedor proveedor) {
        Dialog<Proveedor> d = new Dialog<>();
        d.setTitle("Editar Proveedor");
        d.setHeaderText("Editando: " + proveedor.getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        TextField txtNombre = new TextField(proveedor.getNombre());
        TextField txtTelefono = new TextField(proveedor.getNumeroTelefono());
        TextField txtCorreo = new TextField(proveedor.getCorreoElectronico());

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Nombre:"), 0, 0); grid.add(txtNombre, 1, 0);
        grid.add(new Label("Teléfono:"), 0, 1); grid.add(txtTelefono, 1, 1);
        grid.add(new Label("Correo:"), 0, 2); grid.add(txtCorreo, 1, 2);
        d.getDialogPane().setContent(grid);

        d.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                proveedor.setNombre(txtNombre.getText().trim());
                proveedor.setNumeroTelefono(txtTelefono.getText().trim());
                proveedor.setCorreoElectronico(txtCorreo.getText().trim());
                return proveedor;
            }
            return null;
        });

        d.showAndWait().ifPresent(p -> {
            String sqlUpd = "UPDATE PROVEEDOR SET nombre=?, numero_telefono=?, correo_electronico=? WHERE id_proveedor=?";
            try (Connection c = conexion.establecerconexio();
                 PreparedStatement ps = c.prepareStatement(sqlUpd)) {
                ps.setString(1, p.getNombre());
                ps.setString(2, p.getNumeroTelefono());
                ps.setString(3, p.getCorreoElectronico());
                ps.setInt(4, p.getIdProveedor());
                ps.executeUpdate();
                mostrarInfo("Proveedor actualizado.");
                cargarProveedores();
            } catch (SQLException e) { mostrarError("Error al actualizar: " + e.getMessage()); }
        });
    }

    private void fnEliminar(Proveedor proveedor) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar proveedor \"" + proveedor.getNombre() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        String sql = "DELETE FROM PROVEEDOR WHERE id_proveedor = ?";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, proveedor.getIdProveedor());
            ps.executeUpdate();
            mostrarInfo("Proveedor eliminado.");
            cargarProveedores();
        } catch (SQLException e) { mostrarError("Error al eliminar: " + e.getMessage()); }
    }

    private void actualizarTotal() {
        lblTotal.setText("Total: " + listaProveedores.size() + " proveedor(es)");
    }

    public void fnVolver() { appNavigator.volverMenu(); }

    private void mostrarInfo(String m) { Alert a = new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
    private void mostrarError(String m) { Alert a = new Alert(Alert.AlertType.ERROR, m, ButtonType.OK); a.setHeaderText(null); a.showAndWait(); }
}
