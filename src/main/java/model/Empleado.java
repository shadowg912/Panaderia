package model;

public class Empleado {
    private int idEmpleado;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String numeroTelefono;
    private String puesto;

    public Empleado() {}

    public Empleado(int idEmpleado, String nombre, String apellido1, String apellido2,
                    String numeroTelefono, String puesto) {
        this.idEmpleado = idEmpleado;
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.numeroTelefono = numeroTelefono;
        this.puesto = puesto;
    }

    // Constructor simplificado para ComboBox
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

    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }

    // Para mostrar en ComboBox: "Juan Pérez"
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

