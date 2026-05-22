package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import utils.AppNavigator;

import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static utils.AlertHelper.mostrarError;

public class Dashboard_ventas_controller {

    @FXML private ToggleButton btnDia;
    @FXML private ToggleButton btnSemana;
    @FXML private ToggleButton btnMes;
    @FXML private ToggleButton btnTrimestre;
    @FXML private VBox vboxBarras;
    @FXML private PieChart pieChart;
    @FXML private Label lblPeriodo;
    @FXML private Label lblTotalGanancias;
    @FXML private VBox vboxTopProductos;
    @FXML private VBox vboxTopClientes;
    @FXML private Label lblEntregados;
    @FXML private Label lblCancelados;

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

        cargarBarChart(inicio, fin, periodo);
        cargarTotalGanancias(inicio, fin);
        cargarTopProductos(inicio, fin);
        cargarTopClientes(inicio, fin);
        cargarEnviosStats();
        cargarPieChart(inicio, fin);
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

    private void cargarBarChart(LocalDate inicio, LocalDate fin, String periodo) {
        vboxBarras.getChildren().clear();

        String sql;
        boolean esTrimestre = periodo.equals("Último trimestre");
        if (esTrimestre) {
            sql = "SELECT YEAR(fecha_orden) as año, MONTH(fecha_orden) as mes, COALESCE(SUM(monto_total), 0) as total "
                + "FROM ORDEN_VENTA WHERE estado != 'CANCELADA' AND fecha_orden BETWEEN ? AND ? "
                + "GROUP BY YEAR(fecha_orden), MONTH(fecha_orden) ORDER BY año, mes";
        } else {
            sql = "SELECT CAST(fecha_orden AS DATE) as fecha, COALESCE(SUM(monto_total), 0) as total "
                + "FROM ORDEN_VENTA WHERE estado != 'CANCELADA' AND fecha_orden BETWEEN ? AND ? "
                + "GROUP BY CAST(fecha_orden AS DATE) ORDER BY fecha";
        }

        List<String> fechas = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String fechaLabel;
                if (esTrimestre) {
                    int mes = rs.getInt("mes");
                    LocalDate fechaMes = LocalDate.of(rs.getInt("año"), mes, 1);
                    String monthName = fechaMes.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                    fechaLabel = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1);
                } else {
                    java.sql.Date fechaSQL = rs.getDate("fecha");
                    fechaLabel = fechaSQL != null
                        ? String.format("%02d/%02d", fechaSQL.toLocalDate().getDayOfMonth(), fechaSQL.toLocalDate().getMonthValue())
                        : "—";
                }
                fechas.add(fechaLabel);
                valores.add(rs.getDouble("total"));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando gráfico: " + e.getMessage());
            return;
        }

        if (fechas.isEmpty()) {
            vboxBarras.getChildren().add(crearVacio());
            return;
        }

        double maxValor = valores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        for (int i = 0; i < fechas.size(); i++) {
            vboxBarras.getChildren().add(crearBarraRow(fechas.get(i), valores.get(i), maxValor));
        }
    }

    private HBox crearBarraRow(String fecha, double valor, double maxValor) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("custom-bar-row");

        Label lblFecha = new Label(fecha);
        lblFecha.getStyleClass().add("custom-bar-date");

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
        bar.widthProperty().bind(barContainer.widthProperty().multiply(ratio));

        StackPane.setAlignment(bar, Pos.CENTER_LEFT);

        Label lblValor = new Label(String.format("%,.0f", valor));
        lblValor.getStyleClass().add("custom-bar-value");

        barContainer.getChildren().addAll(track, bar);
        Tooltip.install(barContainer, new Tooltip("RD$ " + String.format("%,.2f", valor)));

        row.getChildren().addAll(lblFecha, barContainer, lblValor);
        return row;
    }

    private void cargarTotalGanancias(LocalDate inicio, LocalDate fin) {
        String sql = "SELECT COALESCE(SUM(monto_total), 0) as total FROM ORDEN_VENTA WHERE estado != 'CANCELADA' AND fecha_orden BETWEEN ? AND ?";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblTotalGanancias.setText(FMT.format(rs.getDouble("total")));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando total: " + e.getMessage());
        }
    }

    private void cargarTopProductos(LocalDate inicio, LocalDate fin) {
        vboxTopProductos.getChildren().clear();
        String sql = "SELECT TOP 3 p.nombre, SUM(dov.cantidad) as total_cant, SUM(dov.subtotal) as total_ingresos "
                   + "FROM DETALLE_ORDEN_VENTA dov "
                   + "INNER JOIN PRODUCTO p ON dov.id_producto = p.id_producto "
                   + "INNER JOIN ORDEN_VENTA ov ON dov.id_orden_venta = ov.id_orden_venta "
                   + "WHERE ov.estado != 'CANCELADA' AND ov.fecha_orden BETWEEN ? AND ? "
                   + "GROUP BY p.id_producto, p.nombre ORDER BY total_ingresos DESC";

        List<String> nombres = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                nombres.add(rs.getString("nombre"));
                valores.add(rs.getDouble("total_ingresos"));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando productos: " + e.getMessage());
            return;
        }

        double maxValor = valores.isEmpty() ? 0 : valores.get(0);
        for (int i = 0; i < nombres.size(); i++) {
            vboxTopProductos.getChildren().add(
                crearRankingItem(i + 1, nombres.get(i), valores.get(i), maxValor, "#38bdf8")
            );
        }
        if (nombres.isEmpty()) vboxTopProductos.getChildren().add(crearVacio());
    }

    private void cargarTopClientes(LocalDate inicio, LocalDate fin) {
        vboxTopClientes.getChildren().clear();
        String sql = "SELECT TOP 5 c.razon_social, COALESCE(SUM(ov.monto_total), 0) as total "
                   + "FROM ORDEN_VENTA ov INNER JOIN CLIENTE c ON ov.id_cliente = c.id_cliente "
                   + "WHERE ov.estado != 'CANCELADA' AND ov.fecha_orden BETWEEN ? AND ? "
                   + "GROUP BY c.id_cliente, c.razon_social ORDER BY total DESC";

        List<String> nombres = new ArrayList<>();
        List<Double> valores = new ArrayList<>();

        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                nombres.add(rs.getString("razon_social"));
                valores.add(rs.getDouble("total"));
            }
        } catch (SQLException e) {
            mostrarError("Error cargando clientes: " + e.getMessage());
            return;
        }

        double maxValor = valores.isEmpty() ? 0 : valores.get(0);
        for (int i = 0; i < nombres.size(); i++) {
            vboxTopClientes.getChildren().add(
                crearRankingItem(i + 1, nombres.get(i), valores.get(i), maxValor, "#8b5cf6")
            );
        }
        if (nombres.isEmpty()) vboxTopClientes.getChildren().add(crearVacio());
    }

    private void cargarEnviosStats() {
        String sql = "SELECT ee.nombre as estado, COUNT(*) as total FROM ENVIO e "
                   + "INNER JOIN ESTADO_ENVIO ee ON e.id_estado_envio = ee.id_estado_envio "
                   + "WHERE ee.nombre IN ('ENTREGADO', 'CANCELADO') GROUP BY ee.nombre";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int entregados = 0, cancelados = 0;
            while (rs.next()) {
                if ("ENTREGADO".equals(rs.getString("estado"))) entregados = rs.getInt("total");
                else if ("CANCELADO".equals(rs.getString("estado"))) cancelados = rs.getInt("total");
            }
            lblEntregados.setText(String.valueOf(entregados));
            lblCancelados.setText(String.valueOf(cancelados));
        } catch (SQLException e) {
            mostrarError("Error cargando envíos: " + e.getMessage());
        }
    }

    private void cargarPieChart(LocalDate inicio, LocalDate fin) {
        pieChart.getData().clear();
        String sql = "SELECT estado, COUNT(*) as total FROM ORDEN_VENTA "
                   + "WHERE estado IN ('PENDIENTE','FACTURADA','CANCELADA') AND fecha_orden BETWEEN ? AND ? "
                   + "GROUP BY estado";
        try (Connection c = conexion.establecerconexio();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            ResultSet rs = ps.executeQuery();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            while (rs.next()) {
                int total = rs.getInt("total");
                if (total > 0) {
                    PieChart.Data data = new PieChart.Data(traducirEstado(rs.getString("estado")), total);
                    pieData.add(data);
                }
            }
            if (!pieData.isEmpty()) {
                pieChart.setData(pieData);
            }
        } catch (SQLException e) {
            mostrarError("Error cargando gráfico de estados: " + e.getMessage());
        }
    }

    private String traducirEstado(String estado) {
        switch (estado) {
            case "FACTURADA": return "Facturadas";
            case "CANCELADA": return "Canceladas";
            case "PENDIENTE": return "Pendientes";
            default: return estado;
        }
    }

    private HBox crearRankingItem(int posicion, String nombre, double valor, double maxValor, String barColor) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblPos = new Label(posicion + ".");
        lblPos.setStyle("-fx-font-weight: bold; -fx-text-fill: #cdb08e; -fx-font-size: 12px; -fx-min-width: 22;");

        Label lblNombre = new Label(nombre);
        lblNombre.setStyle("-fx-text-fill: #535659; -fx-font-size: 12px;");

        ProgressBar pb = new ProgressBar(maxValor > 0 ? Math.min(valor / maxValor, 1.0) : 0);
        pb.setPrefWidth(60);
        pb.setPrefHeight(6);
        pb.getStyleClass().add("mini-bar");
        if (barColor != null) {
            pb.setStyle("-fx-accent: " + barColor + ";");
        }

        Label lblValor = new Label(FMT.format(valor));
        lblValor.setStyle("-fx-text-fill: #100e0a; -fx-font-size: 12px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(lblPos, lblNombre, spacer, pb, lblValor);
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
