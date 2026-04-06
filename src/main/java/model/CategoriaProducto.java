package model;

public class CategoriaProducto {
    private int idCategoriaProducto;
    private String nombre;


    public CategoriaProducto() {}

    public CategoriaProducto(int idCategoriaProducto, String nombre) {
        this.idCategoriaProducto = idCategoriaProducto;
        this.nombre = nombre;

    }

    public int getIdCategoriaProducto() { return idCategoriaProducto; }
    public void setIdCategoriaProducto(int idCategoriaProducto) { this.idCategoriaProducto = idCategoriaProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }



    @Override
    public String toString() {
        return nombre;
    }
}
