package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.math.BigDecimal;
import java.util.Date;

public class OrdenProduccion {
    private final SimpleIntegerProperty idOrdenProduccion;
    private final SimpleStringProperty nombreProducto;
    private final SimpleStringProperty nombreEmpleado;
    private final SimpleStringProperty cantidadTexto;
    private final SimpleStringProperty fechaTexto;
    private final SimpleStringProperty estado;

    public OrdenProduccion() {
        this.idOrdenProduccion = new SimpleIntegerProperty(0);
        this.nombreProducto = new SimpleStringProperty("");
        this.nombreEmpleado = new SimpleStringProperty("");
        this.cantidadTexto = new SimpleStringProperty("");
        this.fechaTexto = new SimpleStringProperty("");
        this.estado = new SimpleStringProperty("");
    }

    public int getIdOrdenProduccion() { return idOrdenProduccion.get(); }
    public void setIdOrdenProduccion(int value) { idOrdenProduccion.set(value); }
    public SimpleIntegerProperty idOrdenProduccionProperty() { return idOrdenProduccion; }

    public String getNombreProducto() { return nombreProducto.get(); }
    public void setNombreProducto(String value) { nombreProducto.set(value); }
    public SimpleStringProperty nombreProductoProperty() { return nombreProducto; }

    public String getNombreEmpleado() { return nombreEmpleado.get(); }
    public void setNombreEmpleado(String value) { nombreEmpleado.set(value); }
    public SimpleStringProperty nombreEmpleadoProperty() { return nombreEmpleado; }

    public String getCantidadTexto() { return cantidadTexto.get(); }
    public void setCantidadTexto(String value) { cantidadTexto.set(value); }
    public SimpleStringProperty cantidadTextoProperty() { return cantidadTexto; }

    public String getFechaTexto() { return fechaTexto.get(); }
    public void setFechaTexto(String value) { fechaTexto.set(value); }
    public SimpleStringProperty fechaTextoProperty() { return fechaTexto; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String value) { estado.set(value); }
    public SimpleStringProperty estadoProperty() { return estado; }
}