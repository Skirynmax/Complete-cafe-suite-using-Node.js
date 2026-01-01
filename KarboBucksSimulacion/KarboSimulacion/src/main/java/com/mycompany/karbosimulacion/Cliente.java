package com.mycompany.karbosimulacion;

import java.util.ArrayList;
import java.util.List;

public class Cliente {
    public String nombre;
    public List<Producto> pedido;

    public Cliente(String nombre) {
        this.nombre = nombre;
        this.pedido = new ArrayList<>();
    }
}
