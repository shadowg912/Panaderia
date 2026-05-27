package model;

import javafx.beans.property.*;

public class Pago {

    private final StringProperty  numero       = new SimpleStringProperty();
    private final StringProperty  fecha        = new SimpleStringProperty();
    private final DoubleProperty  total        = new SimpleDoubleProperty();
    private final DoubleProperty  saldo        = new SimpleDoubleProperty();
    private final StringProperty  estado       = new SimpleStringProperty();
    private final BooleanProperty seleccionada = new SimpleBooleanProperty(false);

    // ── Constructor ───────────────────────────────────────────────────────────
    public Pago(String numero, String fecha, double total, double saldo, String estado) {
        this.numero.set(numero);
        this.fecha.set(fecha);
        this.total.set(total);
        this.saldo.set(saldo);
        this.estado.set(estado);
    }

    // ── Properties (para TableView y bindings) ────────────────────────────────
    public StringProperty  numeroProperty()       { return numero; }
    public StringProperty  fechaProperty()        { return fecha; }
    public DoubleProperty  totalProperty()        { return total; }
    public DoubleProperty  saldoProperty()        { return saldo; }
    public StringProperty  estadoProperty()       { return estado; }
    public BooleanProperty seleccionadaProperty() { return seleccionada; }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String  getNumero()       { return numero.get(); }
    public String  getFecha()        { return fecha.get(); }
    public double  getTotal()        { return total.get(); }
    public double  getSaldo()        { return saldo.get(); }
    public String  getEstado()       { return estado.get(); }
    public boolean isSeleccionada()  { return seleccionada.get(); }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setNumero(String v)      { numero.set(v); }
    public void setFecha(String v)       { fecha.set(v); }
    public void setTotal(double v)       { total.set(v); }
    public void setSaldo(double v)       { saldo.set(v); }
    public void setEstado(String v)      { estado.set(v); }
    public void setSeleccionada(boolean v) { seleccionada.set(v); }

    @Override
    public String toString() {
        return "Pago{" + numero.get() + ", saldo=" + saldo.get() + ", estado=" + estado.get() + "}";
    }
}