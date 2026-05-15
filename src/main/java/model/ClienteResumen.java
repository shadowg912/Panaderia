package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ClienteResumen {
    private final SimpleIntegerProperty idCliente;
    private final SimpleStringProperty razonSocial;
    private final SimpleStringProperty rnc;
    private final SimpleStringProperty telefono;
    private final SimpleStringProperty provincia;

    public ClienteResumen() {
        this.idCliente = new SimpleIntegerProperty(0);
        this.razonSocial = new SimpleStringProperty("");
        this.rnc = new SimpleStringProperty("");
        this.telefono = new SimpleStringProperty("");
        this.provincia = new SimpleStringProperty("");
    }

    public ClienteResumen(int idCliente, String razonSocial, String rnc, String telefono, String provincia) {
        this.idCliente = new SimpleIntegerProperty(idCliente);
        this.razonSocial = new SimpleStringProperty(razonSocial);
        this.rnc = new SimpleStringProperty(rnc != null ? rnc : "");
        this.telefono = new SimpleStringProperty(telefono != null ? telefono : "");
        this.provincia = new SimpleStringProperty(provincia != null ? provincia : "");
    }

    public int getIdCliente() { return idCliente.get(); }
    public void setIdCliente(int v) { idCliente.set(v); }
    public SimpleIntegerProperty idClienteProperty() { return idCliente; }

    public String getRazonSocial() { return razonSocial.get(); }
    public void setRazonSocial(String v) { razonSocial.set(v); }
    public SimpleStringProperty razonSocialProperty() { return razonSocial; }

    public String getRnc() { return rnc.get(); }
    public void setRnc(String v) { rnc.set(v); }
    public SimpleStringProperty rncProperty() { return rnc; }

    public String getTelefono() { return telefono.get(); }
    public void setTelefono(String v) { telefono.set(v); }
    public SimpleStringProperty telefonoProperty() { return telefono; }

    public String getProvincia() { return provincia.get(); }
    public void setProvincia(String v) { provincia.set(v); }
    public SimpleStringProperty provinciaProperty() { return provincia; }
}