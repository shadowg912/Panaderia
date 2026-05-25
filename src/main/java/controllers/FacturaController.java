package controllers;

import Data_base.CONEXION;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FacturaController {

    private static final String EMPRESA_NOMBRE = "Panadería El Horno de Oro";
    private static final String EMPRESA_RNC = "131-456789-0";
    private static final String EMPRESA_DIRECCION = "Calle Principal #123, Zona Colonial, Santo Domingo";
    private static final DecimalFormat FMT = new DecimalFormat("#,##0.00");

    public static void mostrarFactura(int idOrdenVenta) {
        try (Connection conn = new CONEXION().establecerconexio()) {
            InputStream logo = FacturaController.class.getResourceAsStream("/images/logo.png");
            if (logo == null) {
                mostrarError("No se encontró el logo en /images/logo.png");
                return;
            }

            String jrxmlPath = "/reports/factura.jrxml";
            InputStream jrxml = FacturaController.class.getResourceAsStream(jrxmlPath);
            if (jrxml == null) {
                mostrarError("No se encontró la plantilla de factura en " + jrxmlPath);
                return;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("logoPath", logo);
            params.put("nombreEmpresa", EMPRESA_NOMBRE);
            params.put("rncEmpresa", EMPRESA_RNC);
            params.put("direccionEmpresa", EMPRESA_DIRECCION);

            String sqlFactura = "SELECT fv.numero_factura, fv.fecha_emision, fv.estado, fv.subtotal, fv.itbis, fv.monto_total, " +
                    "ov.id_orden_venta, c.razon_social, c.rnc, " +
                    "d.calle, d.numero, s.nombre as sector, cd.nombre as ciudad, p.nombre as provincia " +
                    "FROM FACTURA_VENTA fv " +
                    "INNER JOIN ORDEN_VENTA ov ON fv.id_orden_venta = ov.id_orden_venta " +
                    "INNER JOIN CLIENTE c ON ov.id_cliente = c.id_cliente " +
                    "LEFT JOIN DIRECCION d ON c.id_direccion = d.id_direccion " +
                    "LEFT JOIN SECTOR s ON d.id_sector = s.id_sector " +
                    "LEFT JOIN CIUDAD cd ON s.id_ciudad = cd.id_ciudad " +
                    "LEFT JOIN PROVINCIA p ON cd.id_provincia = p.id_provincia " +
                    "WHERE fv.id_orden_venta = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlFactura)) {
                ps.setInt(1, idOrdenVenta);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    mostrarError("No se encontró factura para la orden #" + idOrdenVenta);
                    return;
                }
                params.put("numeroFactura", rs.getString("numero_factura") != null ? rs.getString("numero_factura") : "—");
                java.sql.Date fe = rs.getDate("fecha_emision");
                params.put("fechaEmision", fe != null ? new SimpleDateFormat("dd/MM/yyyy").format(fe) : "—");
                params.put("estado", rs.getString("estado") != null ? rs.getString("estado") : "—");
                params.put("idOrden", rs.getInt("id_orden_venta"));
                params.put("nombreCliente", rs.getString("razon_social") != null ? rs.getString("razon_social") : "—");
                params.put("rncCliente", rs.getString("rnc") != null ? rs.getString("rnc") : "—");

                StringBuilder dir = new StringBuilder();
                String calle = rs.getString("calle");
                int numero = rs.getInt("numero");
                if (calle != null) dir.append("Calle ").append(calle);
                if (numero > 0) dir.append(" #").append(numero);
                String sector = rs.getString("sector");
                if (sector != null) dir.append(", ").append(sector);
                String ciudad = rs.getString("ciudad");
                if (ciudad != null) dir.append(", ").append(ciudad);
                String provincia = rs.getString("provincia");
                if (provincia != null) dir.append(", ").append(provincia);
                params.put("direccionCliente", dir.length() > 0 ? dir.toString() : "—");

                double sub = rs.getBigDecimal("subtotal") != null ? rs.getBigDecimal("subtotal").doubleValue() : 0.0;
                double itb = rs.getBigDecimal("itbis") != null ? rs.getBigDecimal("itbis").doubleValue() : 0.0;
                double tot = rs.getBigDecimal("monto_total") != null ? rs.getBigDecimal("monto_total").doubleValue() : 0.0;
                params.put("subtotalStr", "RD$ " + FMT.format(sub));
                params.put("itbisStr",    "RD$ " + FMT.format(itb));
                params.put("totalStr",    "RD$ " + FMT.format(tot));
            }

            String sqlDetalle = "SELECT p.nombre as producto, dov.cantidad, dov.precio_unitario, dov.subtotal " +
                    "FROM DETALLE_ORDEN_VENTA dov " +
                    "INNER JOIN PRODUCTO p ON dov.id_producto = p.id_producto " +
                    "WHERE dov.id_orden_venta = ? ORDER BY p.nombre";
            List<Map<String, Object>> detalles = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sqlDetalle)) {
                ps.setInt(1, idOrdenVenta);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("nombreProducto", rs.getString("producto"));
                    double cant = rs.getBigDecimal("cantidad") != null ? rs.getBigDecimal("cantidad").doubleValue() : 0;
                    double pu = rs.getDouble("precio_unitario");
                    double st = rs.getDouble("subtotal");
                    row.put("cantidadStr", cant == (int) cant ? String.valueOf((int) cant) : String.valueOf(cant));
                    row.put("precioStr", "RD$ " + FMT.format(pu));
                    row.put("subtotalStr", "RD$ " + FMT.format(st));
                    detalles.add(row);
                }
            }

            if (detalles.isEmpty()) {
                mostrarError("La orden #" + idOrdenVenta + " no tiene productos asociados.");
                return;
            }

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(detalles);

            byte[] jrxmlBytes = jrxml.readAllBytes();
            JasperReport jasperReport = JasperCompileManager.compileReport(new ByteArrayInputStream(jrxmlBytes));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

            JasperViewer.viewReport(jasperPrint, false);

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null && e.getCause() != null) {
                msg = e.getCause().getMessage();
            }
            mostrarError("Error al generar factura: " + (msg != null ? msg : e.toString()));
        }
    }

    private static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alert.setTitle("Facturación");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
