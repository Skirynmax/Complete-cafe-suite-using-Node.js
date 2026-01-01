package com.mycompany.karbosimulacion;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class Pedido {
    public String _id;
    public String nombre;
    public List<Producto> productos;
    public double totalPagar;
    public String estado;
    public float tiempoEstimado;
    public String fechaCreado;

    public String getId() {
        return _id != null ? _id : "N/A";
    }

    public String getFormattedTime() {
        if (fechaCreado != null) {
            return fechaCreado.substring(11, 16);
        }
        return "";
    }
}