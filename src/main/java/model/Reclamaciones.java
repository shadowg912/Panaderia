package model;

import java.sql.Timestamp;

public class Reclamaciones {
    private int idReclamacionVenta;
    private Timestamp fecha;
    private String motivo;
    private String estadoActual;
    private String tipoReclamacion;
    private String prioridad;
    private int idOrdenVenta;
    private int idCliente;
    private Integer idEmpleado;

    public Reclamaciones() {}

    public Reclamaciones(int idReclamacionVenta, Timestamp fecha, String motivo,
                            String estadoActual, String tipoReclamacion, String prioridad,
                            int idOrdenVenta, int idCliente, Integer idEmpleado) {
        this.idReclamacionVenta = idReclamacionVenta;
        this.fecha = fecha;
        this.motivo = motivo;
        this.estadoActual = estadoActual;
        this.tipoReclamacion = tipoReclamacion;
        this.prioridad = prioridad;
        this.idOrdenVenta = idOrdenVenta;
        this.idCliente = idCliente;
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

    public int getIdOrdenVenta() { return idOrdenVenta; }
    public void setIdOrdenVenta(int idOrdenVenta) { this.idOrdenVenta = idOrdenVenta; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public Integer getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Integer idEmpleado) { this.idEmpleado = idEmpleado; }
}