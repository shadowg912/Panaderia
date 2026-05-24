package model;

import javafx.beans.property.*;

public class VentaResumen {
    private final SimpleIntegerProperty idOrdenVenta;
    private final SimpleStringProperty fecha;
    private final SimpleStringProperty nombreCliente;
    private final SimpleDoubleProperty total;
    private final SimpleStringProperty estado;

    public VentaResumen() {
        this.idOrdenVenta = new SimpleIntegerProperty(0);
        this.fecha = new SimpleStringProperty("");
        this.nombreCliente = new SimpleStringProperty("");
        this.total = new SimpleDoubleProperty(0);
        this.estado = new SimpleStringProperty("");
    }

    public int getIdOrdenVenta() { return idOrdenVenta.get(); }
    public void setIdOrdenVenta(int v) { idOrdenVenta.set(v); }
    public SimpleIntegerProperty idOrdenVentaProperty() { return idOrdenVenta; }

    public String getFecha() { return fecha.get(); }
    public void setFecha(String v) { fecha.set(v); }
    public SimpleStringProperty fechaProperty() { return fecha; }

    public String getNombreCliente() { return nombreCliente.get(); }
    public void setNombreCliente(String v) { nombreCliente.set(v); }
    public SimpleStringProperty nombreClienteProperty() { return nombreCliente; }

    public double getTotal() { return total.get(); }
    public void setTotal(double v) { total.set(v); }
    public SimpleDoubleProperty totalProperty() { return total; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String v) { estado.set(v); }
    public SimpleStringProperty estadoProperty() { return estado; }
}
