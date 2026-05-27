package controllers;

import Data_base.CONEXION;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Cliente;
import model.FacturaPendiente;
import utils.AppNavigator;

import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static utils.AlertHelper.*;

public class Ventana_pagos_controller {

    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private TableView<FacturaPendiente> tblFacturas;
    @FXML private TableColumn<FacturaPendiente, Boolean> colSeleccion;
    @FXML private TableColumn<FacturaPendiente, String> colNumFactura;
    @FXML private TableColumn<FacturaPendiente, String> colFecha;
    @FXML private TableColumn<FacturaPendiente, Double> colTotal;
    @FXML private TableColumn<FacturaPendiente, Double> colPagado;
    @FXML private TableColumn<FacturaPendiente, Double> colSaldo;
    @FXML private Label lblSeleccionadas;
    @FXML private Label lblTotalPendiente;
    @FXML private Button btnPagar;

    private final CONEXION conexion = new CONEXION();
    private static final NumberFormat FMT = NumberFormat.getCurrencyInstance(new Locale("es", "DO"));
    private final ObservableList<FacturaPendiente> listaFacturas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarClientes();
        tblFacturas.setItems(listaFacturas);
    }

    private void configurarColumnas() {
        colSeleccion.setCellValueFactory(cd -> cd.getValue().seleccionadaProperty());
        colSeleccion.setCellFactory(col -> new TableCell<>() {
            private final CheckBox check = new CheckBox();
            {
                check.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < getTableView().getItems().size()) {
                        FacturaPendiente f = getTableView().getItems().get(idx);
                        f.setSeleccionada(check.isSelected());
                        actualizarResumen();
                    }
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    check.setSelected(getTableView().getItems().get(getIndex()).isSeleccionada());
                    setGraphic(check);
                }
            }
        });

        colNumFactura.setCellValueFactory(cd -> cd.getValue().numeroFacturaProperty());
        colFecha.setCellValueFactory(cd -> cd.getValue().fechaProperty());
        colTotal.setCellValueFactory(cd -> cd.getValue().montoTotalProperty().asObject());
        colPagado.setCellValueFactory(cd -> cd.getValue().montoPagadoProperty().asObject());
        colSaldo.setCellValueFactory(cd -> cd.getValue().saldoPendienteProperty().asObject());

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
        colSaldo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : FMT.format(val));
                setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");
            }
        });
    }

    private void cargarClientes() {
        ObservableList<Cliente> items = FXCollections.observableArrayList();
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
    }

    @FXML
    public void fnClienteSeleccionado() {
        cargarFacturasPendientes();
    }

    private void cargarFacturasPendientes() {
        listaFacturas.clear();
        actualizarResumen();
        Cliente cliente = cmbCliente.getValue();
        if (cliente == null) return;

        String sql = "SELECT fv.id_factura_venta, fv.numero_factura, fv.id_orden_venta, " +
                     "fv.monto_total, fv.fecha_emision, " +
                     "COALESCE(pg.total_pagado, 0) as monto_pagado " +
                     "FROM FACTURA_VENTA fv " +
                     "LEFT JOIN (SELECT id_orden_venta, SUM(monto) as total_pagado FROM PAGO GROUP BY id_orden_venta) pg " +
                     "ON fv.id_orden_venta = pg.id_orden_venta " +
                     "WHERE fv.id_empresa_cliente = ? AND fv.estado = 'EMITIDA' " +
                     "AND fv.monto_total - COALESCE(pg.total_pagado, 0) > 0 " +
                     "ORDER BY fv.fecha_emision ASC";

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cliente.getIdCliente());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FacturaPendiente f = new FacturaPendiente();
                f.setIdFactura(rs.getInt("id_factura_venta"));
                f.setNumeroFactura(rs.getString("numero_factura"));
                f.setIdOrdenVenta(rs.getInt("id_orden_venta"));
                f.setMontoTotal(rs.getDouble("monto_total"));
                f.setMontoPagado(rs.getDouble("monto_pagado"));
                f.setSaldoPendiente(rs.getDouble("monto_total") - rs.getDouble("monto_pagado"));
                Date fecha = rs.getDate("fecha_emision");
                f.setFecha(fecha != null ? fecha.toLocalDate().toString() : "");
                listaFacturas.add(f);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar facturas pendientes: " + e.getMessage());
        }
    }

    private void actualizarResumen() {
        int count = 0;
        double total = 0;
        for (FacturaPendiente f : listaFacturas) {
            if (f.isSeleccionada()) {
                count++;
                total += f.getSaldoPendiente();
            }
        }
        lblSeleccionadas.setText(count + " factura(s) seleccionada(s)");
        lblTotalPendiente.setText("Total pendiente: " + FMT.format(total));
        btnPagar.setDisable(count == 0);
    }

    @FXML
    public void fnPagar() {
        List<FacturaPendiente> seleccionadas = new ArrayList<>();
        for (FacturaPendiente f : listaFacturas) {
            if (f.isSeleccionada()) seleccionadas.add(f);
        }
        if (seleccionadas.isEmpty()) {
            mostrarAdvertencia("Seleccione al menos una factura para pagar.");
            return;
        }
        mostrarDialogoPago(seleccionadas);
    }

    private void mostrarDialogoPago(List<FacturaPendiente> seleccionadas) {
        double totalPendiente = seleccionadas.stream().mapToDouble(FacturaPendiente::getSaldoPendiente).sum();

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(btnPagar.getScene().getWindow());
        dialog.setTitle("Registrar Pago");

        VBox root = new VBox(20);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #f5f2eb;");

        Label lblTitulo = new Label("Registrar Pago");
        lblTitulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #100e0a;");

        Label lblFormaPago = new Label("Forma de Pago");
        lblFormaPago.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #bfb6a6;");
        Label lblEfectivo = new Label("Efectivo");
        lblEfectivo.setStyle("-fx-font-size: 15px; -fx-text-fill: #535659; -fx-padding: 0 0 8 0;");

        StringBuilder facturasList = new StringBuilder();
        for (FacturaPendiente f : seleccionadas) {
            if (facturasList.length() > 0) facturasList.append("\n");
            facturasList.append("  • ").append(f.getNumeroFactura())
                        .append(" — Saldo: ").append(FMT.format(f.getSaldoPendiente()));
        }

        Label lblFacturasTitle = new Label("FACTURAS A PAGAR (" + seleccionadas.size() + ")");
        lblFacturasTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #bfb6a6;");
        Label lblFacturasLista = new Label(facturasList.toString());
        lblFacturasLista.setStyle("-fx-font-size: 14px; -fx-text-fill: #535659; -fx-line-spacing: 4;");

        Label lblTotalLabel = new Label("Total pendiente: " + FMT.format(totalPendiente));
        lblTotalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #100e0a;");

        Label lblMontoLabel = new Label("MONTO A PAGAR");
        lblMontoLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #bfb6a6;");
        TextField txtMonto = new TextField();
        txtMonto.setPromptText("0.00");
        txtMonto.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-pref-height: 48;");

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");

        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-pref-height: 44; -fx-padding: 0 28; -fx-font-size: 14px; -fx-background-color: #ffffff; -fx-text-fill: #535659; -fx-border-color: #c2baab; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> dialog.close());

        Button btnConfirmar = new Button("Confirmar Pago");
        btnConfirmar.setStyle("-fx-pref-height: 44; -fx-padding: 0 28; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #cdb08e; -fx-text-fill: #100e0a; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        btnConfirmar.setOnAction(e -> {
            String montoStr = txtMonto.getText().trim();
            if (montoStr.isEmpty()) {
                lblError.setText("Ingrese un monto a pagar.");
                return;
            }
            double monto;
            try {
                monto = Double.parseDouble(montoStr.replace(",", ""));
            } catch (NumberFormatException ex) {
                lblError.setText("Monto invalido. Use numeros y punto decimal.");
                return;
            }
            if (monto <= 0) {
                lblError.setText("El monto debe ser mayor que cero.");
                return;
            }
            if (monto > totalPendiente) {
                lblError.setText("El monto no puede exceder el total pendiente de " + FMT.format(totalPendiente));
                return;
            }
            lblError.setText("");
            dialog.close();
            procesarPago(seleccionadas, monto, totalPendiente);
        });

        botones.getChildren().addAll(btnCancelar, btnConfirmar);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #c2baab;");
        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #c2baab;");

        root.getChildren().addAll(
            lblTitulo, sep1,
            lblFormaPago, lblEfectivo,
            lblFacturasTitle, lblFacturasLista,
            lblTotalLabel, sep2,
            lblMontoLabel, txtMonto,
            lblError, botones
        );

        Scene scene = new Scene(root, 480, 520);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void procesarPago(List<FacturaPendiente> seleccionadas, double montoPagar, double totalPendiente) {
        int idFormaPago = obtenerIdFormaPagoEfectivo();
        if (idFormaPago <= 0) {
            mostrarError("No se encontro la forma de pago 'Efectivo' en el sistema.");
            return;
        }

        double restante = montoPagar;
        StringBuilder resultado = new StringBuilder();
        double totalPagado = 0;

        try (Connection conn = conexion.establecerconexio()) {
            conn.setAutoCommit(false);

            for (FacturaPendiente f : seleccionadas) {
                if (restante <= 0) break;

                double saldo = f.getSaldoPendiente();
                double abono = Math.min(restante, saldo);
                if (abono <= 0) continue;

                String sqlPago = "INSERT INTO PAGO (id_orden_venta, id_forma_pago, monto, fecha) VALUES (?, ?, ?, GETDATE())";
                try (PreparedStatement ps = conn.prepareStatement(sqlPago)) {
                    ps.setInt(1, f.getIdOrdenVenta());
                    ps.setInt(2, idFormaPago);
                    ps.setDouble(3, abono);
                    ps.executeUpdate();
                }

                double nuevoPagado = f.getMontoPagado() + abono;
                totalPagado += abono;
                restante -= abono;

                boolean pagoCompleto = Math.abs(nuevoPagado - f.getMontoTotal()) < 0.01;
                if (pagoCompleto) {
                    String sqlFac = "UPDATE FACTURA_VENTA SET estado = 'PAGADA' WHERE id_factura_venta = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlFac)) {
                        ps.setInt(1, f.getIdFactura());
                        ps.executeUpdate();
                    }
                }

                resultado.append("  • ").append(f.getNumeroFactura())
                         .append(" — Abonado: ").append(FMT.format(abono));
                if (pagoCompleto) {
                    resultado.append(" (PAGADA)");
                } else {
                    double nuevoSaldo = f.getMontoTotal() - nuevoPagado;
                    resultado.append(" — Saldo restante: ").append(FMT.format(nuevoSaldo));
                }
                resultado.append("\n");
            }

            conn.commit();

            mostrarResultadoPago(montoPagar, totalPagado, resultado.toString());
            cargarFacturasPendientes();

        } catch (SQLException e) {
            mostrarError("Error al procesar el pago: " + e.getMessage());
        }
    }

    private int obtenerIdFormaPagoEfectivo() {
        String sql = "SELECT id_forma_pago FROM FORMA_PAGO WHERE nombre = 'Efectivo' AND estado = 1";
        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("id_forma_pago");
        } catch (SQLException e) {
            mostrarError("Error al consultar forma de pago: " + e.getMessage());
        }
        return -1;
    }

    private void mostrarResultadoPago(double montoSolicitado, double montoPagado, String detalle) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pago Registrado");
        alert.setHeaderText(null);

        StringBuilder contenido = new StringBuilder();
        contenido.append("Pago registrado exitosamente.\n\n");
        contenido.append("Monto solicitado: ").append(FMT.format(montoSolicitado)).append("\n");
        contenido.append("Total pagado: ").append(FMT.format(montoPagado)).append("\n\n");
        contenido.append("Detalle por factura:\n");
        contenido.append(detalle);

        alert.setContentText(contenido.toString());
        alert.showAndWait();
    }

    @FXML
    public void fnVolverMenu() {
        AppNavigator.cargarDashboard();
    }
}
