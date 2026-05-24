package model;

import java.util.List;

public class OrdenProduccionEstado {

    public static int idOrdenProduccion;
    public static int idEmpleado;
    public static String nombreEmpleado;
    public static String fecha;
    public static List<DetalleOrdenProduccion> detalles;

    public static void limpiar() {
        idOrdenProduccion = 0;
        idEmpleado = 0;
        nombreEmpleado = null;
        fecha = null;
        detalles = null;
    }

    private OrdenProduccionEstado() {}
}
