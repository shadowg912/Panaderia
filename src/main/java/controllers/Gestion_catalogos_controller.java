package controllers;

import Data_base.CONEXION;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import utils.AppNavigator;

import java.net.URL;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static utils.AlertHelper.*;

public class Gestion_catalogos_controller implements Initializable {

    @FXML private ComboBox<String> cmbCatalogo;
    @FXML private TableView<CatalogoItem> tablaCatalogos;
    @FXML private TableColumn<CatalogoItem, Integer> colId;
    @FXML private TableColumn<CatalogoItem, String> colNombre;
    @FXML private TableColumn<CatalogoItem, String> colDescripcion;
    @FXML private TableColumn<CatalogoItem, String> colEstado;
    @FXML private TableColumn<CatalogoItem, Void> colAccion;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private VBox panelDescripcion;
    @FXML private Label lblTituloForm;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Label lblTotalRegistros;

    private CONEXION conexion = new CONEXION();
    private ObservableList<CatalogoItem> lista = FXCollections.observableArrayList();
    private int idEditando = -1;

    private static class CatalogoConf {
        String tabla, idCol, nombreCol, descCol;
        boolean hasDesc, hasEstado;
        CatalogoConf(String tabla, String idCol, String nombreCol, String descCol, boolean hasDesc, boolean hasEstado) {
            this.tabla = tabla; this.idCol = idCol; this.nombreCol = nombreCol;
            this.descCol = descCol; this.hasDesc = hasDesc; this.hasEstado = hasEstado;
        }
    }

    private static final Map<String, CatalogoConf> CATALOGOS = new LinkedHashMap<>();
    static {
        CATALOGOS.put("Categoría de Producto",    new CatalogoConf("CATEGORIA_PRODUCTO",    "id_categoria_producto",    "nombre", "descripcion", true, true));
        CATALOGOS.put("Categoría de Proveedor",   new CatalogoConf("CATEGORIA_PROVEEDOR",   "id_categoria_proveedor",   "nombre", "descripcion", true, true));
        CATALOGOS.put("Unidad de Medida",         new CatalogoConf("UNIDAD",                 "id_unidad",                "nombre", "descripcion", true, true));
        CATALOGOS.put("Forma de Pago",            new CatalogoConf("FORMA_PAGO",             "id_forma_pago",            "nombre", null,          false, true));
        CATALOGOS.put("País",                     new CatalogoConf("PAIS",                   "id_pais",                  "nombre", null,          false, true));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbCatalogo.setItems(FXCollections.observableArrayList(CATALOGOS.keySet()));
        colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre()));
        colDescripcion.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDescripcion()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "Activo" : "Inactivo"));
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 4 12; -fx-font-weight: bold;");
                btn.setOnAction(e -> {
                    CatalogoItem item = getTableView().getItems().get(getIndex());
                    toggleEstado(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); return;
                }
                boolean a = getTableView().getItems().get(getIndex()).isActivo();
                if (a) {
                    btn.setText("Desactivar");
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-border-color: #ef4444; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 12; -fx-font-weight: bold;");
                } else {
                    btn.setText("Activar");
                    btn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 4 12; -fx-font-weight: bold;");
                }
                setGraphic(btn);
            }
        });
        tablaCatalogos.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarFormulario(sel);
        });
    }

    @FXML
    public void fnSeleccionarCatalogo() {
        idEditando = -1;
        limpiarFormulario();
        cargarDatos();
        CatalogoConf conf = catalogoActual();
        if (conf != null) {
            colDescripcion.setVisible(conf.hasDesc);
            panelDescripcion.setVisible(conf.hasDesc);
            lblTotalRegistros.setText("Cargando...");
        }
    }

    private CatalogoConf catalogoActual() {
        String sel = cmbCatalogo.getValue();
        return sel != null ? CATALOGOS.get(sel) : null;
    }

    private void cargarDatos() {
        CatalogoConf conf = catalogoActual();
        if (conf == null) return;
        lista.clear();

        String cols = conf.idCol + ", " + conf.nombreCol;
        if (conf.hasDesc) cols += ", " + conf.descCol;
        if (conf.hasEstado) cols += ", estado";

        String sql = "SELECT " + cols + " FROM " + conf.tabla + " ORDER BY " + conf.nombreCol;

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt(conf.idCol);
                String nom = rs.getString(conf.nombreCol);
                String desc = conf.hasDesc ? rs.getString(conf.descCol) : null;
                boolean act = conf.hasEstado ? rs.getBoolean("estado") : true;
                lista.add(new CatalogoItem(id, nom, desc, act));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando " + cmbCatalogo.getValue() + ": " + e.getMessage());
        }
        tablaCatalogos.setItems(lista);
        lblTotalRegistros.setText("Total: " + lista.size() + " registros");
    }

    @FXML
    public void fnGuardar() {
        CatalogoConf conf = catalogoActual();
        if (conf == null) { mostrarAdvertencia("Seleccione un catálogo."); return; }

        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) { mostrarAdvertencia("El nombre es obligatorio."); return; }

        String descripcion = conf.hasDesc ? txtDescripcion.getText().trim() : null;

        if (idEditando > 0) {
            actualizar(conf, idEditando, nombre, descripcion);
        } else {
            insertar(conf, nombre, descripcion);
        }
    }

    private void insertar(CatalogoConf conf, String nombre, String descripcion) {
        String cols = conf.nombreCol;
        String vals = "?";
        if (conf.hasDesc) { cols += ", " + conf.descCol; vals += ", ?"; }
        String sql = "INSERT INTO " + conf.tabla + " (" + cols + ") VALUES (" + vals + ")";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            if (conf.hasDesc) ps.setString(2, descripcion != null ? descripcion : "");
            ps.executeUpdate();
            mostrarInfo("Registro creado exitosamente.");
            limpiarFormulario();
            cargarDatos();
        } catch (SQLException e) {
            mostrarError("Error al crear: " + e.getMessage());
        }
    }

    private void actualizar(CatalogoConf conf, int id, String nombre, String descripcion) {
        String sql = "UPDATE " + conf.tabla + " SET " + conf.nombreCol + " = ?";
        if (conf.hasDesc) sql += ", " + conf.descCol + " = ?";
        sql += " WHERE " + conf.idCol + " = ?";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            if (conf.hasDesc) { ps.setString(2, descripcion != null ? descripcion : ""); ps.setInt(3, id); }
            else { ps.setInt(2, id); }
            ps.executeUpdate();
            mostrarInfo("Registro actualizado exitosamente.");
            limpiarFormulario();
            cargarDatos();
        } catch (SQLException e) {
            mostrarError("Error al actualizar: " + e.getMessage());
        }
    }

    private void toggleEstado(CatalogoItem item) {
        CatalogoConf conf = catalogoActual();
        if (conf == null || !conf.hasEstado) return;
        int nuevo = item.isActivo() ? 0 : 1;
        String sql = "UPDATE " + conf.tabla + " SET estado = ? WHERE " + conf.idCol + " = ?";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevo);
            ps.setInt(2, item.getId());
            ps.executeUpdate();
            cargarDatos();
        } catch (SQLException e) {
            mostrarError("Error al cambiar estado: " + e.getMessage());
        }
    }

    private void cargarFormulario(CatalogoItem item) {
        idEditando = item.getId();
        lblTituloForm.setText("Editando: " + item.getNombre());
        txtNombre.setText(item.getNombre());
        if (catalogoActual().hasDesc) txtDescripcion.setText(item.getDescripcion());
        btnGuardar.setText("Actualizar");
    }

    private void limpiarFormulario() {
        idEditando = -1;
        lblTituloForm.setText("Nuevo Registro");
        txtNombre.clear();
        txtDescripcion.clear();
        btnGuardar.setText("Guardar");
    }

    @FXML
    public void fnCancelar() { limpiarFormulario(); }

    @FXML
    public void fnVolver() { AppNavigator.cargarDashboard(); }

    public static class CatalogoItem {
        private final int id;
        private final String nombre;
        private final String descripcion;
        private final boolean activo;

        public CatalogoItem(int id, String nombre, String descripcion, boolean activo) {
            this.id = id; this.nombre = nombre; this.descripcion = descripcion; this.activo = activo;
        }
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getDescripcion() { return descripcion; }
        public boolean isActivo() { return activo; }
    }
}
