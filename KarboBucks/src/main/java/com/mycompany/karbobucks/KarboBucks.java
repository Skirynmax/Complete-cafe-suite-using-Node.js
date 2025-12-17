/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.karbobucks;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author verkut
 */
public class KarboBucks extends JFrame {
    
    private static final String API_URL = "http://dam2.colexio-karbo.com:6001/api";    
    
    private JPanel panelPrincipal;
    
    public KarboBucks(){
        
       SetUpUI();
    }
    
     
    private void SetUpUI(){
        setTitle("KarboBucks - Sistema de Gestión");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        panelPrincipal = new PanelPrincipal(API_URL);//CREAMOS EL PANEL PRINCIPAL Y LE PASAMOS LA URL DE LA API
       
        add(panelPrincipal,new BorderLayout().CENTER);
    }

    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new KarboBucks().setVisible(true);
        });
    }
}
