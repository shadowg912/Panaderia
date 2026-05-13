package model;

public class Provincia {
    private int id_provincia;
    private String nombre;
    private int idPais;

    public Provincia(String nombre) {
        this.nombre = nombre;
    }

    public Provincia(int id_provincia, String nombre) {
        this.id_provincia = id_provincia;
        this.nombre = nombre;
    }

    public Provincia(int id_provincia, String nombre, int idPais) {
        this.id_provincia = id_provincia;
        this.nombre = nombre;
        this.idPais = idPais;
    }

    public int getId_provincia() { return id_provincia; }
    public void setId_provincia(int id_provincia) { this.id_provincia = id_provincia; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getIdPais() { return idPais; }
    public void setIdPais(int idPais) { this.idPais = idPais; }

    @Override
    public String toString() {
        return nombre;
    }
}
