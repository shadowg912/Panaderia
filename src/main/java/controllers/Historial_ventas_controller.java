package controllers;

import Data_base.CONEXION;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.VentaResumen;
import utils.AppNavigator;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static utils.AlertHelper.*;

public class Historial_ventas_controller implements Initializable {

    @FXML private TableView<VentaResumen> tablaVentas;
    @FXML private TableColumn<VentaResumen, Integer> colId;
    @FXML private TableColumn<VentaResumen, String> colFecha;
    @FXML private TableColumn<VentaResumen, String> colCliente;
    @FXML private TableColumn<VentaResumen, Double> colTotal;
    @FXML private TableColumn<VentaResumen, String> colEstado;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Label lblTotalRegistros;

    private CONEXION conexion = new CONEXION();
    private ObservableList<VentaResumen> listaVentas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnas();
        cargarFiltros();
        cargarDatos();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getIdOrdenVenta()));
        colFecha.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFecha()));
        colCliente.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombreCliente()));
        colTotal.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getTotal()));
        colEstado.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEstado()));
    }

    private void cargarFiltros() {
        cmbEstado.setItems(FXCollections.observableArrayList(
                "Todos", "PENDIENTE", "FACTURADA", "CANCELADA"
        ));
        cmbEstado.getSelectionModel().selectFirst();
    }

    @FXML
    public void fnFiltrar() {
        cargarDatos();
    }

    private void cargarDatos() {
        listaVentas.clear();
        StringBuilder sql = new StringBuilder(
            "SELECT ov.id_orden_venta, ov.fecha_orden, ov.estado, ov.monto_total, " +
            "c.razon_social " +
            "FROM ORDEN_VENTA ov " +
            "INNER JOIN CLIENTE c ON ov.id_cliente = c.id_cliente WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        String estado = cmbEstado.getValue();
        if (estado != null && !"Todos".equals(estado)) {
            sql.append(" AND ov.estado = ?");
            params.add(estado);
        }

        LocalDate desde = dpDesde.getValue();
        if (desde != null) {
            sql.append(" AND ov.fecha_orden >= ?");
            params.add(java.sql.Date.valueOf(desde));
        }

        LocalDate hasta = dpHasta.getValue();
        if (hasta != null) {
            sql.append(" AND ov.fecha_orden <= ?");
            params.add(java.sql.Date.valueOf(hasta));
        }

        sql.append(" ORDER BY ov.fecha_orden DESC");

        try (Connection conn = conexion.establecerconexio();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                VentaResumen v = new VentaResumen();
                v.setIdOrdenVenta(rs.getInt("id_orden_venta"));
                java.sql.Date fecha = rs.getDate("fecha_orden");
                if (fecha != null) v.setFecha(fecha.toLocalDate().toString());
                v.setNombreCliente(rs.getString("razon_social"));
                v.setTotal(rs.getDouble("monto_total"));
                v.setEstado(rs.getString("estado"));
                listaVentas.add(v);
            }
        } catch (Exception e) {
            mostrarError("Error al consultar historial: " + e.getMessage());
        }
        tablaVentas.setItems(listaVentas);
        lblTotalRegistros.setText("Total: " + listaVentas.size() + " órdenes");
    }

    @FXML
    public void fnVolverMenu() {
        AppNavigator.cargarDashboard();
    }
}
