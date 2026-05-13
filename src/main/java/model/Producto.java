package model;

import javafx.beans.property.SimpleStringProperty;

import java.math.BigDecimal;

public class Producto {
    private int idProducto;
    private String nombre;
    private CategoriaProducto categoriaProducto;
    private BigDecimal precioUnitario;
    private Unidad unidad;
    private String tipoProducto;

    private final SimpleStringProperty nombreDisplay;
    private final SimpleStringProperty categoriaDisplay;
    private final SimpleStringProperty precioDisplay;
    private final SimpleStringProperty unidadDisplay;

    public Producto() {
        this.nombreDisplay = new SimpleStringProperty("");
        this.categoriaDisplay = new SimpleStringProperty("");
        this.precioDisplay = new SimpleStringProperty("");
        this.unidadDisplay = new SimpleStringProperty("");
    }

    public Producto(int idProducto, String nombre) {
        this();
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.nombreDisplay.set(nombre);
    }

    public Producto(int idProducto, String nombre, CategoriaProducto categoriaProducto,
                    BigDecimal precioUnitario, Unidad unidad) {
        this();
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.categoriaProducto = categoriaProducto;
        this.precioUnitario = precioUnitario;
        this.unidad = unidad;
        this.nombreDisplay.set(nombre != null ? nombre : "");
        this.categoriaDisplay.set(categoriaProducto != null ? categoriaProducto.getNombre() : "");
        this.precioDisplay.set(precioUnitario != null ? String.format("%.2f", precioUnitario) : "");
        this.unidadDisplay.set(unidad != null ? unidad.getNombre() : "");
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; nombreDisplay.set(nombre); }

    public CategoriaProducto getCategoriaProducto() { return categoriaProducto; }
    public void setCategoriaProducto(CategoriaProducto categoriaProducto) { this.categoriaProducto = categoriaProducto; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public Unidad getUnidad() { return unidad; }
    public void setUnidad(Unidad unidad) { this.unidad = unidad; }

    public String getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; }

    public int getIdCategoriaProducto() {
        return categoriaProducto != null ? categoriaProducto.getIdCategoriaProducto() : 0;
    }

    public int getIdUnidad() {
        return unidad != null ? unidad.getIdUnidad() : 0;
    }

    public String getNombreDisplay() { return nombreDisplay.get(); }
    public void setNombreDisplay(String value) { nombreDisplay.set(value); }
    public SimpleStringProperty nombreDisplayProperty() { return nombreDisplay; }

    public String getCategoriaDisplay() { return categoriaDisplay.get(); }
    public void setCategoriaDisplay(String value) { categoriaDisplay.set(value); }
    public SimpleStringProperty categoriaDisplayProperty() { return categoriaDisplay; }

    public String getPrecioDisplay() { return precioDisplay.get(); }
    public void setPrecioDisplay(String value) { precioDisplay.set(value); }
    public SimpleStringProperty precioDisplayProperty() { return precioDisplay; }

    public String getUnidadDisplay() { return unidadDisplay.get(); }
    public void setUnidadDisplay(String value) { unidadDisplay.set(value); }
    public SimpleStringProperty unidadDisplayProperty() { return unidadDisplay; }

    @Override
    public String toString() {
        return nombre;
    }
}