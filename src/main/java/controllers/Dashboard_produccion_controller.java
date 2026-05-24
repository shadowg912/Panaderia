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

public class Dashboard_produccion_controller {

    @FXML private ToggleButton btnDia;
    @FXML private ToggleButton btnSemana;
    @FXML private ToggleButton btnMes;
    @FXML private ToggleButton btnTrimestre;
    @FXML private VBox vboxBarrasProductos;
    @FXML private VBox vboxBarrasEmpleados;
    @FXML private PieChart pieChart;
    @FXML private Label lblPeriodo;
    @FXML private Label lblTotalOrdenes;
    @FXML private Label lblPendientes;
    @FXML private Label lblEnProceso;
    @FXML private Label lblCompletadas;
    @FXML private Label lblTasaCumplimiento;

    private final CONEXION conexion = new CONEXION();
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final AppNavigator appNavigator = new AppNavigator();
    private static final NumberFormat FMT_PCT = NumberFormat.getPercentInstance(new Locale("es", "DO"));

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
        cargarBarrasProductos(inicio, fin);
        cargarBarrasEmpleados(inicio, fin);
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
        String sqlTotal = "SELECT COUNT(*) as total FROM ORDEN_PRODUCCION";
        String sqlPeriodo = "SELECT estado, COUNT(*) as total FROM ORDEN_PRODUCCION WHERE fecha_produccion BETWEEN ? AND ? GROUP BY estado";

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sqlTotal);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) lblTotalOrdenes.setText(String.valueOf(rs.getInt("total")));
        } catch (SQLException e) {
            mostrarError("Error cargando total órdenes: " + e.getMessage());
        }

        int pendientes = 0, enProceso = 0, completadas = 0;
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sqlPeriodo)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String estado = rs.getString("estado");
                int total = rs.getInt("total");
                switch (estado) {
                    case "Pendiente": pendientes = total; break;
                    case "En Proceso": enProceso = total; break;
                    case "Completada": completadas = total; break;
                }
            }
        } catch (SQLException e) {
            mostrarError("Error cargando KPIs: " + e.getMessage());
        }

        lblPendientes.setText(String.valueOf(pendientes));
        lblEnProceso.setText(String.valueOf(enProceso));
        lblCompletadas.setText(String.valueOf(completadas));

        int totalPeriodo = pendientes + enProceso + completadas;
        double tasa = totalPeriodo > 0 ? (double) completadas / totalPeriodo : 0;
        lblTasaCumplimiento.setText(FMT_PCT.format(tasa));
    }

    private void cargarBarrasProductos(LocalDate inicio, LocalDate fin) {
        vboxBarrasProductos.getChildren().clear();
        String sql = "SELECT TOP 5 p.nombre, SUM(op.cantidad_planificada) as total "
                   + "FROM ORDEN_PRODUCCION op "
                   + "INNER JOIN PRODUCTO p ON op.id_producto = p.id_producto "
                   + "WHERE op.estado = 'Completada' AND op.fecha_produccion BETWEEN ? AND ? "
                   + "GROUP BY p.id_producto, p.nombre ORDER BY total DESC";

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
            mostrarError("Error cargando productos: " + e.getMessage());
            return;
        }

        if (nombres.isEmpty()) {
            vboxBarrasProductos.getChildren().add(crearVacio());
            return;
        }

        double maxValor = valores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        for (int i = 0; i < nombres.size(); i++) {
            vboxBarrasProductos.getChildren().add(
                crearBarraRow(nombres.get(i), valores.get(i), maxValor, "#10b981")
            );
        }
    }

    private void cargarBarrasEmpleados(LocalDate inicio, LocalDate fin) {
        vboxBarrasEmpleados.getChildren().clear();
        String sql = "SELECT TOP 5 e.nombre + ' ' + e.apellido1 as empleado, COUNT(*) as total "
                   + "FROM ORDEN_PRODUCCION op "
                   + "INNER JOIN EMPLEADO e ON op.id_empleado = e.id_empleado "
                   + "WHERE op.estado = 'Completada' AND op.fecha_produccion BETWEEN ? AND ? "
                   + "GROUP BY e.id_empleado, e.nombre, e.apellido1 ORDER BY total DESC";

        List<String> nombres = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                nombres.add(rs.getString("empleado"));
                valores.add((double) rs.getInt("total"));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando empleados: " + e.getMessage());
            return;
        }

        if (nombres.isEmpty()) {
            vboxBarrasEmpleados.getChildren().add(crearVacio());
            return;
        }

        double maxValor = valores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        for (int i = 0; i < nombres.size(); i++) {
            vboxBarrasEmpleados.getChildren().add(
                crearBarraRow(nombres.get(i), valores.get(i), maxValor, "#38bdf8")
            );
        }
    }

    private void cargarPieChart() {
        pieChart.getData().clear();
        String sql = "SELECT estado, COUNT(*) as total FROM ORDEN_PRODUCCION GROUP BY estado";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            while (rs.next()) {
                int total = rs.getInt("total");
                if (total > 0) {
                    pieData.add(new PieChart.Data(rs.getString("estado"), total));
                }
            }
            if (!pieData.isEmpty()) {
                pieChart.setData(pieData);
            }
        } catch (SQLException e) {
            mostrarError("Error cargando gráfico: " + e.getMessage());
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
