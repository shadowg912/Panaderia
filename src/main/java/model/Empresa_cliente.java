package model;

public class Empresa_cliente {
    private int idEmpresaCliente;
    private String Nombre;
    private String rnc;
    private String telefono;
    private String correo;
    private Direccion direccion;

    public Empresa_cliente(){}

    public Empresa_cliente(int idEmpresaCliente, String nombre) {
        this.idEmpresaCliente = idEmpresaCliente;
        this.Nombre = nombre;
    }


    public Empresa_cliente(String nombre, String rnc, String telefono, String correo, Direccion direccion) {
        this.Nombre = nombre;
        this.rnc = rnc;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion=direccion;
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

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public int getIdEmpresaCliente() {
        return idEmpresaCliente;
    }

    public void setIdEmpresaCliente(int idEmpresaCliente) {
        this.idEmpresaCliente = idEmpresaCliente;
    }

    @Override
    public String toString() {
        return Nombre;
    }

}
