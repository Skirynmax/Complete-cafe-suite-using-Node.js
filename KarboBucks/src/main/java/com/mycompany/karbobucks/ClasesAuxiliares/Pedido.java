/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.karbobucks.ClasesAuxiliares;

import java.util.List;


/**
 *
 * @author verkut
 */
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
