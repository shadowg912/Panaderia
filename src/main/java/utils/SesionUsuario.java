package utils;

public class SesionUsuario {
    private static int idUsuario;
    private static String nombreUsuario;
    private static String nombreRol;
    private static int idEmpleado;
    private static String nombreEmpleado;
    private static boolean sesionActiva;

    public static void iniciarSesion(int id, String usuario, String rol, int idEmp, String nombreEmp) {
        idUsuario = id;
        nombreUsuario = usuario;
        nombreRol = rol;
        idEmpleado = idEmp;
        nombreEmpleado = nombreEmp;
        sesionActiva = true;
    }

    public static void cerrarSesion() {
        idUsuario = 0;
        nombreUsuario = null;
        nombreRol = null;
        idEmpleado = 0;
        nombreEmpleado = null;
        sesionActiva = false;
    }

    public static int getIdUsuario() { return idUsuario; }
    public static String getNombreUsuario() { return nombreUsuario; }
    public static String getNombreRol() { return nombreRol; }
    public static int getIdEmpleado() { return idEmpleado; }
    public static String getNombreEmpleado() { return nombreEmpleado; }
    public static boolean isSesionActiva() { return sesionActiva; }
}
