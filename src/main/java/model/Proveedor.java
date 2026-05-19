package model;

import model.CategoriaProveedor;
public class Proveedor {
    private int idProveedor;
    private String nombre;
    private int idDireccion;
    private String correoElectronico;
    private String numeroTelefono;
    private int idCategoriaProveedor;
    private CategoriaProveedor categoriaProveedor;

    public Proveedor() {}

    public Proveedor(int idProveedor, String nombre, int idDireccion,
                     String correoElectronico, String numeroTelefono,
                     int idCategoriaProveedor) {
        this.idProveedor = idProveedor;
        this.nombre = nombre;
        this.idDireccion = idDireccion;
        this.correoElectronico = correoElectronico;
        this.numeroTelefono = numeroTelefono;
        this.idCategoriaProveedor = idCategoriaProveedor;
    }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getIdDireccion() { return idDireccion; }
    public void setIdDireccion(int idDireccion) { this.idDireccion = idDireccion; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public String getNumeroTelefono() { return numeroTelefono; }
    public void setNumeroTelefono(String numeroTelefono) { this.numeroTelefono = numeroTelefono; }

    public int getIdCategoriaProveedor() {
        return categoriaProveedor != null ? categoriaProveedor.getIdCategoriaProveedor() : idCategoriaProveedor;
    }
    public void setIdCategoriaProveedor(int idCategoriaProveedor) { this.idCategoriaProveedor = idCategoriaProveedor; }

    public CategoriaProveedor getCategoriaProveedor() { return categoriaProveedor; }
    public void setCategoriaProveedor(CategoriaProveedor categoriaProveedor) { this.categoriaProveedor = categoriaProveedor; }

    @Override
    public String toString() {
        return nombre;
    }
}