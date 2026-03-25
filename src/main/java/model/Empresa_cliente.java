package model;

public class Empresa_cliente {

    private String Nombre;
    private String rnc;
    private String telefono;
    private String correo;

    public Empresa_cliente(String nombre, String rnc, String telefono, String correo) {
        this.Nombre = nombre;
        this.rnc = rnc;
        this.telefono = telefono;
        this.correo = correo;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getRnc() {
        return rnc;
    }

    public void setRnc(String rnc) {
        this.rnc = rnc;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
