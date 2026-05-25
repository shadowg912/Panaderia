package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import utils.AppNavigator;

import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static utils.AlertHelper.mostrarError;

public class Dashboard_inventario_controller {

    @FXML private ToggleButton btnDia;
    @FXML private ToggleButton btnSemana;
    @FXML private ToggleButton btnMes;
    @FXML private ToggleButton btnTrimestre;
    @FXML private VBox vboxBarrasCategoria;
    @FXML private PieChart pieChartTipo;
    @FXML private Label lblPeriodo;
    @FXML private Label lblTotalProductos;
    @FXML private Label lblStockBajo;
    @FXML private Label lblStockBajoTexto;
    @FXML private Label lblValorInventario;
    @FXML private Label lblEntradas;
    @FXML private Label lblSalidas;

    private final CONEXION conexion = new CONEXION();
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final AppNavigator appNavigator = new AppNavigator();
    private static final NumberFormat FMT = NumberFormat.getCurrencyInstance(new Locale("es", "DO"));

    @FXML
    public void initialize() {
        btnDia.setToggleGroup(toggleGroup);
        btnSemana.setToggleGroup(toggleGroup);
        btnMes.setToggleGroup(toggleGroup);
        btnTrimestre.setToggleGroup(toggleGroup);

        btnMes.setSelected(true);

        toggleGroup.selectedToggleProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarDashboard();
        });

        cargarDashboard();
    }

    private void cargarDashboard() {
        String periodo = getPeriodoLabel();
        LocalDate[] rango = getRango(periodo);
        LocalDate inicio = rango[0];
        LocalDate fin = rango[1];

        lblPeriodo.setText(periodo + " (" + inicio + " — " + fin + ")");

        cargarBarrasCategoria();
        cargarPieChartTipo();
        cargarTotalProductos();
        cargarStockBajo();
        cargarValorInventario();
        cargarMovimientos(inicio, fin);
    }

    private String getPeriodoLabel() {
        if (btnDia.isSelected()) return "Hoy";
        if (btnSemana.isSelected()) return "Última semana";
        if (btnMes.isSelected()) return "Último mes";
        return "Último trimestre";
    }

    private LocalDate[] getRango(String periodo) {
        LocalDate fin = LocalDate.now();
        LocalDate inicio;
        switch (periodo) {
            case "Hoy":               inicio = fin; break;
            case "Última semana":     inicio = fin.minusDays(6); break;
            case "Último trimestre":  inicio = fin.minusDays(89); break;
            default:                  inicio = fin.minusDays(29); break;
        }
        return new LocalDate[]{inicio, fin};
    }

    private void cargarBarrasCategoria() {
        vboxBarrasCategoria.getChildren().clear();
        String sql = "SELECT cp.nombre, COALESCE(SUM(i.stock_actual), 0) as stock "
                   + "FROM CATEGORIA_PRODUCTO cp "
                   + "LEFT JOIN PRODUCTO p ON cp.id_categoria_producto = p.id_categoria_producto AND p.estado = 1 "
                   + "LEFT JOIN [dbo].[INVENTARIO] i ON p.id_producto = i.id_producto "
                   + "WHERE cp.estado = 1 "
                   + "GROUP BY cp.id_categoria_producto, cp.nombre "
                   + "ORDER BY stock DESC";

        List<String> categorias = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(rs.getString("nombre"));
                valores.add(rs.getDouble("stock"));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando stock por categoría: " + e.getMessage());
            return;
        }

        if (categorias.isEmpty()) {
            vboxBarrasCategoria.getChildren().add(crearVacio());
            return;
        }

        double maxValor = valores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        for (int i = 0; i < categorias.size(); i++) {
            vboxBarrasCategoria.getChildren().add(
                crearBarraRow(categorias.get(i), valores.get(i), maxValor, "#cdb08e")
            );
        }
    }

    private void cargarPieChartTipo() {
        pieChartTipo.getData().clear();
        String sql = "SELECT tipo_producto, COUNT(*) as total FROM PRODUCTO WHERE estado = 1 GROUP BY tipo_producto";
        String[] colores = {"#10b981", "#38bdf8", "#f59e0b"};

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            while (rs.next()) {
                int total = rs.getInt("total");
                if (total > 0) {
                    pieData.add(new PieChart.Data(traducirTipo(rs.getString("tipo_producto")), total));
                }
            }
            if (!pieData.isEmpty()) {
                pieChartTipo.setData(pieData);
                for (int i = 0; i < pieData.size() && i < colores.length; i++) {
                    final int idx = i;
                    pieData.get(i).nodeProperty().addListener((obs, old, node) -> {
                        if (node != null) node.setStyle("-fx-pie-color: " + colores[idx] + ";");
                    });
                }
            }
        } catch (SQLException e) {
            mostrarError("Error cargando distribución: " + e.getMessage());
        }
    }

    private String traducirTipo(String tipo) {
        switch (tipo) {
            case "PRODUCTO_TERMINADO": return "Terminado";
            case "MATERIA_PRIMA":      return "Materia Prima";
            case "MATERIAL_EMPAQUE":   return "Empaque";
            default:                   return tipo;
        }
    }

    private void cargarTotalProductos() {
        String sql = "SELECT COUNT(*) as total FROM PRODUCTO WHERE estado = 1";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lblTotalProductos.setText(String.valueOf(rs.getInt("total")));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando total productos: " + e.getMessage());
        }
    }

    private void cargarStockBajo() {
        String sql = "SELECT COUNT(*) as bajo FROM PRODUCTO p "
                    + "LEFT JOIN [dbo].[INVENTARIO] i ON p.id_producto = i.id_producto "
                   + "WHERE p.estado = 1 AND COALESCE(i.stock_actual, 0) < 5";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int total = rs.getInt("bajo");
                lblStockBajo.setText(String.valueOf(total));
                lblStockBajoTexto.setText(total == 1 ? "producto" : "productos");
            }
        } catch (SQLException e) {
            mostrarError("Error cargando stock bajo: " + e.getMessage());
        }
    }

    private void cargarValorInventario() {
        String sql = "SELECT COALESCE(SUM(p.precio_unitario * COALESCE(i.stock_actual, 0)), 0) as valor "
                    + "FROM PRODUCTO p LEFT JOIN [dbo].[INVENTARIO] i ON p.id_producto = i.id_producto "
                   + "WHERE p.estado = 1";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lblValorInventario.setText(FMT.format(rs.getDouble("valor")));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando valor inventario: " + e.getMessage());
        }
    }

    private void cargarMovimientos(LocalDate inicio, LocalDate fin) {
        String sql = "SELECT tm.naturaleza, COUNT(*) as total "
                   + "FROM MOVIMIENTO_INVENTARIO m "
                   + "INNER JOIN TIPO_MOVIMIENTO tm ON m.id_tipo_movimiento = tm.id_tipo "
                   + "WHERE m.fecha BETWEEN ? AND ? "
                   + "GROUP BY tm.naturaleza";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            int entradas = 0, salidas = 0;
            while (rs.next()) {
                if ("ENTRADA".equals(rs.getString("naturaleza"))) entradas = rs.getInt("total");
                else if ("SALIDA".equals(rs.getString("naturaleza"))) salidas = rs.getInt("total");
            }
            lblEntradas.setText(String.valueOf(entradas));
            lblSalidas.setText(String.valueOf(salidas));
        } catch (SQLException e) {
            mostrarError("Error cargando movimientos: " + e.getMessage());
        }
    }

    private HBox crearBarraRow(String label, double valor, double maxValor, String barColor) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("custom-bar-row");

        Label lblLabel = new Label(label);
        lblLabel.getStyleClass().add("custom-bar-date");
        lblLabel.setMinWidth(120);

        StackPane barContainer = new StackPane();
        barContainer.setPrefHeight(26);
        HBox.setHgrow(barContainer, Priority.ALWAYS);

        Rectangle track = new Rectangle();
        track.setWidth(1);
        track.heightProperty().bind(barContainer.heightProperty());
        track.getStyleClass().add("custom-bar-track");
        track.widthProperty().bind(barContainer.widthProperty());

        double ratio = maxValor > 0 ? Math.min(valor / maxValor, 1.0) : 0;
        Rectangle bar = new Rectangle();
        bar.setHeight(26);
        bar.getStyleClass().add("custom-bar-fill");
        bar.setStyle("-fx-fill: " + barColor + ";");
        bar.widthProperty().bind(barContainer.widthProperty().multiply(ratio));

        StackPane.setAlignment(bar, Pos.CENTER_LEFT);

        Label lblValor = new Label(String.format("%,.0f", valor));
        lblValor.getStyleClass().add("custom-bar-value");

        barContainer.getChildren().addAll(track, bar);
        Tooltip.install(barContainer, new Tooltip(String.format("%,.0f", valor) + " unidades"));

        row.getChildren().addAll(lblLabel, barContainer, lblValor);
        return row;
    }

    private Label crearVacio() {
        Label lbl = new Label("Sin datos en este período");
        lbl.setStyle("-fx-text-fill: #bfb6a6; -fx-font-size: 12px; -fx-font-style: italic;");
        return lbl;
    }

    @FXML
    public void fnVolverMenu() {
        appNavigator.volverMenu();
    }
}
