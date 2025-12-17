/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.karbobucks;

import javax.swing.*;
import javax.swing.border.EmptyBorder; // Importar para los bordes de la tarjeta
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import com.google.gson.*;
import java.net.http.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import io.socket.client.IO;
import io.socket.client.Socket;
import com.mycompany.karbobucks.ClasesAuxiliares.*;

/**
 *
 * @author verkut
 */
public class PanelCamarero extends JPanel {
    private String API_URL;

    private Gson gson;
    private HttpClient httpClient;

    // Panel de Camarero
    private JTextField customerNameField;
    // Eliminamos productList y productListModel, los reemplazaremos por un JPanel
    private JPanel panelGridProductos; // Nuevo panel para el grid de productos
    //Visual
    private JList<Producto> listaVisualProductosPedidoActual;
    //Datos
    public DefaultListModel<Producto> listaDatosProductosPedidoActual;
    //Total
    private JLabel totalLabel;

    private EjecutorProcesoTiempo ejecutorTiempo;

    public PanelCamarero(Gson gson, HttpClient httpClient, String API_URL) {
        this.API_URL = API_URL;
        this.gson = gson;
        this.httpClient = httpClient;
        this.ejecutorTiempo = new EjecutorProcesoTiempo();
        SetUp();
    }

    private void SetUp() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior - Información del cliente
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Nombre del Cliente:"));
        customerNameField = new JTextField(20);
        topPanel.add(customerNameField);
        panel.add(topPanel, BorderLayout.NORTH);

        // Panel central - Productos (Grid) y pedido actual
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Grid de productos disponibles (Estilo TPV)
        JPanel panelProductos = new JPanel(new BorderLayout());
        panelProductos.setBorder(BorderFactory.createTitledBorder("Productos Disponibles (TPV)"));

        // Creamos el panel que contendrá las tarjetas en forma de cuadrícula
        panelGridProductos = new JPanel(new GridLayout(0, 3, 10, 10)); // 3 columnas, número de filas automático
        panelGridProductos.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane productsScroll = new JScrollPane(panelGridProductos);
        productsScroll.getVerticalScrollBar().setUnitIncrement(16); // Scroll suave
        panelProductos.add(productsScroll, BorderLayout.CENTER);


        // Lista de items del pedido actual (Se mantiene la JList por su funcionalidad
        // de scroll y eliminación)
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setBorder(BorderFactory.createTitledBorder("Pedido Actual"));
        listaDatosProductosPedidoActual = new DefaultListModel<>();
        listaVisualProductosPedidoActual = new JList<>(listaDatosProductosPedidoActual);//Se asigna la lista de datos a la lista visual
        // Utiliza el RenderizadoCeldaPedido original para la lista del pedido
        // listaVisualProductosPedidoActual.setCellRenderer(new
        JScrollPane orderScroll = new JScrollPane(listaVisualProductosPedidoActual);
        orderPanel.add(orderScroll, BorderLayout.CENTER);

        JPanel orderButtonsPanel = new JPanel(new FlowLayout());
        JButton removeButton = new JButton("➖ Eliminar Seleccionado");
        // Mejor contraste para el botón eliminar
        removeButton.setBackground(new Color(220, 53, 69)); // Rojo más vibrante
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.setOpaque(true);
        removeButton.setBorderPainted(false);
        removeButton.addActionListener(e -> EliminarProductoDelPedido());
        orderButtonsPanel.add(removeButton);
        orderPanel.add(orderButtonsPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(panelProductos);
        splitPane.setRightComponent(orderPanel);
        panel.add(splitPane, BorderLayout.CENTER);

        // Panel inferior - Total y botones
        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: €0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        bottomPanel.add(totalLabel, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Botón limpiar con mejor contraste
        JButton clearButton = new JButton("🗑️ Limpiar");
        clearButton.setBackground(new Color(108, 117, 125)); // Gris más oscuro
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setOpaque(true);
        clearButton.setBorderPainted(false);
        clearButton.addActionListener(e -> LimpiarPedido());

        // Botón enviar con mejor contraste
        JButton submitButton = new JButton("✅ Enviar Pedido");
        submitButton.setBackground(new Color(25, 135, 84)); // Verde más oscuro para mejor contraste
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setOpaque(true);
        submitButton.setBorderPainted(false);
        submitButton.addActionListener(e -> EnviarPedido());

        buttonsPanel.add(clearButton);
        buttonsPanel.add(submitButton);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        CargarProductos();

        this.setLayout(new BorderLayout());
        this.add(panel, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> {
            // Esto asegura que la división se establece después de que el panel está
            // dimensionado
            splitPane.setDividerLocation(0.75); // Asignar el 75% al panel de productos
        });
    }

    private void CargarProductos() {
        new Thread(() -> {//nuevo hilo
            try {
                HttpRequest request = HttpRequest.newBuilder()//request a la api para recoger la carta
                        .uri(URI.create(API_URL + "/carta"))
                        .GET()
                        .build();
                System.out.println(request.toString());
                

                HttpResponse<String> response = httpClient.send(request,//respuesta de la api recogida
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {//exito y procesa la info recogida
                    Producto[] products = gson.fromJson(response.body(), Producto[].class);

                    SwingUtilities.invokeLater(() -> {
                        panelGridProductos.removeAll(); // Limpiar el grid antes de añadir
                        for (Producto p : products) {//pedido a pedido
                            TarjetaProducto tarjeta = new TarjetaProducto(p, this);
                            // Añadir cada producto como una tarjeta al grid
                            panelGridProductos.add(tarjeta);
                        }
                        panelGridProductos.revalidate();
                        panelGridProductos.repaint();
                        System.out.println("Productos cargados y renderizados en TPV: " + products.length);
                    });
                } else {
                    System.err.println("Error al cargar productos. Status: " + response.statusCode());
                }
            } catch (Exception e) {
                System.err.println("Error en loadProducts:");
                e.printStackTrace();
            }
        }).start();
    }

    private void EliminarProductoDelPedido() {
        int selectedIndex = listaVisualProductosPedidoActual.getSelectedIndex();
        if (selectedIndex != -1) {//elimina el producto seleccionado de la lista visual y de la lista de datos
            listaDatosProductosPedidoActual.remove(selectedIndex);
            ActualizarTotal();
        }
    }

    public void ActualizarTotal() {//actualiza el total del pedido
        double total = 0;
        for (int i = 0; i < listaDatosProductosPedidoActual.size(); i++) {//recorre la lista de datos
            total += listaDatosProductosPedidoActual.getElementAt(i).Precio;//suma el precio de cada producto
        }
        totalLabel.setText(String.format("Total: €%.2f", total));//actualiza el label del total
    }

    private void LimpiarPedido() {//limpia el pedido
        customerNameField.setText("");//limpia el campo de texto
        listaDatosProductosPedidoActual.clear();//limpia la lista de datos
        ActualizarTotal();//actualiza el total
    }

    private void EnviarPedido() {//envia el pedido
        String nombreCliente = customerNameField.getText().trim();
        if (nombreCliente.isEmpty()) {//si el nombre del cliente esta vacio
            JOptionPane.showMessageDialog(this,
                    "Por favor ingrese el nombre del cliente",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (listaDatosProductosPedidoActual.isEmpty()) {//si la lista de datos esta vacia
            JOptionPane.showMessageDialog(this,
                    "El pedido está vacío",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Deshabilitar botón para evitar múltiples clicks
        JButton submitButton = (JButton) ((Component) SwingUtilities.getAncestorOfClass(JButton.class,
                customerNameField));
        if (submitButton != null) {
            submitButton.setEnabled(false);
        }

        new Thread(() -> {//nuevo hilo
            try {

                // 1. CALCULAR TIEMPO (BLOQUEANTE - ESPERA AQUÍ)
                int longitudLista = listaDatosProductosPedidoActual.size();//obtiene la cantidad de productos en el pedido
                System.out.println("Calculando tiempo para " + longitudLista + " productos...");//imprime la cantidad de productos


                float tiempoEntrega = ejecutorTiempo.getTiempoCalculado(longitudLista);//obtiene el tiempo de entrega llamando al proceso externo

                System.out.println("Tiempo calculado: " + tiempoEntrega);

                if (tiempoEntrega < 0) {//si el tiempo es negativo
                    SwingUtilities.invokeLater(() -> {//invoca la interfaz grafica
                        JOptionPane.showMessageDialog(this,//muestra un mensaje de error
                                "Error al calcular tiempo de entrega",//mensaje de error
                                "Error", JOptionPane.ERROR_MESSAGE);//tipo de mensaje
                        if (submitButton != null)
                            submitButton.setEnabled(true);//habilita el boton
                    });
                    return;
                }

                // 2. PREPARAR DATOS
                System.out.println("Preparando datos del pedido...");
                List<Producto> items = new ArrayList<>();//lista de productos
                double total = 0;
                for (int i = 0; i < listaDatosProductosPedidoActual.size(); i++) {//recorre la lista de datos
                    Producto item = listaDatosProductosPedidoActual.getElementAt(i);//obtiene el producto
                    items.add(item);//añade el producto a la lista
                    total += item.Precio;//suma el precio del producto
                }

                JsonObject orderData = new JsonObject();//crea un json para el pedido
                orderData.addProperty("tiempoEntrega", tiempoEntrega);//agrega el tiempo de entrega
                orderData.addProperty("nombre", nombreCliente);//agrega el nombre del cliente
                orderData.addProperty("totalPagar", total);//agrega el total a pagar
                JsonArray itemsArray = gson.toJsonTree(items).getAsJsonArray();//crea un array de productos
                orderData.add("productos", itemsArray);//agrega el array de productos

                System.out.println("Datos preparados: " + orderData.toString());

                // 3. ENVIAR A LA API
                System.out.println("Enviando a la API...");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL + "/pedidos/crear"))//url de la api
                        .header("Content-Type", "application/json")//tipo de contenido
                        .POST(HttpRequest.BodyPublishers.ofString(orderData.toString()))//Método POST para subir los datos a la api en json
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());//respuesta de la api

                System.out.println("Respuesta API: " + response.statusCode());//imprime el codigo de respuesta

                SwingUtilities.invokeLater(() -> {
                    if (response.statusCode() == 201) {//Si hay exito
                        JOptionPane.showMessageDialog(this,
                                "¡Pedido enviado con éxito!\n" +
                                        "Tiempo de entrega: " + tiempoEntrega + " minutos",
                                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        LimpiarPedido();
                    } else {//si hay un error
                        JOptionPane.showMessageDialog(this,
                                "Error al enviar pedido: " + response.body(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    if (submitButton != null)
                        submitButton.setEnabled(true);
                });


            } catch (Exception e) {
                System.err.println("Error en submitOrder:");
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Error: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    if (submitButton != null)
                        submitButton.setEnabled(true);
                });
            }
        }).start();
        
    }
}