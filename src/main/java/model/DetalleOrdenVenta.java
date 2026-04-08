package model;

import java.math.BigDecimal;

public class DetalleOrdenVenta {
    private int idOrdenVenta;
    private int idProducto;
    private BigDecimal cantidad;
    private double precioUnitario;
    private double subtotal;

    public DetalleOrdenVenta() {}

    private String nombreProducto;

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public DetalleOrdenVenta(int idOrdenVenta, int idProducto, BigDecimal cantidad,
                             double precioUnitario, double subtotal) {
        this.idOrdenVenta = idOrdenVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public int getIdOrdenVenta() { return idOrdenVenta; }
    public void setIdOrdenVenta(int idOrdenVenta) { this.idOrdenVenta = idOrdenVenta; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}