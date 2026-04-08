package model;

public class Provincia {
    private int id_provincia;
    private String nombre;

    public Provincia(String nombre) {
        this.nombre = nombre;
    }

    public Provincia(int id_provincia, String nombre) {
        this.id_provincia = id_provincia;
        this.nombre = nombre;

    }

    public int getId_provincia() {
        return id_provincia;
    }

    public void setId_provincia(int id_provincia) {
        this.id_provincia = id_provincia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
