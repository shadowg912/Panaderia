package model;

import javafx.beans.property.*;

public class Ingrediente {

    private final IntegerProperty idIngrediente = new SimpleIntegerProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty unidadMedida = new SimpleStringProperty();
    private final StringProperty categoriaNombre = new SimpleStringProperty();
    private final BooleanProperty activo = new SimpleBooleanProperty(true);

    public Ingrediente() {}

    public Ingrediente(int id, String nombre, String unidad, String categoria, boolean activo) {
        setIdIngrediente(id);
        setNombre(nombre);
        setUnidadMedida(unidad);
        setCategoriaNombre(categoria);
        setActivo(activo);
    }

    public int getIdIngrediente() { return idIngrediente.get(); }
    public void setIdIngrediente(int v) { idIngrediente.set(v); }
    public IntegerProperty idIngredienteProperty() { return idIngrediente; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String v) { nombre.set(v); }
    public StringProperty nombreProperty() { return nombre; }

    public String getUnidadMedida() { return unidadMedida.get(); }
    public void setUnidadMedida(String v) { unidadMedida.set(v); }
    public StringProperty unidadMedidaProperty() { return unidadMedida; }

    public String getCategoriaNombre() { return categoriaNombre.get(); }
    public void setCategoriaNombre(String v) { categoriaNombre.set(v); }
    public StringProperty categoriaNombreProperty() { return categoriaNombre; }

    public boolean isActivo() { return activo.get(); }
    public void setActivo(boolean v) { activo.set(v); }
    public BooleanProperty activoProperty() { return activo; }
}
