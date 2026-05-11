package model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Usuario {
    private SimpleIntegerProperty idUsuario;
    private SimpleStringProperty nombreUsuario;
    private SimpleStringProperty nombreEmpleado;
    private SimpleStringProperty nombreRol;
    private SimpleBooleanProperty estado;
    private SimpleIntegerProperty idEmpleado;

    public Usuario() {
        this.idUsuario = new SimpleIntegerProperty(0);
        this.nombreUsuario = new SimpleStringProperty("");
        this.nombreEmpleado = new SimpleStringProperty("");
        this.nombreRol = new SimpleStringProperty("");
        this.estado = new SimpleBooleanProperty(true);
        this.idEmpleado = new SimpleIntegerProperty(0);
    }

    public int getIdUsuario() { return idUsuario.get(); }
    public void setIdUsuario(int value) { idUsuario.set(value); }
    public SimpleIntegerProperty idUsuarioProperty() { return idUsuario; }

    public String getNombreUsuario() { return nombreUsuario.get(); }
    public void setNombreUsuario(String value) { nombreUsuario.set(value); }
    public SimpleStringProperty nombreUsuarioProperty() { return nombreUsuario; }

    public String getNombreEmpleado() { return nombreEmpleado.get(); }
    public void setNombreEmpleado(String value) { nombreEmpleado.set(value); }
    public SimpleStringProperty nombreEmpleadoProperty() { return nombreEmpleado; }

    public String getNombreRol() { return nombreRol.get(); }
    public void setNombreRol(String value) { nombreRol.set(value); }
    public SimpleStringProperty nombreRolProperty() { return nombreRol; }

    public boolean isEstado() { return estado.get(); }
    public void setEstado(boolean value) { estado.set(value); }
    public SimpleBooleanProperty estadoProperty() { return estado; }

    public int getIdEmpleado() { return idEmpleado.get(); }
    public void setIdEmpleado(int value) { idEmpleado.set(value); }
    public SimpleIntegerProperty idEmpleadoProperty() { return idEmpleado; }

    public String getEstadoTexto() {
        return estado.get() ? "Activo" : "Inactivo";
    }
}