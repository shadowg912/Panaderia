package model;

import java.math.BigDecimal;
import java.sql.Date;

public class OrdenVenta {
    private int idOrdenVenta;
    private int idCliente;
    private String estado;
    private Date fechaOrden;
    private Integer idFormaPago;
    private Double subtotal;
    private Double itbis;
    private Double montoTotal;
    private Date fechaEntrega;
    private String motivoCancelacion;

    public OrdenVenta() {}

    public OrdenVenta(int idOrdenVenta, int idCliente, String estado, Date fechaOrden,
                      Integer idFormaPago, Double subtotal, Double itbis, Double montoTotal,
                      Date fechaEntrega, String motivoCancelacion) {
        this.idOrdenVenta = idOrdenVenta;
        this.idCliente = idCliente;
        this.estado = estado;
        this.fechaOrden = fechaOrden;
        this.idFormaPago = idFormaPago;
        this.subtotal = subtotal;
        this.itbis = itbis;
        this.montoTotal = montoTotal;
        this.fechaEntrega = fechaEntrega;
        this.motivoCancelacion = motivoCancelacion;
    }

    public int getIdOrdenVenta() { return idOrdenVenta; }
    public void setIdOrdenVenta(int idOrdenVenta) { this.idOrdenVenta = idOrdenVenta; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getFechaOrden() { return fechaOrden; }
    public void setFechaOrden(Date fechaOrden) { this.fechaOrden = fechaOrden; }

    public Integer getIdFormaPago() { return idFormaPago; }
    public void setIdFormaPago(Integer idFormaPago) { this.idFormaPago = idFormaPago; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getItbis() { return itbis; }
    public void setItbis(Double itbis) { this.itbis = itbis; }

    public Double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(Double montoTotal) { this.montoTotal = montoTotal; }

    public Date getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(Date fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
}