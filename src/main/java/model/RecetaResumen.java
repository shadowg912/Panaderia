package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class RecetaResumen {
    private final SimpleIntegerProperty idProducto;
    private final SimpleStringProperty nombreProducto;
    private final SimpleIntegerProperty numIngredientes;

    public RecetaResumen() {
        this.idProducto = new SimpleIntegerProperty(0);
        this.nombreProducto = new SimpleStringProperty("");
        this.numIngredientes = new SimpleIntegerProperty(0);
    }

    public RecetaResumen(int idProducto, String nombreProducto, int numIngredientes) {
        this.idProducto = new SimpleIntegerProperty(idProducto);
        this.nombreProducto = new SimpleStringProperty(nombreProducto);
        this.numIngredientes = new SimpleIntegerProperty(numIngredientes);
    }

    public int getIdProducto() { return idProducto.get(); }
    public void setIdProducto(int value) { idProducto.set(value); }
    public SimpleIntegerProperty idProductoProperty() { return idProducto; }

    public String getNombreProducto() { return nombreProducto.get(); }
    public void setNombreProducto(String value) { nombreProducto.set(value); }
    public SimpleStringProperty nombreProductoProperty() { return nombreProducto; }

    public int getNumIngredientes() { return numIngredientes.get(); }
    public void setNumIngredientes(int value) { numIngredientes.set(value); }
    public SimpleIntegerProperty numIngredientesProperty() { return numIngredientes; }
}