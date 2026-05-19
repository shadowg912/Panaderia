package model;

import java.util.List;

public class CompraEstado {
    public static int idCompraMaterial;
    public static int idProveedor;
    public static String nombreProveedor;
    public static int idFormaPago;
    public static String nombreFormaPago;
    public static List<DetalleCompra> detalles;

    public static void limpiar() {
        idCompraMaterial = 0;
        idProveedor = 0;
        nombreProveedor = null;
        idFormaPago = 0;
        nombreFormaPago = null;
        detalles = null;
    }

    private CompraEstado() {}
}
