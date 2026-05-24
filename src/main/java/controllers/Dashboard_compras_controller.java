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

public class Dashboard_compras_controller {

    @FXML private ToggleButton btnDia;
    @FXML private ToggleButton btnSemana;
    @FXML private ToggleButton btnMes;
    @FXML private ToggleButton btnTrimestre;
    @FXML private VBox vboxBarrasProveedores;
    @FXML private VBox vboxBarrasCategorias;
    @FXML private PieChart pieChart;
    @FXML private Label lblPeriodo;
    @FXML private Label lblTotalCompras;
    @FXML private Label lblPendientes;
    @FXML private Label lblPagadas;
    @FXML private Label lblTotalGastado;
    @FXML private Label lblProveedores;

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
        cargarKPIs(inicio, fin);
        cargarBarrasProveedores(inicio, fin);
        cargarBarrasCategorias(inicio, fin);
        cargarPieChart();
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

    private void cargarKPIs(LocalDate inicio, LocalDate fin) {
        String sqlTotal = "SELECT COUNT(*) as total FROM COMPRA_MATERIAL";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sqlTotal);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) lblTotalCompras.setText(String.valueOf(rs.getInt("total")));
        } catch (SQLException e) {
            mostrarError("Error cargando total compras: " + e.getMessage());
        }

        String sqlPeriodo = "SELECT cm.estado, COUNT(*) as total, COALESCE(SUM(dc.monto_total), 0) as gastado "
                          + "FROM COMPRA_MATERIAL cm "
                          + "LEFT JOIN DETALLE_COMPRA dc ON cm.id_compra_material = dc.id_compra_material "
                          + "WHERE cm.fecha BETWEEN ? AND ? "
                          + "GROUP BY cm.estado";
        int pendientes = 0, pagadas = 0;
        double totalGastado = 0;
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sqlPeriodo)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String estado = rs.getString("estado");
                int total = rs.getInt("total");
                if ("PENDIENTE".equals(estado)) pendientes = total;
                else if ("PAGADA".equals(estado)) { pagadas = total; totalGastado += rs.getDouble("gastado"); }
            }
        } catch (SQLException e) {
            mostrarError("Error cargando KPIs compras: " + e.getMessage());
        }

        lblPendientes.setText(String.valueOf(pendientes));
        lblPagadas.setText(String.valueOf(pagadas));
        lblTotalGastado.setText(FMT.format(totalGastado));

        String sqlProv = "SELECT COUNT(DISTINCT id_proveedor) as total FROM COMPRA_MATERIAL WHERE fecha BETWEEN ? AND ?";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sqlProv)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) lblProveedores.setText(String.valueOf(rs.getInt("total")));
        } catch (SQLException e) {
            mostrarError("Error cargando proveedores: " + e.getMessage());
        }
    }

    private void cargarBarrasProveedores(LocalDate inicio, LocalDate fin) {
        vboxBarrasProveedores.getChildren().clear();
        String sql = "SELECT TOP 5 pv.nombre, COALESCE(SUM(dc.monto_total), 0) as total "
                   + "FROM COMPRA_MATERIAL cm "
                   + "INNER JOIN PROVEEDOR pv ON cm.id_proveedor = pv.id_proveedor "
                   + "LEFT JOIN DETALLE_COMPRA dc ON cm.id_compra_material = dc.id_compra_material "
                   + "WHERE cm.estado = 'PAGADA' AND cm.fecha BETWEEN ? AND ? "
                   + "GROUP BY pv.id_proveedor, pv.nombre ORDER BY total DESC";

        List<String> nombres = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                nombres.add(rs.getString("nombre"));
                valores.add(rs.getDouble("total"));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando proveedores: " + e.getMessage());
            return;
        }

        if (nombres.isEmpty()) {
            vboxBarrasProveedores.getChildren().add(crearVacio());
            return;
        }

        double maxValor = valores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        for (int i = 0; i < nombres.size(); i++) {
            vboxBarrasProveedores.getChildren().add(
                crearBarraRow(nombres.get(i), valores.get(i), maxValor, "#f59e0b")
            );
        }
    }

    private void cargarBarrasCategorias(LocalDate inicio, LocalDate fin) {
        vboxBarrasCategorias.getChildren().clear();
        String sql = "SELECT TOP 5 cp.nombre as categoria, COALESCE(SUM(dc.monto_total), 0) as total "
                   + "FROM COMPRA_MATERIAL cm "
                   + "INNER JOIN DETALLE_COMPRA dc ON cm.id_compra_material = dc.id_compra_material "
                   + "INNER JOIN PRODUCTO p ON dc.id_producto = p.id_producto "
                   + "INNER JOIN CATEGORIA_PRODUCTO cp ON p.id_categoria_producto = cp.id_categoria_producto "
                   + "WHERE cm.estado = 'PAGADA' AND cm.fecha BETWEEN ? AND ? "
                   + "GROUP BY cp.id_categoria_producto, cp.nombre ORDER BY total DESC";

        List<String> nombres = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                nombres.add(rs.getString("categoria"));
                valores.add(rs.getDouble("total"));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando categorías: " + e.getMessage());
            return;
        }

        if (nombres.isEmpty()) {
            vboxBarrasCategorias.getChildren().add(crearVacio());
            return;
        }

        double maxValor = valores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        for (int i = 0; i < nombres.size(); i++) {
            vboxBarrasCategorias.getChildren().add(
                crearBarraRow(nombres.get(i), valores.get(i), maxValor, "#8b5cf6")
            );
        }
    }

    private void cargarPieChart() {
        pieChart.getData().clear();
        String sql = "SELECT estado, COUNT(*) as total FROM COMPRA_MATERIAL GROUP BY estado";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            while (rs.next()) {
                int total = rs.getInt("total");
                if (total > 0) {
                    pieData.add(new PieChart.Data(traducirEstado(rs.getString("estado")), total));
                }
            }
            if (!pieData.isEmpty()) {
                pieChart.setData(pieData);
            }
        } catch (SQLException e) {
            mostrarError("Error cargando gráfico: " + e.getMessage());
        }
    }

    private String traducirEstado(String estado) {
        switch (estado) {
            case "PENDIENTE": return "Pendientes";
            case "PAGADA": return "Pagadas";
            case "CANCELADA": return "Canceladas";
            default: return estado;
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

        Label lblValor = new Label(FMT.format(valor));
        lblValor.getStyleClass().add("custom-bar-value");

        barContainer.getChildren().addAll(track, bar);
        Tooltip.install(barContainer, new Tooltip(FMT.format(valor)));

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
