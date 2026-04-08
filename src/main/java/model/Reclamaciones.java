package model;

import javafx.application.Application;

import java.sql.Timestamp;

public class Reclamaciones {
    private int idReclamacionVenta;
    private Timestamp fecha;
    private String motivo;
    private String estadoActual;
    private String tipoReclamacion;
    private String prioridad;
    private int idFacturaVenta;
    private int idEmpresaCliente;
    private Integer idEmpleado;

    public Reclamaciones() {}

    public Reclamaciones(int idReclamacionVenta, Timestamp fecha, String motivo,
                            String estadoActual, String tipoReclamacion, String prioridad,
                            int idFacturaVenta, int idEmpresaCliente, Integer idEmpleado) {
        this.idReclamacionVenta = idReclamacionVenta;
        this.fecha = fecha;
        this.motivo = motivo;
        this.estadoActual = estadoActual;
        this.tipoReclamacion = tipoReclamacion;
        this.prioridad = prioridad;
        this.idFacturaVenta = idFacturaVenta;
        this.idEmpresaCliente = idEmpresaCliente;
        this.idEmpleado = idEmpleado;
    }

    public int getIdReclamacionVenta() { return idReclamacionVenta; }
    public void setIdReclamacionVenta(int idReclamacionVenta) { this.idReclamacionVenta = idReclamacionVenta; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }

    public String getTipoReclamacion() { return tipoReclamacion; }
    public void setTipoReclamacion(String tipoReclamacion) { this.tipoReclamacion = tipoReclamacion; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public int getIdFacturaVenta() { return idFacturaVenta; }
    public void setIdFacturaVenta(int idFacturaVenta) { this.idFacturaVenta = idFacturaVenta; }

    public int getIdEmpresaCliente() { return idEmpresaCliente; }
    public void setIdEmpresaCliente(int idEmpresaCliente) { this.idEmpresaCliente = idEmpresaCliente; }

    public Integer getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Integer idEmpleado) { this.idEmpleado = idEmpleado; }
}