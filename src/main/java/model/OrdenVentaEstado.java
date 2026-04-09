package model;

import model.DetalleOrdenVenta;
import model.OrdenVenta;
import java.util.List;

public class OrdenVentaEstado {


    public static OrdenVenta ordenActual;
    public static List<DetalleOrdenVenta> detalles;
    public static int    idEmpresaCliente;
    public static int    idFormaPago;
    public static String nombreCliente;
    public static String nombreFormaPago;

    public static void limpiar() {
        ordenActual      = null;
        detalles         = null;
        nombreCliente    = null;
        nombreFormaPago  = null;
        idEmpresaCliente = 0;
        idFormaPago      = 0;
    }


    private OrdenVentaEstado() {}
}