package com.mycompany.karbosimulacion;

public class Producto {
    public int id;
    public String Nombre;
    public String Descripcion;
    public double Precio;
    public String Foto_1, Foto_2;

    public Producto(int id, String Nombre, String Descripcion, double Precio, String Foto_1, String Foto_2) {
        this.id = id;
        this.Nombre = Nombre;
        this.Descripcion = Descripcion;
        this.Precio = Precio;
        this.Foto_1 = Foto_1;
        this.Foto_2 = Foto_2;
    }

    @Override
    public String toString() {
        return Nombre + " - €" + String.format("%.2f", Precio);
    }

}