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

    public String getGrasasSaturadas() {
        return grasasSaturadas;
    }

    public void setGrasasSaturadas(String grasasSaturadas) {
        this.grasasSaturadas = grasasSaturadas;
    }

    public String getGrasasTrans() {
        return grasasTrans;
    }

    public void setGrasasTrans(String grasasTrans) {
        this.grasasTrans = grasasTrans;
    }

    public String getCarbohidratos() {
        return carbohidratos;
    }

    public void setCarbohidratos(String carbohidratos) {
        this.carbohidratos = carbohidratos;
    }

    public String getAzucares() {
        return azucares;
    }

    public void setAzucares(String azucares) {
        this.azucares = azucares;
    }

    public String getProteinas() {
        return proteinas;
    }

    public void setProteinas(String proteinas) {
        this.proteinas = proteinas;
    }

    public String getFibraDietetica() {
        return fibraDietetica;
    }

    public void setFibraDietetica(String fibraDietetica) {
        this.fibraDietetica = fibraDietetica;
    }

    public String getSodio() {
        return sodio;
    }

    public void setSodio(String sodio) {
        this.sodio = sodio;
    }

    public String getVitaminasMinerales() {
        return vitaminasMinerales;
    }

    public void setVitaminasMinerales(String vitaminasMinerales) {
        this.vitaminasMinerales = vitaminasMinerales;
    }

    public String getCondicionesConservacion() { return condicionesConservacion; }
    public void setCondicionesConservacion(String condicionesConservacion) {
        this.condicionesConservacion = condicionesConservacion;
    }
}
