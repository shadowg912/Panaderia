package model;

public class Ingrediente {
    private int idIngrediente;
    private String nombre;
    private double cantidad;
    private String unidad;

    public Ingrediente() {}

    public Ingrediente(int id, String nombre, double cantidad, String unidad) {
        this.idIngrediente = id;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.unidad = unidad;
    }

    public int getIdIngrediente() { return idIngrediente; }
    public void setIdIngrediente(int id) { this.idIngrediente = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
}