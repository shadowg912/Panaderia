package model;

import model.DetalleOrdenVenta;
import model.OrdenVenta;
import java.util.List;

public class OrdenVentaEstado {

    public static int idOrdenVenta;
    public static OrdenVenta ordenActual;
    public static List<DetalleOrdenVenta> detalles;
    public static int    idCliente;
    public static int    idFormaPago;
    public static String nombreCliente;
    public static String nombreFormaPago;
    public static Integer idEmpleado;

    public static void limpiar() {
        ordenActual      = null;
        detalles         = null;
        nombreCliente    = null;
        nombreFormaPago  = null;
        idCliente = 0;
        idFormaPago      = 0;
        idEmpleado       = null;
        idOrdenVenta     = 0;
    }


    private OrdenVentaEstado() {}
}