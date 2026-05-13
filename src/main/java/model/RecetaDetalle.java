package model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class RecetaDetalle {
    private final SimpleIntegerProperty idProductoIngrediente;
    private final SimpleStringProperty nombreIngrediente;
    private final SimpleDoubleProperty cantidad;
    private final SimpleStringProperty unidadMedida;

    public RecetaDetalle() {
        this.idProductoIngrediente = new SimpleIntegerProperty(0);
        this.nombreIngrediente = new SimpleStringProperty("");
        this.cantidad = new SimpleDoubleProperty(0);
        this.unidadMedida = new SimpleStringProperty("");
    }

    public RecetaDetalle(int idProductoIngrediente, String nombreIngrediente, double cantidad, String unidadMedida) {
        this.idProductoIngrediente = new SimpleIntegerProperty(idProductoIngrediente);
        this.nombreIngrediente = new SimpleStringProperty(nombreIngrediente);
        this.cantidad = new SimpleDoubleProperty(cantidad);
        this.unidadMedida = new SimpleStringProperty(unidadMedida);
    }

    public int getIdProductoIngrediente() { return idProductoIngrediente.get(); }
    public void setIdProductoIngrediente(int value) { idProductoIngrediente.set(value); }
    public SimpleIntegerProperty idProductoIngredienteProperty() { return idProductoIngrediente; }

    public String getNombreIngrediente() { return nombreIngrediente.get(); }
    public void setNombreIngrediente(String value) { nombreIngrediente.set(value); }
    public SimpleStringProperty nombreIngredienteProperty() { return nombreIngrediente; }

    public double getCantidad() { return cantidad.get(); }
    public void setCantidad(double value) { cantidad.set(value); }
    public SimpleDoubleProperty cantidadProperty() { return cantidad; }

    public String getUnidadMedida() { return unidadMedida.get(); }
    public void setUnidadMedida(String value) { unidadMedida.set(value); }
    public SimpleStringProperty unidadMedidaProperty() { return unidadMedida; }
}