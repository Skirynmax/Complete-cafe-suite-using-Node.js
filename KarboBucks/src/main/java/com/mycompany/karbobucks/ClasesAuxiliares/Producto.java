/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.karbobucks.ClasesAuxiliares;

/**
 *
 * @author verkut
 */
public class Producto {
        public int id;
        public String Nombre;
        public String Descripcion;
        public double Precio;
        public String Foto_1,Foto_2;
        
        @Override
        public String toString() {
            return Nombre + " - €" + String.format("%.2f", Precio);
        }
        
        
    }