package model;

public class CategoriaProducto {
    private int idCategoriaProducto;
    private String nombre;
    private String descripcion;

    public CategoriaProducto() {}

    public CategoriaProducto(int idCategoriaProducto, String nombre, String descripcion) {
        this.idCategoriaProducto = idCategoriaProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdCategoriaProducto() { return idCategoriaProducto; }
    public void setIdCategoriaProducto(int idCategoriaProducto) { this.idCategoriaProducto = idCategoriaProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return nombre;
    }
}
