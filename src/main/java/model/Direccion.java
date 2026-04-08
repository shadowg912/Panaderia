package model;


import model.Sector;

public class Direccion {
    private  int id_direccion;
    private  String calle;
    private  int numero;
    private   String referencia;
    private Sector sector;

    public Direccion(int id_direccion, String calle, int numero, String referencia, Sector sector) {
        this.id_direccion = id_direccion;
        this.calle = calle;
        this.numero = numero;
        this.referencia = referencia;
        this.sector = sector;
    }

    public int getId_direccion() {
        return id_direccion;
    }

    public void setId_direccion(int id_direccion) {
        this.id_direccion = id_direccion;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }
}