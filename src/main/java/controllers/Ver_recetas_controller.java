package controllers;

import Data_base.CONEXION;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Producto;
import model.RecetaDetalle;
import model.RecetaResumen;
import model.Unidad;
import utils.AppNavigator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import static utils.AlertHelper.*;

public class Ver_recetas_controller {

    @FXML private TableView<RecetaResumen> tablaRecetas;
    @FXML private TableColumn<RecetaResumen, Integer> colId;
    @FXML private TableColumn<RecetaResumen, String> colProducto;
    @FXML private TableColumn<RecetaResumen, String> colIngredientes;
    @FXML private TableColumn<RecetaResumen, Void> colAccion;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;

    private CONEXION conexion = new CONEXION();
    private AppNavigator appNavigator = new AppNavigator();
    private ObservableList<RecetaResumen> listaRecetas = FXCollections.observableArrayList(
            item -> new javafx.beans.Observable[]{item.idProductoProperty(), item.nombreProductoProperty(), item.numIngredientesProperty()}
    );

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarRecetas();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(cellData -> cellData.getValue().idProductoProperty().asObject());
        colProducto.setCellValueFactory(cellData -> cellData.getValue().nombreProductoProperty());
        colIngredientes.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNumIngredientes())));

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("Ver");
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox contenedor = new HBox(6, btnVer, btnEditar, btnEliminar);

            {
                btnVer.setStyle(
                        "-fx-background-color: #38bdf8; -fx-text-fill: #100e0a; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 7 12;"
                );
                btnEditar.setStyle(
                        "-fx-background-color: #cdb08e; -fx-text-fill: #100e0a; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 7 12;"
                );
                btnEliminar.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 7 12;"
                );

                btnVer.setOnAction(e -> {
                    RecetaResumen item = getTableView().getItems().get(getIndex());
                    fnVerReceta(item);
                });
                btnEditar.setOnAction(e -> {
                    RecetaResumen item = getTableView().getItems().get(getIndex());
                    fnEditarReceta(item);
                });
                btnEliminar.setOnAction(e -> {
                    RecetaResumen item = getTableView().getItems().get(getIndex());
                    fnEliminarReceta(item);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tablaRecetas.setItems(listaRecetas);
    }

    private void cargarRecetas() {
        listaRecetas.clear();
        String textoBusqueda = txtBuscar.getText().trim();

        String sql = "SELECT p.id_producto, p.nombre, COUNT(rd.id_receta_detalle) as num_ingredientes " +
                   "FROM PRODUCTO p " +
                   "INNER JOIN RECETA_DETALLE rd ON p.id_producto = rd.id_producto_final " +
                   "WHERE p.tipo_producto = 'PRODUCTO_TERMINADO' ";

        if (!textoBusqueda.isEmpty()) {
            sql += " AND LOWER(p.nombre) LIKE ? ";
        }

        sql += " GROUP BY p.id_producto, p.nombre ORDER BY p.nombre";

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!textoBusqueda.isEmpty()) {
                ps.setString(1, "%" + textoBusqueda.toLowerCase() + "%");
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaRecetas.add(new RecetaResumen(
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getInt("num_ingredientes")
                ));
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar recetas: " + e.getMessage());
        }

        lblTotal.setText("Total: " + listaRecetas.size() + " receta(s)");
    }

    @FXML
    public void fnBuscar(ActionEvent event) {
        cargarRecetas();
    }

    private void fnVerReceta(RecetaResumen receta) {
        StringBuilder info = new StringBuilder();
        info.append("Ingredientes de: ").append(receta.getNombreProducto()).append("\n\n");

        String sql = "SELECT p.nombre, rd.cantidad, rd.unidad_medida " +
                   "FROM RECETA_DETALLE rd " +
                   "INNER JOIN PRODUCTO p ON rd.id_producto_ingrediente = p.id_producto " +
                   "WHERE rd.id_producto_final = ? ORDER BY p.nombre";

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, receta.getIdProducto());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                info.append("• ").append(rs.getString("nombre"))
                    .append(" — ").append(rs.getBigDecimal("cantidad").stripTrailingZeros().toPlainString())
                    .append(" ").append(rs.getString("unidad_medida") != null ? rs.getString("unidad_medida") : "")
                    .append("\n");
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar receta: " + e.getMessage());
        }

        Alert a = new Alert(Alert.AlertType.INFORMATION, info.toString(), ButtonType.OK);
        a.setTitle(receta.getNombreProducto());
        a.setHeaderText("Detalle de Receta");
        a.showAndWait();
    }

    private void fnEditarReceta(RecetaResumen receta) {
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Editar Receta");
        dialogo.setHeaderText("Editando receta de: " + receta.getNombreProducto());

        ButtonType btnGuardar = new ButtonType("Guardar Cambios", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        ObservableList<RecetaDetalle> listaIngredientes = FXCollections.observableArrayList(
                item -> new javafx.beans.Observable[]{item.nombreIngredienteProperty(), item.cantidadProperty(), item.unidadMedidaProperty()}
        );
        cargarIngredientes(receta.getIdProducto(), listaIngredientes);

        TableView<RecetaDetalle> tblIngredientes = new TableView<>();
        tblIngredientes.setPrefHeight(200);

        TableColumn<RecetaDetalle, String> colIng = new TableColumn<>("Ingrediente");
        colIng.setPrefWidth(250);
        colIng.setCellValueFactory(c -> c.getValue().nombreIngredienteProperty());

        TableColumn<RecetaDetalle, Double> colCant = new TableColumn<>("Cantidad");
        colCant.setPrefWidth(80);
        colCant.setCellValueFactory(c -> c.getValue().cantidadProperty().asObject());

        TableColumn<RecetaDetalle, String> colUnd = new TableColumn<>("Unidad");
        colUnd.setPrefWidth(100);
        colUnd.setCellValueFactory(c -> c.getValue().unidadMedidaProperty());

        TableColumn<RecetaDetalle, Void> colQuitar = new TableColumn<>("");
        colQuitar.setPrefWidth(70);
        colQuitar.setCellFactory(col -> new TableCell<>() {
            private final Button btnQ = new Button("X");
            {
                btnQ.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 4 8;");
                btnQ.setOnAction(e -> {
                    RecetaDetalle item = getTableView().getItems().get(getIndex());
                    listaIngredientes.remove(item);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btnQ);
            }
        });

        tblIngredientes.getColumns().addAll(colIng, colCant, colUnd, colQuitar);
        tblIngredientes.setItems(listaIngredientes);

        ComboBox<Producto> cmbNuevoIng = new ComboBox<>();
        ObservableList<Producto> productosIng = FXCollections.observableArrayList();
        String sqlP = "SELECT id_producto, nombre FROM PRODUCTO WHERE tipo_producto = 'MATERIA_PRIMA' ORDER BY nombre";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sqlP);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productosIng.add(new Producto(rs.getInt("id_producto"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar datos: " + e.getMessage());
        }
        cmbNuevoIng.setItems(productosIng);
        cmbNuevoIng.setPromptText("Seleccionar...");

        TextField txtCantNueva = new TextField();
        txtCantNueva.setPromptText("Cantidad");
        txtCantNueva.setPrefWidth(80);

        ComboBox<Unidad> cmbUnidadNueva = new ComboBox<>();
        ObservableList<Unidad> unidades = FXCollections.observableArrayList();
        String sqlU = "SELECT id_unidad, nombre FROM UNIDAD ORDER BY nombre";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sqlU);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                unidades.add(new Unidad(rs.getInt("id_unidad"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar datos: " + e.getMessage());
        }
        cmbUnidadNueva.setItems(unidades);
        cmbUnidadNueva.setPromptText("Unidad");

        Button btnAgregar = new Button("+ Agregar");
        btnAgregar.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 8 14;");
        btnAgregar.setOnAction(e -> {
            Producto ing = cmbNuevoIng.getValue();
            String cantTexto = txtCantNueva.getText().trim();
            Unidad und = cmbUnidadNueva.getValue();
            if (ing == null || cantTexto.isEmpty() || und == null) return;
            try {
                double cant = Double.parseDouble(cantTexto);
                listaIngredientes.add(new RecetaDetalle(ing.getIdProducto(), ing.getNombre(), cant, und.getNombre()));
                cmbNuevoIng.setValue(null);
                txtCantNueva.clear();
                cmbUnidadNueva.setValue(null);
            } catch (NumberFormatException ex) {
                mostrarAdvertencia("Cantidad inválida.");
            }
        });

        HBox agregarRow = new HBox(8, cmbNuevoIng, txtCantNueva, cmbUnidadNueva, btnAgregar);
        agregarRow.setStyle("-fx-padding: 10 0 0 0;");

        VBox content = new VBox(8, tblIngredientes, agregarRow);
        content.setStyle("-fx-padding: 10;");
        dialogo.getDialogPane().setContent(content);

        dialogo.setResultConverter(dialogBtn -> {
            if (dialogBtn == btnGuardar) {
                guardarIngredientes(receta.getIdProducto(), listaIngredientes);
            }
            return null;
        });

        dialogo.showAndWait();
        cargarRecetas();
    }

    private void cargarIngredientes(int idProductoFinal, ObservableList<RecetaDetalle> lista) {
        String sql = "SELECT rd.id_producto_ingrediente, p.nombre, rd.cantidad, rd.unidad_medida " +
                   "FROM RECETA_DETALLE rd " +
                   "INNER JOIN PRODUCTO p ON rd.id_producto_ingrediente = p.id_producto " +
                   "WHERE rd.id_producto_final = ? ORDER BY p.nombre";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProductoFinal);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new RecetaDetalle(
                        rs.getInt("id_producto_ingrediente"),
                        rs.getString("nombre"),
                        rs.getDouble("cantidad"),
                        rs.getString("unidad_medida")
                ));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando ingredientes: " + e.getMessage());
        }
    }

    private void guardarIngredientes(int idProductoFinal, ObservableList<RecetaDetalle> listaIngredientes) {
        String sqlDelete = "DELETE FROM RECETA_DETALLE WHERE id_producto_final = ?";
        String sqlInsert = "INSERT INTO RECETA_DETALLE (id_producto_final, id_producto_ingrediente, cantidad, unidad_medida) VALUES (?, ?, ?, ?)";

        try (Connection conn = conexion.establecerconexio()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                psDel.setInt(1, idProductoFinal);
                psDel.executeUpdate();
            }

            try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                for (RecetaDetalle ing : listaIngredientes) {
                    psIns.setInt(1, idProductoFinal);
                    psIns.setInt(2, ing.getIdProductoIngrediente());
                    psIns.setBigDecimal(3, BigDecimal.valueOf(ing.getCantidad()));
                    psIns.setString(4, ing.getUnidadMedida());
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            conn.commit();
            mostrarInfo("Receta actualizada exitosamente.");
        } catch (SQLException e) {
            mostrarError("Error al guardar receta: " + e.getMessage());
        }
    }

    private void fnEliminarReceta(RecetaResumen receta) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Está seguro que desea eliminar TODA la receta de \"" + receta.getNombreProducto() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);

        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        String sql = "DELETE FROM RECETA_DETALLE WHERE id_producto_final = ?";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, receta.getIdProducto());
            ps.executeUpdate();
            mostrarInfo("Receta eliminada exitosamente.");
            cargarRecetas();
        } catch (SQLException e) {
            mostrarError("Error al eliminar: " + e.getMessage());
        }
    }

    public void fnVolverMenu() {
        appNavigator.volverMenu();
    }



}