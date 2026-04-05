package model;

public class CategoriaIngrediente {
    private int idCategoriaIngrediente;
    private String nombre;
    private String descripcion;

    public CategoriaIngrediente() {}

    public CategoriaIngrediente(int idCategoriaIngrediente, String nombre, String descripcion) {
        this.idCategoriaIngrediente = idCategoriaIngrediente;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdCategoriaIngrediente() { return idCategoriaIngrediente; }
    public void setIdCategoriaIngrediente(int idCategoriaIngrediente) {
        this.idCategoriaIngrediente = idCategoriaIngrediente;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return nombre;
    }
}
