package model;
import java.math.BigDecimal;

public class RecetaProducto {
    private Producto producto;
    private Ingrediente ingrediente;
    private BigDecimal cantidadIngrediente;


    public RecetaProducto() {}

    public RecetaProducto(Producto producto, Ingrediente ingrediente, BigDecimal cantidadIngrediente) {
        this.producto = producto;
        this.ingrediente = ingrediente;
        this.cantidadIngrediente = cantidadIngrediente;
    }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Ingrediente getIngrediente() { return ingrediente; }
    public void setIngrediente(Ingrediente ingrediente) { this.ingrediente = ingrediente; }

    public BigDecimal getCantidadIngrediente() { return cantidadIngrediente; }
    public void setCantidadIngrediente(BigDecimal cantidadIngrediente) {
        this.cantidadIngrediente = cantidadIngrediente;
    }


    public int getIdProducto() {
        return producto != null ? producto.getIdProducto() : 0;
    }

    public int getIdIngrediente() {
        return ingrediente != null ? ingrediente.getIdIngrediente() : 0;
    }
}