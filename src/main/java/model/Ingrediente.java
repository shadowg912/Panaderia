package model;

public class Ingrediente {
    private int idIngrediente;
    private String nombre;
    private String unidadMedida;
    private CategoriaIngrediente categoriaIngrediente;  // FK como objeto

    public Ingrediente() {}

    public Ingrediente(int idIngrediente, String nombre, String unidadMedida,
                       CategoriaIngrediente categoriaIngrediente) {
        this.idIngrediente = idIngrediente;
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.categoriaIngrediente = categoriaIngrediente;
    }

    public int getIdIngrediente() { return idIngrediente; }
    public void setIdIngrediente(int idIngrediente) { this.idIngrediente = idIngrediente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public CategoriaIngrediente getCategoriaIngrediente() { return categoriaIngrediente; }
    public void setCategoriaIngrediente(CategoriaIngrediente categoriaIngrediente) {
        this.categoriaIngrediente = categoriaIngrediente;
    }

    @Override
    public String toString() {
        return nombre;
    }

}
