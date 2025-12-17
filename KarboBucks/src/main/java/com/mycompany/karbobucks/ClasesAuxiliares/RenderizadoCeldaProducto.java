/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.karbobucks.ClasesAuxiliares;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author verkut
 */
public class RenderizadoCeldaProducto extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Producto) {
                Producto p = (Producto) value;
                setText("<html><b>" + p.Nombre + "</b><br>" + 
                       p.Foto_1 + " - €" + String.format("%.2f", p.Precio) + "</html>");
            }
            return this;
        }
    }