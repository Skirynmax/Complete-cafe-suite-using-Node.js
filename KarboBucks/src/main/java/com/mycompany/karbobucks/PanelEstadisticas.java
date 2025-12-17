package com.mycompany.karbobucks;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PanelEstadisticas extends JPanel {

    private String API_URL;
    private Gson gson;
    private HttpClient httpClient;

    // Componentes de UI
    private JLabel lblTotalPedidos;
    private JLabel lblVentaTotal;
    private JPanel panelEstadoPedidos;
    private DefaultTableModel tableModelProductos;
    private JTable tableProductos;

    public PanelEstadisticas(Gson gson, HttpClient httpClient, String API_URL) {
        this.gson = gson;
        this.httpClient = httpClient;
        this.API_URL = API_URL;

        SetUp();
    }

    private void SetUp() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel superior - Resumen General
        JPanel panelResumen = crearPanelResumen();
        add(panelResumen, BorderLayout.NORTH);

        // Panel central - División entre pedidos por estado y productos más vendidos
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(250);

        // Panel de pedidos por estado
        JPanel panelEstado = crearPanelEstadoPedidos();
        splitPane.setTopComponent(panelEstado);

        // Panel de productos más vendidos
        JPanel panelProductos = crearPanelProductosMasVendidos();
        splitPane.setBottomComponent(panelProductos);

        add(splitPane, BorderLayout.CENTER);

        // Panel inferior - Botón de actualizar
        JPanel panelBotones = crearPanelBotones();
        add(panelBotones, BorderLayout.SOUTH);

        // Cargar datos iniciales
        CargarEstadisticas();
    }

    private JPanel crearPanelResumen() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBorder(BorderFactory.createTitledBorder("📊 Resumen General"));
        panel.setPreferredSize(new Dimension(0, 140)); // Tamaño del panel del resumen general

        // Tarjeta Total Pedidos
        JPanel cardPedidos = crearTarjetaEstadistica("Total de Pedidos", "0", new Color(13, 110, 253));
        lblTotalPedidos = (JLabel) ((JPanel) cardPedidos.getComponent(1)).getComponent(0);
        panel.add(cardPedidos);

        // Tarjeta Venta Total
        JPanel cardVentas = crearTarjetaEstadistica("Venta Total", "€0.00", new Color(25, 135, 84));
        lblVentaTotal = (JLabel) ((JPanel) cardVentas.getComponent(1)).getComponent(0);
        panel.add(cardVentas);

        return panel;
    }

    private JPanel crearTarjetaEstadistica(String titulo, String valorInicial, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));//creamos el panel
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panelValor = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelValor.setOpaque(false);
        JLabel lblValor = new JLabel(valorInicial);
        lblValor.setFont(new Font("Arial", Font.BOLD, 28));
        lblValor.setForeground(Color.WHITE);
        panelValor.add(lblValor);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(panelValor, BorderLayout.CENTER);

        return card;
    }

    private JPanel crearPanelEstadoPedidos() {
        JPanel panel = new JPanel(new BorderLayout());//creamos el panel
        panel.setBorder(BorderFactory.createTitledBorder("📋 Pedidos por Estado"));//titulo del panel

        panelEstadoPedidos = new JPanel();
        panelEstadoPedidos.setLayout(new BoxLayout(panelEstadoPedidos, BoxLayout.Y_AXIS));//layout vertical
        panelEstadoPedidos.setBorder(new EmptyBorder(10, 10, 10, 10));//bordes

        JScrollPane scrollPane = new JScrollPane(panelEstadoPedidos);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);//incremento de la barra de desplazamiento
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelProductosMasVendidos() {
        JPanel panel = new JPanel(new BorderLayout());//creamos el panel
        panel.setBorder(BorderFactory.createTitledBorder("🏆 Productos Más Vendidos"));//titulo del panel

        // Tabla de productos
        String[] columnas = { "Producto", "Cantidad Vendida", "Veces Ordenado", "Ingreso Total", "Precio Promedio" };
        tableModelProductos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableProductos = new JTable(tableModelProductos);
        tableProductos.setFont(new Font("Arial", Font.PLAIN, 12));
        tableProductos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tableProductos.setRowHeight(25);
        tableProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tableProductos);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));//creamos el panel

        JButton btnActualizar = new JButton("🔄 Actualizar Estadísticas");//boton de actualizar
        btnActualizar.setBackground(new Color(13, 110, 253));//color del boton
        btnActualizar.setForeground(Color.WHITE);//color del texto
        btnActualizar.setFocusPainted(false);//quitar el borde del boton
        btnActualizar.setOpaque(true);//poner el boton opaco
        btnActualizar.setBorderPainted(false);//quitar el borde del boton
        btnActualizar.setFont(new Font("Arial", Font.BOLD, 14));//fuente del texto
        btnActualizar.addActionListener(e -> CargarEstadisticas());

        panel.add(btnActualizar);

        return panel;
    }

    public void CargarEstadisticas() {
        // Ejecutar en hilos separados para no bloquear la UI
        new Thread(() -> {
            CargarResumen();
        }).start();

        new Thread(() -> {
            CargarProductosMasVendidos();
        }).start();
    }

    private void CargarResumen() {//llamada a la api para cargar el resumen
        try {
            String url = API_URL + "/pedidos/agregacion/resumen";//url de la api

            HttpRequest request = HttpRequest.newBuilder()//request a la api
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());//respuesta de la api

            System.out.println("Código de respuesta: " + response.statusCode());
            System.out.println("Cuerpo de respuesta: " + response.body());

            if (response.statusCode() == 200) {//resultado correcto
                JsonObject data = gson.fromJson(response.body(), JsonObject.class);//parseo de la respuesta

                System.out.println("Datos parseados: " + data.toString());

                int totalPedidos = data.get("totalPedidos").getAsInt();//total de pedidos
                double ventaTotal = data.get("ventaTotal").getAsDouble();//venta total
                JsonArray pedidosPorEstado = data.getAsJsonArray("pedidosPorEstado");//pedidos por estado recogidos en un array json


                // Actualizar UI
                SwingUtilities.invokeLater(() -> {
                    lblTotalPedidos.setText(String.valueOf(totalPedidos));
                    lblVentaTotal.setText(String.format("€%.2f", ventaTotal));

                    // Actualizar panel de estados
                    panelEstadoPedidos.removeAll();
                    for (int i = 0; i < pedidosPorEstado.size(); i++) {//recorrido de los pedidos por estado
                        JsonObject estado = pedidosPorEstado.get(i).getAsJsonObject();//estado actual
                        String nombreEstado = estado.get("_id").getAsString();//nombre del estado
                        int cantidad = estado.get("cantidad").getAsInt();//cantidad de pedidos
                        double totalVentas = estado.get("totalVentas").getAsDouble();//total de ventas

                        JPanel cardEstado = crearTarjetaEstado(nombreEstado, cantidad, totalVentas);//creacion de la tarjeta
                        panelEstadoPedidos.add(cardEstado);
                        panelEstadoPedidos.add(Box.createRigidArea(new Dimension(0, 10)));
                    }
                    panelEstadoPedidos.revalidate();//recarga el panel
                    panelEstadoPedidos.repaint();
                    System.out.println("UI actualizada correctamente");
                });
            } else {
                System.err.println("Error: código de respuesta " + response.statusCode());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Error al cargar el resumen. Código: " + response.statusCode(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        } catch (Exception e) {
            System.err.println("Excepción en CargarResumen:");
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Error al cargar el resumen: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private JPanel crearTarjetaEstado(String estado, int cantidad, double totalVentas) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        // Fondo gris claro en lugar de blanco para mejor contraste
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                new EmptyBorder(10, 15, 10, 15)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Icono y estado
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        String emoji = obtenerEmojiEstado(estado);
        JLabel lblEstado = new JLabel(emoji + " " + estado.toUpperCase());
        lblEstado.setFont(new Font("Arial", Font.BOLD, 14));
        lblEstado.setForeground(Color.BLACK); // Color negro para mejor contraste
        leftPanel.add(lblEstado);

        // Información
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));
        rightPanel.setOpaque(false);

        JLabel lblCantidad = new JLabel("Cantidad: " + cantidad);
        lblCantidad.setFont(new Font("Arial", Font.PLAIN, 12));
        lblCantidad.setForeground(new Color(52, 58, 64)); // Gris oscuro para mejor contraste

        JLabel lblVentas = new JLabel(String.format("Ventas: €%.2f", totalVentas));
        lblVentas.setFont(new Font("Arial", Font.PLAIN, 12));
        lblVentas.setForeground(new Color(0, 128, 0)); // Verde oscuro para mejor contraste

        rightPanel.add(lblCantidad);
        rightPanel.add(lblVentas);

        card.add(leftPanel, BorderLayout.WEST);
        card.add(rightPanel, BorderLayout.CENTER);

        return card;
    }

    private String obtenerEmojiEstado(String estado) {
        switch (estado.toLowerCase()) {
            case "pendiente":
                return "⏳";
            case "en preparacion":
                return "👨‍🍳";
            case "listo":
                return "✅";
            case "entregado":
                return "🎉";
            default:
                return "📦";
        }
    }

    private void CargarProductosMasVendidos() {//Llama a la api para cargar los productos mas vendidos
        try {
            String url = API_URL + "/pedidos/agregacion/productos-mas-vendidos";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Código de respuesta (productos): " + response.statusCode());
            System.out.println("Cuerpo de respuesta (productos): " + response.body());

            if (response.statusCode() == 200) {//resultado correcto
                JsonObject responseData = gson.fromJson(response.body(), JsonObject.class);//parseo de la respuesta
                JsonArray productos = responseData.getAsJsonArray("datos");//productos mas vendidos en jsonArray

                System.out.println("Cantidad de productos: " + productos.size());

                // Actualizar tabla
                SwingUtilities.invokeLater(() -> {
                    tableModelProductos.setRowCount(0);

                    for (int i = 0; i < productos.size(); i++) {
                        JsonObject producto = productos.get(i).getAsJsonObject();//producto mas vendido en json

                        String nombre = producto.get("producto").getAsString();//nombre del producto
                        int cantidadVendida = producto.get("cantidadVendida").getAsInt();//cantidad vendida
                        int vecesOrdenado = producto.get("vecesOrdenado").getAsInt();//veces ordenado
                        double ingresoTotal = producto.get("ingresoTotal").getAsDouble();//ingreso total
                        double precioPromedio = producto.get("precioPromedio").getAsDouble();//precio promedio

                        tableModelProductos.addRow(new Object[] {
                                nombre,
                                cantidadVendida,
                                vecesOrdenado,
                                String.format("€%.2f", ingresoTotal),
                                String.format("€%.2f", precioPromedio)
                        });
                    }
                    System.out.println("Tabla de productos actualizada correctamente");
                });
            } else {
                System.err.println("Error: código de respuesta (productos) " + response.statusCode());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Error al cargar productos más vendidos. Código: " + response.statusCode(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        } catch (Exception e) {
            System.err.println("Excepción en CargarProductosMasVendidos:");
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Error al cargar productos más vendidos: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
    }
}