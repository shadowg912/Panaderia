package model;

import javafx.beans.property.*;
import java.math.BigDecimal;

public class DetalleProduccion {
    private final IntegerProperty idProduccion = new SimpleIntegerProperty();
    private final IntegerProperty idProducto = new SimpleIntegerProperty();
    private final StringProperty nombreProducto = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> cantidadProducida = new SimpleObjectProperty<>();

    public DetalleProduccion() {}

    public DetalleProduccion(int idProduccion, int idProducto, String nombreProducto, BigDecimal cantidadProducida) {
        this.idProduccion.set(idProduccion);
        this.idProducto.set(idProducto);
        this.nombreProducto.set(nombreProducto);
        this.cantidadProducida.set(cantidadProducida);
    }

    public int getIdProduccion() { return idProduccion.get(); }
    public void setIdProduccion(int value) { idProduccion.set(value); }
    public IntegerProperty idProduccionProperty() { return idProduccion; }

    public int getIdProducto() { return idProducto.get(); }
    public void setIdProducto(int value) { idProducto.set(value); }
    public IntegerProperty idProductoProperty() { return idProducto; }

    public String getNombreProducto() { return nombreProducto.get(); }
    public void setNombreProducto(String value) { nombreProducto.set(value); }
    public StringProperty nombreProductoProperty() { return nombreProducto; }

    public BigDecimal getCantidadProducida() { return cantidadProducida.get(); }
    public void setCantidadProducida(BigDecimal value) { cantidadProducida.set(value); }
    public ObjectProperty<BigDecimal> cantidadProducidaProperty() { return cantidadProducida; }
}
