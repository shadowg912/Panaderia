package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Date;

public class EnvioResumen {
    private final SimpleIntegerProperty idEnvio;
    private final SimpleIntegerProperty idOrden;
    private final SimpleStringProperty cliente;
    private final SimpleStringProperty transportista;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty numeroSeguimiento;
    private final SimpleStringProperty fechaEstimada;

    private int idEstadoEnvio;
    private Integer idTransportista;
    private String direccion;
    private Date fechaAsignacion;
    private Date fechaSalida;
    private Date fechaEntregaReal;

    public EnvioResumen() {
        this.idEnvio = new SimpleIntegerProperty(0);
        this.idOrden = new SimpleIntegerProperty(0);
        this.cliente = new SimpleStringProperty("");
        this.transportista = new SimpleStringProperty("");
        this.estado = new SimpleStringProperty("");
        this.numeroSeguimiento = new SimpleStringProperty("");
        this.fechaEstimada = new SimpleStringProperty("");
    }

    public int getIdEnvio() { return idEnvio.get(); }
    public void setIdEnvio(int v) { idEnvio.set(v); }
    public SimpleIntegerProperty idEnvioProperty() { return idEnvio; }

    public int getIdOrden() { return idOrden.get(); }
    public void setIdOrden(int v) { idOrden.set(v); }
    public SimpleIntegerProperty idOrdenProperty() { return idOrden; }

    public String getCliente() { return cliente.get(); }
    public void setCliente(String v) { cliente.set(v); }
    public SimpleStringProperty clienteProperty() { return cliente; }

    public String getTransportista() { return transportista.get(); }
    public void setTransportista(String v) { transportista.set(v); }
    public SimpleStringProperty transportistaProperty() { return transportista; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String v) { estado.set(v); }
    public SimpleStringProperty estadoProperty() { return estado; }

    public String getNumeroSeguimiento() { return numeroSeguimiento.get(); }
    public void setNumeroSeguimiento(String v) { numeroSeguimiento.set(v); }
    public SimpleStringProperty numeroSeguimientoProperty() { return numeroSeguimiento; }

    public String getFechaEstimada() { return fechaEstimada.get(); }
    public void setFechaEstimada(String v) { fechaEstimada.set(v); }
    public SimpleStringProperty fechaEstimadaProperty() { return fechaEstimada; }

    public int getIdEstadoEnvio() { return idEstadoEnvio; }
    public void setIdEstadoEnvio(int v) { this.idEstadoEnvio = v; }

    public Integer getIdTransportista() { return idTransportista; }
    public void setIdTransportista(Integer v) { this.idTransportista = v; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String v) { this.direccion = v; }

    public Date getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(Date v) { this.fechaAsignacion = v; }

    public Date getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(Date v) { this.fechaSalida = v; }

    public Date getFechaEntregaReal() { return fechaEntregaReal; }
    public void setFechaEntregaReal(Date v) { this.fechaEntregaReal = v; }
}