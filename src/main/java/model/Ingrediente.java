package model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class Ingrediente {
    private final SimpleIntegerProperty idIngrediente;
    private final SimpleStringProperty nombre;
    private final SimpleDoubleProperty cantidad;
    private final SimpleStringProperty unidad;

    public Ingrediente() {
        this.idIngrediente = new SimpleIntegerProperty(0);
        this.nombre = new SimpleStringProperty("");
        this.cantidad = new SimpleDoubleProperty(0);
        this.unidad = new SimpleStringProperty("");
    }

    public Ingrediente(int id, String nombre, double cantidad, String unidad) {
        this.idIngrediente = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.cantidad = new SimpleDoubleProperty(cantidad);
        this.unidad = new SimpleStringProperty(unidad);
    }

    public int getIdIngrediente() { return idIngrediente.get(); }
    public void setIdIngrediente(int id) { idIngrediente.set(id); }
    public SimpleIntegerProperty idIngredienteProperty() { return idIngrediente; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String value) { nombre.set(value); }
    public SimpleStringProperty nombreProperty() { return nombre; }

    public double getCantidad() { return cantidad.get(); }
    public void setCantidad(double value) { cantidad.set(value); }
    public SimpleDoubleProperty cantidadProperty() { return cantidad; }

    public String getUnidad() { return unidad.get(); }
    public void setUnidad(String value) { unidad.set(value); }
    public SimpleStringProperty unidadProperty() { return unidad; }
}