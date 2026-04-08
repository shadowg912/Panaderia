package model;


import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

    public class OrdenProduccion {
        private int idOrdenProduccion;
        private Producto producto;
        private Empleado empleado;
        private BigDecimal cantidadPlanificada;
        private Date fechaProduccion;
        private String estado;
        private Timestamp fechaRegistro;

        public OrdenProduccion() {}

        public OrdenProduccion(int idOrdenProduccion, Producto producto, Empleado empleado,
                               BigDecimal cantidadPlanificada, Date fechaProduccion,
                               String estado, Timestamp fechaRegistro) {
            this.idOrdenProduccion = idOrdenProduccion;
            this.producto = producto;
            this.empleado = empleado;
            this.cantidadPlanificada = cantidadPlanificada;
            this.fechaProduccion = fechaProduccion;
            this.estado = estado;
            this.fechaRegistro = fechaRegistro;
        }

        // Constructor para insertar (sin ID ni fechas automáticas)
        public OrdenProduccion(Producto producto, Empleado empleado,
                               BigDecimal cantidadPlanificada, String estado) {
            this.producto = producto;
            this.empleado = empleado;
            this.cantidadPlanificada = cantidadPlanificada;
            this.estado = estado;
        }

        public int getIdOrdenProduccion() { return idOrdenProduccion; }
        public void setIdOrdenProduccion(int idOrdenProduccion) { this.idOrdenProduccion = idOrdenProduccion; }

        public Producto getProducto() { return producto; }
        public void setProducto(Producto producto) { this.producto = producto; }

        public int getIdProducto() {
            return producto != null ? producto.getIdProducto() : 0;
        }

        public Empleado getEmpleado() { return empleado; }
        public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

        public int getIdEmpleado() {
            return empleado != null ? empleado.getIdEmpleado() : 0;
        }

        public BigDecimal getCantidadPlanificada() { return cantidadPlanificada; }
        public void setCantidadPlanificada(BigDecimal cantidadPlanificada) { this.cantidadPlanificada = cantidadPlanificada; }

        public Date getFechaProduccion() { return fechaProduccion; }
        public void setFechaProduccion(Date fechaProduccion) { this.fechaProduccion = fechaProduccion; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public Timestamp getFechaRegistro() { return fechaRegistro; }
        public void setFechaRegistro(Timestamp fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    }

