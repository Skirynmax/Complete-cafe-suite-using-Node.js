package com.example.karbobucksapp.models;

public class Producto {
        public int id;
        public String Nombre;
        public String Descripcion;
        public double Precio;
        public String Foto_1,Foto_2;
        

    public Producto() {
    }


    public Producto(int id, String nombre, String descripcion, double precio, String foto_1, String foto_2) {
        this.id = id;
        Nombre = nombre;
        Descripcion = descripcion;
        Precio = precio;
        Foto_1 = foto_1;
        Foto_2 = foto_2;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getNombre() {
        return Nombre;
    }


    public void setNombre(String nombre) {
        Nombre = nombre;
    }


    public String getDescripcion() {
        return Descripcion;
    }


    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }


    public double getPrecio() {
        return Precio;
    }


    public void setPrecio(double precio) {
        Precio = precio;
    }


    public String getFoto_1() {
        return Foto_1;
    }


    public void setFoto_1(String foto_1) {
        Foto_1 = foto_1;
    }


    public String getFoto_2() {
        return Foto_2;
    }


    public void setFoto_2(String foto_2) {
        Foto_2 = foto_2;
    }

  
}
