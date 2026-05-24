package model;

import javafx.beans.property.*;

public class FacturaVenta {
    private final SimpleIntegerProperty idFactura;
    private final SimpleIntegerProperty idOrdenVenta;
    private final SimpleStringProperty numeroFactura;
    private final SimpleStringProperty fechaEmision;
    private final SimpleStringProperty estado;
    private final SimpleDoubleProperty subtotal;
    private final SimpleDoubleProperty itbis;
    private final SimpleDoubleProperty montoTotal;

    public FacturaVenta() {
        this.idFactura = new SimpleIntegerProperty(0);
        this.idOrdenVenta = new SimpleIntegerProperty(0);
        this.numeroFactura = new SimpleStringProperty("");
        this.fechaEmision = new SimpleStringProperty("");
        this.estado = new SimpleStringProperty("");
        this.subtotal = new SimpleDoubleProperty(0);
        this.itbis = new SimpleDoubleProperty(0);
        this.montoTotal = new SimpleDoubleProperty(0);
    }

    public int getIdFactura() { return idFactura.get(); }
    public void setIdFactura(int v) { idFactura.set(v); }
    public SimpleIntegerProperty idFacturaProperty() { return idFactura; }

    public int getIdOrdenVenta() { return idOrdenVenta.get(); }
    public void setIdOrdenVenta(int v) { idOrdenVenta.set(v); }
    public SimpleIntegerProperty idOrdenVentaProperty() { return idOrdenVenta; }

    public String getNumeroFactura() { return numeroFactura.get(); }
    public void setNumeroFactura(String v) { numeroFactura.set(v); }
    public SimpleStringProperty numeroFacturaProperty() { return numeroFactura; }

    public String getFechaEmision() { return fechaEmision.get(); }
    public void setFechaEmision(String v) { fechaEmision.set(v); }
    public SimpleStringProperty fechaEmisionProperty() { return fechaEmision; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String v) { estado.set(v); }
    public SimpleStringProperty estadoProperty() { return estado; }

    public double getSubtotal() { return subtotal.get(); }
    public void setSubtotal(double v) { subtotal.set(v); }
    public SimpleDoubleProperty subtotalProperty() { return subtotal; }

    public double getItbis() { return itbis.get(); }
    public void setItbis(double v) { itbis.set(v); }
    public SimpleDoubleProperty itbisProperty() { return itbis; }

    public double getMontoTotal() { return montoTotal.get(); }
    public void setMontoTotal(double v) { montoTotal.set(v); }
    public SimpleDoubleProperty montoTotalProperty() { return montoTotal; }
}
