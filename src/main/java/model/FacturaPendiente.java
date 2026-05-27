package model;

import javafx.beans.property.*;

public class FacturaPendiente {

    private final SimpleIntegerProperty idFactura;
    private final SimpleIntegerProperty idOrdenVenta;
    private final SimpleStringProperty numeroFactura;
    private final SimpleStringProperty fecha;
    private final SimpleDoubleProperty montoTotal;
    private final SimpleDoubleProperty montoPagado;
    private final SimpleDoubleProperty saldoPendiente;
    private final BooleanProperty seleccionada;

    public FacturaPendiente() {
        this.idFactura = new SimpleIntegerProperty(0);
        this.idOrdenVenta = new SimpleIntegerProperty(0);
        this.numeroFactura = new SimpleStringProperty("");
        this.fecha = new SimpleStringProperty("");
        this.montoTotal = new SimpleDoubleProperty(0);
        this.montoPagado = new SimpleDoubleProperty(0);
        this.saldoPendiente = new SimpleDoubleProperty(0);
        this.seleccionada = new SimpleBooleanProperty(false);
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

    public String getFecha() { return fecha.get(); }
    public void setFecha(String v) { fecha.set(v); }
    public SimpleStringProperty fechaProperty() { return fecha; }

    public double getMontoTotal() { return montoTotal.get(); }
    public void setMontoTotal(double v) { montoTotal.set(v); }
    public SimpleDoubleProperty montoTotalProperty() { return montoTotal; }

    public double getMontoPagado() { return montoPagado.get(); }
    public void setMontoPagado(double v) { montoPagado.set(v); }
    public SimpleDoubleProperty montoPagadoProperty() { return montoPagado; }

    public double getSaldoPendiente() { return saldoPendiente.get(); }
    public void setSaldoPendiente(double v) { saldoPendiente.set(v); }
    public SimpleDoubleProperty saldoPendienteProperty() { return saldoPendiente; }

    public boolean isSeleccionada() { return seleccionada.get(); }
    public void setSeleccionada(boolean v) { seleccionada.set(v); }
    public BooleanProperty seleccionadaProperty() { return seleccionada; }
}
