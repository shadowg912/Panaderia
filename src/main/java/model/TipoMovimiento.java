package model;

public class TipoMovimiento {
    private int idTipo;
    private String nombre;
    private String naturaleza;

    public TipoMovimiento() {}

    public TipoMovimiento(int idTipo, String nombre, String naturaleza) {
        this.idTipo = idTipo;
        this.nombre = nombre;
        this.naturaleza = naturaleza;
    }

    public int getIdTipo() { return idTipo; }
    public void setIdTipo(int idTipo) { this.idTipo = idTipo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNaturaleza() { return naturaleza; }
    public void setNaturaleza(String naturaleza) { this.naturaleza = naturaleza; }

    @Override
    public String toString() { return nombre; }
}
