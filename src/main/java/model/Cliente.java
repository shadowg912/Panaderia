package model;

public class Cliente {
    private int idCliente;
    private String nombre;
    private String rnc;
    private String telefono;
    private String correo;
    private Direccion direccion;

    public Cliente(){}

    public Cliente(int idCliente, String nombre) {
        this.idCliente = idCliente;
        this.nombre = nombre;
    }

    public Cliente(String nombre, String rnc, String telefono, String correo, Direccion direccion) {
        this.nombre = nombre;
        this.rnc = rnc;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRnc() { return rnc; }
    public void setRnc(String rnc) { this.rnc = rnc; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public Direccion getDireccion() { return direccion; }
    public void setDireccion(Direccion direccion) { this.direccion = direccion; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    @Override
    public String toString() {
        return nombre;
    }
}