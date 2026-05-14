package model;

public class Puesto {
    private int idPuesto;
    private String nombre;
    private String descripcion;
    private String area;
    private boolean estado;

    public Puesto() {}

    public Puesto(int idPuesto, String nombre) {
        this.idPuesto = idPuesto;
        this.nombre = nombre;
    }

    public Puesto(int idPuesto, String nombre, String area) {
        this.idPuesto = idPuesto;
        this.nombre = nombre;
        this.area = area;
    }

    public int getIdPuesto() { return idPuesto; }
    public void setIdPuesto(int idPuesto) { this.idPuesto = idPuesto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    @Override
    public String toString() {
        return nombre;
    }
}