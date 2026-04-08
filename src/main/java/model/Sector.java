package model;

public class Sector {
    private int id_sector;
    private String nombre;
    private Ciudad ciudad;

    public Sector(int id_sector, String nombre, Ciudad ciudad) {
        this.id_sector = id_sector;
        this.nombre = nombre;
        this.ciudad = ciudad;
    }

    public int getId_sector() {
        return id_sector;
    }

    public void setId_sector(int id_sector) {
        this.id_sector = id_sector;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Ciudad getCiudad() {
        return ciudad;
    }

    public void setCiudad(Ciudad ciudad) {
        this.ciudad = ciudad;
    }
}
