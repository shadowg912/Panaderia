package model;

import java.math.BigDecimal;

public class Producto {
    private int idProducto;
    private String nombre;
    private CategoriaProducto categoriaProducto;
    private BigDecimal precioUnitario;
    private Unidad unidad;

    public Producto() {}

    public Producto(int idProducto, String nombre, CategoriaProducto categoriaProducto,
                    BigDecimal precioUnitario, Unidad unidad) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.categoriaProducto = categoriaProducto;
        this.precioUnitario = precioUnitario;
        this.unidad = unidad;
    }


    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public CategoriaProducto getCategoriaProducto() { return categoriaProducto; }
    public void setCategoriaProducto(CategoriaProducto categoriaProducto) { this.categoriaProducto = categoriaProducto; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public Unidad getUnidad() { return unidad; }
    public void setUnidad(Unidad unidad) { this.unidad = unidad; }


    public int getIdCategoriaProducto() {
        return categoriaProducto != null ? categoriaProducto.getIdCategoriaProducto() : 0;
    }

    public int getIdUnidad() {
        return unidad != null ? unidad.getIdUnidad() : 0;
    }

    @Override
    public String toString() {
        return nombre;
    }
}