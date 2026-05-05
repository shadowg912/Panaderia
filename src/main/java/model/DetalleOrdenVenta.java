package model;

import javafx.beans.property.*;
import java.math.BigDecimal;

public class DetalleOrdenVenta {
    private final IntegerProperty idOrdenVenta = new SimpleIntegerProperty();
    private final IntegerProperty idProducto = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> cantidad = new SimpleObjectProperty<>();
    private final DoubleProperty precioUnitario = new SimpleDoubleProperty();
    private final DoubleProperty subtotal = new SimpleDoubleProperty();
    private final StringProperty nombreProducto = new SimpleStringProperty();

    public DetalleOrdenVenta() {}

    public DetalleOrdenVenta(int idOrdenVenta, int idProducto, BigDecimal cantidad,
                             double precioUnitario, double subtotal) {
        this.idOrdenVenta.set(idOrdenVenta);
        this.idProducto.set(idProducto);
        this.cantidad.set(cantidad);
        this.precioUnitario.set(precioUnitario);
        this.subtotal.set(subtotal);
    }

    // ── idOrdenVenta ──
    public int getIdOrdenVenta() { return idOrdenVenta.get(); }
    public void setIdOrdenVenta(int value) { idOrdenVenta.set(value); }
    public IntegerProperty idOrdenVentaProperty() { return idOrdenVenta; }

    // ── idProducto ──
    public int getIdProducto() { return idProducto.get(); }
    public void setIdProducto(int value) { idProducto.set(value); }
    public IntegerProperty idProductoProperty() { return idProducto; }

    // ── cantidad ──
    public BigDecimal getCantidad() { return cantidad.get(); }
    public void setCantidad(BigDecimal value) { cantidad.set(value); }
    public ObjectProperty<BigDecimal> cantidadProperty() { return cantidad; }

    // ── precioUnitario ──
    public double getPrecioUnitario() { return precioUnitario.get(); }
    public void setPrecioUnitario(double value) { precioUnitario.set(value); }
    public DoubleProperty precioUnitarioProperty() { return precioUnitario; }

    // ── subtotal ──
    public double getSubtotal() { return subtotal.get(); }
    public void setSubtotal(double value) { subtotal.set(value); }
    public DoubleProperty subtotalProperty() { return subtotal; }

    // ── nombreProducto ──
    public String getNombreProducto() { return nombreProducto.get(); }
    public void setNombreProducto(String value) { nombreProducto.set(value); }
    public StringProperty nombreProductoProperty() { return nombreProducto; }
}