package controllers;

import Data_base.CONEXION;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Cliente;
import utils.AppNavigator;

import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

import static utils.AlertHelper.*;

public class Facturas_pagadas_controller {

    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private TableView<FacturaPagada> tblFacturas;
    @FXML private TableColumn<FacturaPagada, String> colNumFactura;
    @FXML private TableColumn<FacturaPagada, String> colCliente;
    @FXML private TableColumn<FacturaPagada, String> colFecha;
    @FXML private TableColumn<FacturaPagada, Double> colTotal;
    @FXML private TableColumn<FacturaPagada, Double> colPagado;
    @FXML private TableColumn<FacturaPagada, Void> colAccion;
    @FXML private TableView<PagoRow> tblPagos;
    @FXML private TableColumn<PagoRow, Integer> colPagoId;
    @FXML private TableColumn<PagoRow, Double> colPagoMonto;
    @FXML private TableColumn<PagoRow, String> colPagoFecha;
    @FXML private TableColumn<PagoRow, String> colPagoForma;
    @FXML private Label lblTotalRegistros;

    private final CONEXION conexion = new CONEXION();
    private static final NumberFormat FMT = NumberFormat.getCurrencyInstance(new Locale("es", "DO"));
    private final ObservableList<FacturaPagada> listaFacturas = FXCollections.observableArrayList();
    private final ObservableList<PagoRow> listaPagos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarClientes();
        cargarFacturas();
    }

    private void configurarColumnas() {
        colNumFactura.setCellValueFactory(cd -> cd.getValue().numeroFacturaProperty());
        colCliente.setCellValueFactory(cd -> cd.getValue().clienteProperty());
        colFecha.setCellValueFactory(cd -> cd.getValue().fechaProperty());
        colTotal.setCellValueFactory(cd -> cd.getValue().montoTotalProperty().asObject());
        colPagado.setCellValueFactory(cd -> cd.getValue().totalPagadoProperty().asObject());

        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : FMT.format(val));
                setStyle("-fx-alignment: CENTER_RIGHT;");
            }
        });
        colPagado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : FMT.format(val));
                setStyle("-fx-alignment: CENTER_RIGHT;");
            }
        });

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("Factura");
            {
                btnVer.setStyle("-fx-background-color: #cdb08e; -fx-text-fill: #100e0a; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 6 12;");
                btnVer.setOnAction(e -> {
                    FacturaPagada f = getTableView().getItems().get(getIndex());
                    FacturaController.mostrarFactura(f.getIdOrdenVenta());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnVer);
            }
        });

        colPagoId.setCellValueFactory(cd -> cd.getValue().idPagoProperty().asObject());
        colPagoMonto.setCellValueFactory(cd -> cd.getValue().montoProperty().asObject());
        colPagoFecha.setCellValueFactory(cd -> cd.getValue().fechaProperty());
        colPagoForma.setCellValueFactory(cd -> cd.getValue().formaPagoProperty());

        colPagoMonto.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : FMT.format(val));
                setStyle("-fx-alignment: CENTER_RIGHT;");
            }
        });

        tblFacturas.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarPagos(sel.getIdOrdenVenta());
        });
    }

    private void cargarClientes() {
        ObservableList<Cliente> items = FXCollections.observableArrayList();
        items.add(new Cliente(0, "Todos los clientes"));
        String sql = "SELECT id_cliente, razon_social FROM CLIENTE WHERE estado = 1 ORDER BY razon_social";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new Cliente(rs.getInt("id_cliente"), rs.getString("razon_social")));
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar clientes: " + e.getMessage());
        }
        cmbCliente.setItems(items);
        cmbCliente.getSelectionModel().selectFirst();
    }

    @FXML
    public void fnClienteSeleccionado() {
        cargarFacturas();
    }

    private void cargarFacturas() {
        listaFacturas.clear();
        listaPagos.clear();
        Cliente filtro = cmbCliente.getValue();

        StringBuilder sql = new StringBuilder(
            "SELECT fv.id_factura_venta, fv.numero_factura, fv.id_orden_venta, " +
            "fv.monto_total, fv.fecha_emision, c.razon_social, " +
            "COALESCE(pg.total_pagado, 0) as total_pagado " +
            "FROM FACTURA_VENTA fv " +
            "INNER JOIN CLIENTE c ON fv.id_empresa_cliente = c.id_cliente " +
            "LEFT JOIN (SELECT id_orden_venta, SUM(monto) as total_pagado FROM PAGO GROUP BY id_orden_venta) pg " +
            "ON fv.id_orden_venta = pg.id_orden_venta " +
            "WHERE fv.estado = 'PAGADA'"
        );

        if (filtro != null && filtro.getIdCliente() > 0) {
            sql.append(" AND fv.id_empresa_cliente = ?");
        }
        sql.append(" ORDER BY fv.fecha_emision DESC");

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (filtro != null && filtro.getIdCliente() > 0) {
                ps.setInt(1, filtro.getIdCliente());
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FacturaPagada f = new FacturaPagada();
                f.setIdFactura(rs.getInt("id_factura_venta"));
                f.setNumeroFactura(rs.getString("numero_factura"));
                f.setIdOrdenVenta(rs.getInt("id_orden_venta"));
                f.setCliente(rs.getString("razon_social"));
                f.setMontoTotal(rs.getDouble("monto_total"));
                f.setTotalPagado(rs.getDouble("total_pagado"));
                Date fecha = rs.getDate("fecha_emision");
                f.setFecha(fecha != null ? fecha.toLocalDate().toString() : "");
                listaFacturas.add(f);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar facturas pagadas: " + e.getMessage());
        }
        tblFacturas.setItems(listaFacturas);
        lblTotalRegistros.setText("Total: " + listaFacturas.size() + " facturas pagadas");
    }

    private void cargarPagos(int idOrdenVenta) {
        listaPagos.clear();
        String sql = "SELECT p.id_pago, p.monto, p.fecha, fp.nombre as forma_pago " +
                     "FROM PAGO p " +
                     "INNER JOIN FORMA_PAGO fp ON p.id_forma_pago = fp.id_forma_pago " +
                     "WHERE p.id_orden_venta = ? ORDER BY p.fecha DESC";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOrdenVenta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PagoRow p = new PagoRow();
                p.setIdPago(rs.getInt("id_pago"));
                p.setMonto(rs.getDouble("monto"));
                Timestamp ts = rs.getTimestamp("fecha");
                p.setFecha(ts != null ? ts.toLocalDateTime().toLocalDate().toString() : "");
                p.setFormaPago(rs.getString("forma_pago"));
                listaPagos.add(p);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar pagos: " + e.getMessage());
        }
        tblPagos.setItems(listaPagos);
    }

    @FXML
    public void fnVolverMenu() {
        AppNavigator.cargarDashboard();
    }

    public static class FacturaPagada {
        private final SimpleIntegerProperty idFactura = new SimpleIntegerProperty();
        private final SimpleIntegerProperty idOrdenVenta = new SimpleIntegerProperty();
        private final SimpleStringProperty numeroFactura = new SimpleStringProperty();
        private final SimpleStringProperty cliente = new SimpleStringProperty();
        private final SimpleStringProperty fecha = new SimpleStringProperty();
        private final SimpleDoubleProperty montoTotal = new SimpleDoubleProperty();
        private final SimpleDoubleProperty totalPagado = new SimpleDoubleProperty();

        public int getIdFactura() { return idFactura.get(); }
        public void setIdFactura(int v) { idFactura.set(v); }
        public SimpleIntegerProperty idFacturaProperty() { return idFactura; }
        public int getIdOrdenVenta() { return idOrdenVenta.get(); }
        public void setIdOrdenVenta(int v) { idOrdenVenta.set(v); }
        public SimpleIntegerProperty idOrdenVentaProperty() { return idOrdenVenta; }
        public String getNumeroFactura() { return numeroFactura.get(); }
        public void setNumeroFactura(String v) { numeroFactura.set(v); }
        public SimpleStringProperty numeroFacturaProperty() { return numeroFactura; }
        public String getCliente() { return cliente.get(); }
        public void setCliente(String v) { cliente.set(v); }
        public SimpleStringProperty clienteProperty() { return cliente; }
        public String getFecha() { return fecha.get(); }
        public void setFecha(String v) { fecha.set(v); }
        public SimpleStringProperty fechaProperty() { return fecha; }
        public double getMontoTotal() { return montoTotal.get(); }
        public void setMontoTotal(double v) { montoTotal.set(v); }
        public SimpleDoubleProperty montoTotalProperty() { return montoTotal; }
        public double getTotalPagado() { return totalPagado.get(); }
        public void setTotalPagado(double v) { totalPagado.set(v); }
        public SimpleDoubleProperty totalPagadoProperty() { return totalPagado; }
    }

    public static class PagoRow {
        private final SimpleIntegerProperty idPago = new SimpleIntegerProperty();
        private final SimpleDoubleProperty monto = new SimpleDoubleProperty();
        private final SimpleStringProperty fecha = new SimpleStringProperty();
        private final SimpleStringProperty formaPago = new SimpleStringProperty();

        public int getIdPago() { return idPago.get(); }
        public void setIdPago(int v) { idPago.set(v); }
        public SimpleIntegerProperty idPagoProperty() { return idPago; }
        public double getMonto() { return monto.get(); }
        public void setMonto(double v) { monto.set(v); }
        public SimpleDoubleProperty montoProperty() { return monto; }
        public String getFecha() { return fecha.get(); }
        public void setFecha(String v) { fecha.set(v); }
        public SimpleStringProperty fechaProperty() { return fecha; }
        public String getFormaPago() { return formaPago.get(); }
        public void setFormaPago(String v) { formaPago.set(v); }
        public SimpleStringProperty formaPagoProperty() { return formaPago; }
    }
}
