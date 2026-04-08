package model;

public class Usuario {
    private int idUsuario;
    private String nombreUsuario;
    private String passwordHash;
    private Rol rol;
    private boolean estado;
    private Empleado empleado;

    public Usuario() {}

    public Usuario(int idUsuario, String nombreUsuario, String passwordHash, Rol rol, boolean estado) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.estado = estado;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public int getIdRol() {
        return rol != null ? rol.getIdRol() : 0;
    }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public int getIdEmpleado() {
        return empleado != null ? empleado.getIdEmpleado() : 0;
    }
}