package model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.math.BigDecimal;

public class DetalleCompra {
    private SimpleIntegerProperty idCompraMaterial;
    private SimpleIntegerProperty idProducto;
    private SimpleStringProperty nombreProducto;
    private SimpleDoubleProperty cantidad;
    private SimpleDoubleProperty costoUnitario;
    private SimpleDoubleProperty montoTotal;

    public DetalleCompra() {
        idCompraMaterial = new SimpleIntegerProperty(0);
        idProducto = new SimpleIntegerProperty(0);
        nombreProducto = new SimpleStringProperty("");
        cantidad = new SimpleDoubleProperty(0);
        costoUnitario = new SimpleDoubleProperty(0);
        montoTotal = new SimpleDoubleProperty(0);
    }

    public int getIdCompraMaterial() { return idCompraMaterial.get(); }
    public void setIdCompraMaterial(int v) { idCompraMaterial.set(v); }
    public SimpleIntegerProperty idCompraMaterialProperty() { return idCompraMaterial; }

    public int getIdProducto() { return idProducto.get(); }
    public void setIdProducto(int v) { idProducto.set(v); }
    public SimpleIntegerProperty idProductoProperty() { return idProducto; }

    public String getNombreProducto() { return nombreProducto.get(); }
    public void setNombreProducto(String v) { nombreProducto.set(v); }
    public SimpleStringProperty nombreProductoProperty() { return nombreProducto; }

    public double getCantidad() { return cantidad.get(); }
    public void setCantidad(double v) { cantidad.set(v); }
    public void setCantidad(BigDecimal v) { cantidad.set(v.doubleValue()); }
    public SimpleDoubleProperty cantidadProperty() { return cantidad; }

    public double getCostoUnitario() { return costoUnitario.get(); }
    public void setCostoUnitario(double v) { costoUnitario.set(v); }
    public SimpleDoubleProperty costoUnitarioProperty() { return costoUnitario; }

    public double getMontoTotal() { return montoTotal.get(); }
    public void setMontoTotal(double v) { montoTotal.set(v); }
    public SimpleDoubleProperty montoTotalProperty() { return montoTotal; }
}
