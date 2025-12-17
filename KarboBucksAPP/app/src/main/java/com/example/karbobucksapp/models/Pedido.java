package com.example.karbobucksapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Pedido implements Serializable {
    @SerializedName("_id")
    private String id;
    private String nombre;
    private List<ProductItem> productos;
    private double totalPagar;
    private String estado;
    private String fechaCreado,fechaActualizacion;
    private int tiempoEstimado;

    public Pedido() {
    }



    public Pedido(String id, String nombre, List<Pedido.ProductItem> productos, double totalPagar, String estado,
                  String fechaCreado, String fechaActualizacion, int tiempoEstimado) {
        this.id = id;
        this.nombre = nombre;
        this.productos = productos;
        this.totalPagar = totalPagar;
        this.estado = estado;
        this.fechaCreado = fechaCreado;
        this.fechaActualizacion = fechaActualizacion;
        this.tiempoEstimado = tiempoEstimado;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<ProductItem> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductItem> productos) {
        this.productos = productos;
    }

    public double getTotalPagar() {
        return totalPagar;
    }

    public void setTotalPagar(double totalPagar) {
        this.totalPagar = totalPagar;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaCreado() {
        return fechaCreado;
    }

    public void setFechaCreado(String fechaCreado) {
        this.fechaCreado = fechaCreado;
    }

    public int getTiempoEstimado() {
        return tiempoEstimado;
    }

    public void setTiempoEstimado(int tiempoEstimado) {
        this.tiempoEstimado = tiempoEstimado;
    }

    public static class ProductItem implements Serializable {
        private String Nombre;
        private double Precio;

        public ProductItem() {
        }

        public ProductItem(String nombre, double precio) {
            this.Nombre = nombre;
            this.Precio = precio;
        }

        public String getNombre() {
            return Nombre;
        }

        public void setNombre(String nombre) {
            this.Nombre = nombre;
        }

        public double getPrecio() {
            return Precio;
        }

        public void setPrecio(double precio) {
            this.Precio = precio;
        }
    }
}