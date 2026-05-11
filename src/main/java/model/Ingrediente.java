package model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Ingrediente {
    private SimpleIntegerProperty idIngrediente;
    private SimpleStringProperty nombre;
    private SimpleDoubleProperty cantidad;
    private SimpleStringProperty unidad;

    public Ingrediente() {}

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
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public SimpleStringProperty nombreProperty() { return nombre; }

    public double getCantidad() { return cantidad.get(); }
    public void setCantidad(double cantidad) { this.cantidad.set(cantidad); }
    public SimpleDoubleProperty cantidadProperty() { return cantidad; }

    public String getUnidad() { return unidad.get(); }
    public void setUnidad(String unidad) { this.unidad.set(unidad); }
    public SimpleStringProperty unidadProperty() { return unidad; }
}