/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.karbobucks;

import com.google.gson.Gson;
import com.mycompany.karbobucks.ClasesAuxiliares.Pedido;
import com.mycompany.karbobucks.ClasesAuxiliares.Producto;
import java.awt.BorderLayout;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author verkut
 */
public class PanelPrincipal extends JPanel {

    private String API_URL;

    private JTabbedPane tabbedPane;
    private Gson gson;
    private HttpClient httpClient;

    public PanelPrincipal(String API_URL) {
        this.API_URL = API_URL;
        gson = new Gson();
       httpClient = HttpClient.newBuilder()//inicializa el cliente http
    .version(HttpClient.Version.HTTP_1_1) //Establezco version HTTP 1 para la conexión eficiente con el servidor node.js
    .connectTimeout(Duration.ofSeconds(10))//timeout de 10 segundos
    .build();
       
        SetUp();
    }

    private void SetUp() {

        tabbedPane = new JTabbedPane();

        // Pestaña del Camarero (Toma de pedidos)
        JPanel waiterPanel = new PanelCamarero(gson, httpClient, API_URL);
        tabbedPane.addTab("📝 Toma de Pedidos", waiterPanel);

        // Pestaña del Barista (Gestión de pedidos)
       PanelBarista baristaPanel = new PanelBarista(gson, httpClient, API_URL);
        tabbedPane.addTab("☕ Gestión Barista", baristaPanel);

        // Pestaña de Estadísticas
        PanelEstadisticas estadisticasPanel = new PanelEstadisticas(gson, httpClient, API_URL);
        tabbedPane.addTab("📊 Estadísticas", estadisticasPanel);

        // Listener para escuchar los cambios de pestaña
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String selectedTitle = tabbedPane.getTitleAt(selectedIndex);

            if (selectedTitle.equals("☕ Gestión Barista")) {
               baristaPanel.CargarPedidos();
            } else if (selectedTitle.equals("📊 Estadísticas")) {
                estadisticasPanel.CargarEstadisticas();
            }
        });

        setLayout(new BorderLayout());//establece el layout
        add(tabbedPane, BorderLayout.CENTER);//agrega la pestaña
    }

}
