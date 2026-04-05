package model.Ubicacion;

class Provincia {
    private int id_provincia;
    private String nombre;
    private Pais pais;

    public Provincia(int id_provincia, String nombre, Pais pais) {
        this.id_provincia = id_provincia;
        this.nombre = nombre;
        this.pais = pais;
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

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }
}
