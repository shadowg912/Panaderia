package model;

import javafx.beans.property.*;
import java.math.BigDecimal;

public class DetalleOrdenProduccion {
    private final IntegerProperty idDetalle = new SimpleIntegerProperty();
    private final IntegerProperty idOrdenProduccion = new SimpleIntegerProperty();
    private final IntegerProperty idProducto = new SimpleIntegerProperty();
    private final StringProperty nombreProducto = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> cantidadPlanificada = new SimpleObjectProperty<>();

    public DetalleOrdenProduccion() {}

    public DetalleOrdenProduccion(int idProducto, String nombreProducto, BigDecimal cantidadPlanificada) {
        this.idProducto.set(idProducto);
        this.nombreProducto.set(nombreProducto);
        this.cantidadPlanificada.set(cantidadPlanificada);
    }

    public int getIdDetalle() { return idDetalle.get(); }
    public void setIdDetalle(int v) { idDetalle.set(v); }
    public IntegerProperty idDetalleProperty() { return idDetalle; }

    public int getIdOrdenProduccion() { return idOrdenProduccion.get(); }
    public void setIdOrdenProduccion(int v) { idOrdenProduccion.set(v); }
    public IntegerProperty idOrdenProduccionProperty() { return idOrdenProduccion; }

    public int getIdProducto() { return idProducto.get(); }
    public void setIdProducto(int v) { idProducto.set(v); }
    public IntegerProperty idProductoProperty() { return idProducto; }

    public String getNombreProducto() { return nombreProducto.get(); }
    public void setNombreProducto(String v) { nombreProducto.set(v); }
    public StringProperty nombreProductoProperty() { return nombreProducto; }

    public BigDecimal getCantidadPlanificada() { return cantidadPlanificada.get(); }
    public void setCantidadPlanificada(BigDecimal v) { cantidadPlanificada.set(v); }
    public ObjectProperty<BigDecimal> cantidadProducidaProperty() { return cantidadPlanificada; }
}
