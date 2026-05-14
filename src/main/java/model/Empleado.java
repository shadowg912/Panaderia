package model;

public class Empleado {
    private int idEmpleado;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String numeroTelefono;
    private Integer idPuesto;

    public Empleado() {}

    public Empleado(int idEmpleado, String nombre, String apellido1, String apellido2,
                    String numeroTelefono) {
        this.idEmpleado = idEmpleado;
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.numeroTelefono = numeroTelefono;
    }

    public Empleado(int idEmpleado, String nombre, String apellido1) {
        this.idEmpleado = idEmpleado;
        this.nombre = nombre;
        this.apellido1 = apellido1;
    }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido1() { return apellido1; }
    public void setApellido1(String apellido1) { this.apellido1 = apellido1; }

    public String getApellido2() { return apellido2; }
    public void setApellido2(String apellido2) { this.apellido2 = apellido2; }

    public String getNumeroTelefono() { return numeroTelefono; }
    public void setNumeroTelefono(String numeroTelefono) { this.numeroTelefono = numeroTelefono; }

    public Integer getIdPuesto() { return idPuesto; }
    public void setIdPuesto(Integer idPuesto) { this.idPuesto = idPuesto; }

    public String getNombreCompleto() {
        if (apellido2 != null && !apellido2.isEmpty()) {
            return nombre + " " + apellido1 + " " + apellido2;
        }
        return nombre + " " + apellido1;
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}

