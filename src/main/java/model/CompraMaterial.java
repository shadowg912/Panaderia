package model;

import java.time.LocalDate;

public class CompraMaterial {
    private int idCompraMaterial;
    private LocalDate fecha;
    private String estado;
    private int idFormaPago;
    private int idProveedor;
    private String nombreProveedor;
    private String nombreFormaPago;
    private double montoTotal;

    public CompraMaterial() {}

    public int getIdCompraMaterial() { return idCompraMaterial; }
    public void setIdCompraMaterial(int v) { idCompraMaterial = v; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate v) { fecha = v; }

    public String getEstado() { return estado; }
    public void setEstado(String v) { estado = v; }

    public int getIdFormaPago() { return idFormaPago; }
    public void setIdFormaPago(int v) { idFormaPago = v; }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int v) { idProveedor = v; }

    public String getNombreProveedor() { return nombreProveedor; }
    public void setNombreProveedor(String v) { nombreProveedor = v; }

    public String getNombreFormaPago() { return nombreFormaPago; }
    public void setNombreFormaPago(String v) { nombreFormaPago = v; }

    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double v) { montoTotal = v; }
}
