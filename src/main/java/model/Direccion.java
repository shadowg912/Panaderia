package model;


class Pais {
     private int id_pais;
     private String nombre;

    public Pais(int id_pais, String nombre) {
        this.id_pais = id_pais;
        this.nombre = nombre;
    }

    public int getId_pais() {
        return id_pais;
    }

    public void setId_pais(int id_pais) {
        this.id_pais = id_pais;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}


class Provincia {
     private int id_provincia;
     private String nombre;
     private Pais pais;

    public Provincia(int id_provincia, String nombre, Pais pais) {
        this.id_provincia = id_provincia;
        this.nombre = nombre;
        this.pais = pais;
    }

    public int getId_provincia() {
        return id_provincia;
    }

    public void setId_provincia(int id_provincia) {
        this.id_provincia = id_provincia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }
}


class Ciudad {
    private  int id_ciudad;
    private   String nombre;
    private Provincia provincia;

    public Ciudad(int id_ciudad, String nombre, Provincia provincia) {
        this.id_ciudad = id_ciudad;
        this.nombre = nombre;
        this.provincia = provincia;
    }

    public int getId_ciudad() {
        return id_ciudad;
    }

    public void setId_ciudad(int id_ciudad) {
        this.id_ciudad = id_ciudad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }
}


class Sector {
     private int id_sector;
     private String nombre;
     private Ciudad ciudad;

    public Sector(int id_sector, String nombre, Ciudad ciudad) {
        this.id_sector = id_sector;
        this.nombre = nombre;
        this.ciudad = ciudad;
    }

    public int getId_sector() {
        return id_sector;
    }

    public void setId_sector(int id_sector) {
        this.id_sector = id_sector;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Ciudad getCiudad() {
        return ciudad;
    }

    public void setCiudad(Ciudad ciudad) {
        this.ciudad = ciudad;
    }
}


class Direccion {
    private  int id_direccion;
    private  String calle;
    private  int numero;
    private   String referencia;
    private   Sector sector;

    public Direccion(int id_direccion, String calle, int numero, String referencia, Sector sector) {
        this.id_direccion = id_direccion;
        this.calle = calle;
        this.numero = numero;
        this.referencia = referencia;
        this.sector = sector;
    }

    public int getId_direccion() {
        return id_direccion;
    }

    public void setId_direccion(int id_direccion) {
        this.id_direccion = id_direccion;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }
}