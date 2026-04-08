package model;

import model.Provincia;

public class Ciudad {
    private int id_ciudad;
    private String nombre;
    private Provincia provincia;

    public Ciudad(int id_ciudad, String nombre, Provincia provincia) {
        this.id_ciudad = id_ciudad;
        this.nombre = nombre;
        this.provincia = provincia;
    }

    public int getId_ciudad() {
        return id_ciudad;
    }

    public void setId_ciudad(int id_ciudad) {
        this.id_ciudad = id_ciudad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }
}
