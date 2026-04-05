package model;

public class ValorNutricional {
    private Producto producto;  // FK como objeto (y PK)
    private String calorias;
    private String grasasTotales;
    private String grasasSaturadas;
    private String grasasTrans;
    private String carbohidratos;
    private String azucares;
    private String proteinas;
    private String fibraDietetica;
    private String sodio;
    private String vitaminasMinerales;
    private String condicionesConservacion;

    public ValorNutricional() {}

    // Getters y Setters para todos...
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public int getIdProducto() {
        return producto != null ? producto.getIdProducto() : 0;
    }

    public String getCalorias() { return calorias; }
    public void setCalorias(String calorias) { this.calorias = calorias; }

    public String getGrasasTotales() { return grasasTotales; }
    public void setGrasasTotales(String grasasTotales) { this.grasasTotales = grasasTotales; }

    // ... resto de getters/setters (son muchos, los omito por brevedad)

    public String getCondicionesConservacion() { return condicionesConservacion; }
    public void setCondicionesConservacion(String condicionesConservacion) {
        this.condicionesConservacion = condicionesConservacion;
    }
}
