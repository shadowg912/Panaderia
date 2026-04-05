package model;

public class Unidad {
    private int idUnidad;
    private String nombre;
    private String descripcion;
    private String categoria;


    public Unidad() {}

    public Unidad(int idUnidad, String nombre, String descripcion, String categoria) {
        this.idUnidad = idUnidad;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }


    public int getIdUnidad() { return idUnidad; }
    public void setIdUnidad(int idUnidad) { this.idUnidad = idUnidad; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }


    @Override
    public String toString() {
        return nombre;
    }
}
