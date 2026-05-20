package model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class MovimientoInventario {

    private final SimpleIntegerProperty idMovimiento;
    private final SimpleIntegerProperty idProducto;
    private final SimpleStringProperty nombreProducto;
    private final SimpleDoubleProperty cantidad;
    private final SimpleStringProperty tipoNaturaleza;
    private final SimpleStringProperty tipoNombre;
    private final SimpleStringProperty fechaDisplay;
    private final SimpleStringProperty usuarioNombre;
    private final SimpleDoubleProperty stockResultante;

    public MovimientoInventario() {
        idMovimiento = new SimpleIntegerProperty(0);
        idProducto = new SimpleIntegerProperty(0);
        nombreProducto = new SimpleStringProperty("");
        cantidad = new SimpleDoubleProperty(0);
        tipoNaturaleza = new SimpleStringProperty("");
        tipoNombre = new SimpleStringProperty("");
        fechaDisplay = new SimpleStringProperty("");
        usuarioNombre = new SimpleStringProperty("");
        stockResultante = new SimpleDoubleProperty(0);
    }

    public int getIdMovimiento() { return idMovimiento.get(); }
    public void setIdMovimiento(int v) { idMovimiento.set(v); }
    public SimpleIntegerProperty idMovimientoProperty() { return idMovimiento; }

    public int getIdProducto() { return idProducto.get(); }
    public void setIdProducto(int v) { idProducto.set(v); }
    public SimpleIntegerProperty idProductoProperty() { return idProducto; }

    public String getNombreProducto() { return nombreProducto.get(); }
    public void setNombreProducto(String v) { nombreProducto.set(v); }
    public SimpleStringProperty nombreProductoProperty() { return nombreProducto; }

    public double getCantidad() { return cantidad.get(); }
    public void setCantidad(double v) { cantidad.set(v); }
    public SimpleDoubleProperty cantidadProperty() { return cantidad; }

    public String getTipoNaturaleza() { return tipoNaturaleza.get(); }
    public void setTipoNaturaleza(String v) { tipoNaturaleza.set(v); }
    public SimpleStringProperty tipoNaturalezaProperty() { return tipoNaturaleza; }

    public String getTipoNombre() { return tipoNombre.get(); }
    public void setTipoNombre(String v) { tipoNombre.set(v); }
    public SimpleStringProperty tipoNombreProperty() { return tipoNombre; }

    public String getFechaDisplay() { return fechaDisplay.get(); }
    public void setFechaDisplay(String v) { fechaDisplay.set(v); }
    public SimpleStringProperty fechaDisplayProperty() { return fechaDisplay; }

    public String getUsuarioNombre() { return usuarioNombre.get(); }
    public void setUsuarioNombre(String v) { usuarioNombre.set(v); }
    public SimpleStringProperty usuarioNombreProperty() { return usuarioNombre; }

    public double getStockResultante() { return stockResultante.get(); }
    public void setStockResultante(double v) { stockResultante.set(v); }
    public SimpleDoubleProperty stockResultanteProperty() { return stockResultante; }
}
