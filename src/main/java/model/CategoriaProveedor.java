package model;

public class CategoriaProveedor {
    private int idCategoriaProveedor;
    private String nombre;
    private String descripcion;

    public CategoriaProveedor() {}

    public CategoriaProveedor(int idCategoriaProveedor, String nombre) {
        this.idCategoriaProveedor = idCategoriaProveedor;
        this.nombre = nombre;
    }

    public CategoriaProveedor(int idCategoriaProveedor, String nombre, String descripcion) {
        this.idCategoriaProveedor = idCategoriaProveedor;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdCategoriaProveedor() { return idCategoriaProveedor; }
    public void setIdCategoriaProveedor(int idCategoriaProveedor) { this.idCategoriaProveedor = idCategoriaProveedor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return nombre;
    }
}