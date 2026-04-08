package model;

public class FormaPago {
    private int idFormaPago;
    private String nombre;

    public FormaPago() {}

    public FormaPago(int idFormaPago, String nombre) {
        this.idFormaPago = idFormaPago;
        this.nombre = nombre;
    }

    public int getIdFormaPago() { return idFormaPago; }
    public void setIdFormaPago(int idFormaPago) { this.idFormaPago = idFormaPago; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return nombre;
    }
}