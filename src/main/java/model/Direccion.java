package model;


class Pais {
     private int id_pais;
     private String nombre;
}


class Provincia {
     private int id_provincia;
     private String nombre;
     private Pais pais;
}


class Ciudad {
    private  int id_ciudad;
    private   String nombre;
    private Provincia provincia;
}


class Sector {
     private int id_sector;
     private String nombre;
     private Ciudad ciudad;
}


class Direccion {
    private  int id_direccion;
    private  String calle;
    private  int numero;
    private   String referencia;
    private   Sector sector;
}