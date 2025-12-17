/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.karbobucks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mycompany.karbobucks.ClasesAuxiliares.Pedido;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author verkut
 */
public class PanelBarista extends JPanel {
    private String API_URL;

    private Gson gson;
    private HttpClient httpClient;

    // Panel de Barista
    private JTable tablaPedidos;
    //Datos
    private DefaultTableModel tablaDatosPedidos;
    //Visual
    private List<Pedido> pedidosActivos;

    public PanelBarista(Gson gson, HttpClient httpClient, String API_URL) {
        this.API_URL = API_URL;
        this.gson = gson;
        this.httpClient = httpClient;
        pedidosActivos = new ArrayList<>();
        SetUp();
    }

    private void SetUp() {
        // Configurar el layout principal PRIMERO
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tabla de pedidos
        String[] columns = { "ID", "Cliente", "Artículos", "Total", "Estado", "Tiempo Est.", "Hora" };
        tablaDatosPedidos = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPedidos = new JTable(tablaDatosPedidos);
        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPedidos.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(tablaPedidos);

        // Añadir directamente a THIS, no a un panel intermedio
        this.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Botón "En Preparación" con mejor contraste
        JButton preparingButton = new JButton("🔄 En Preparación");
        preparingButton.setBackground(new Color(255, 140, 0)); // Naranja más oscuro (Dark Orange)
        preparingButton.setForeground(Color.WHITE);
        preparingButton.setFocusPainted(false);
        preparingButton.setOpaque(true);
        preparingButton.setBorderPainted(false);
        preparingButton.addActionListener(e -> ActualizarEstadoPedido("En preparación"));

        // Botón "Listo para Recoger" con mejor contraste
        JButton readyButton = new JButton("✓ Listo para Recoger");
        readyButton.setBackground(new Color(30, 100, 150)); // Azul más oscuro
        readyButton.setForeground(Color.WHITE);
        readyButton.setFocusPainted(false);
        readyButton.setOpaque(true);
        readyButton.setBorderPainted(false);
        readyButton.addActionListener(e -> ActualizarEstadoPedido("Listo para recoger"));

        // Botón "Recogido" con mejor contraste
        JButton pickedButton = new JButton("✔ Recogido");
        pickedButton.setBackground(new Color(25, 135, 84)); // Verde más oscuro (igual que el botón de enviar)
        pickedButton.setForeground(Color.WHITE);
        pickedButton.setFocusPainted(false);
        pickedButton.setOpaque(true);
        pickedButton.setBorderPainted(false);
        pickedButton.addActionListener(e -> ActualizarEstadoPedido("Recogido"));

        // Botón "Actualizar" con mejor contraste
        JButton refreshButton = new JButton("🔄 Actualizar");
        refreshButton.setBackground(new Color(108, 117, 125)); // Gris más oscuro
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setOpaque(true);
        refreshButton.setBorderPainted(false);
        refreshButton.addActionListener(e -> CargarPedidos());

        // Botón "Eliminar Pedido"
        JButton deleteButton = new JButton("🗑 Eliminar Pedido");
        deleteButton.setBackground(new Color(220, 53, 69)); // Rojo Bootstrap
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setOpaque(true);
        deleteButton.setBorderPainted(false);
        deleteButton.addActionListener(e -> EliminarPedido());

        buttonsPanel.add(preparingButton);
        buttonsPanel.add(readyButton);
        buttonsPanel.add(pickedButton);
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(deleteButton);

        // Añadir botones directamente a THIS
        this.add(buttonsPanel, BorderLayout.SOUTH);

        CargarPedidos();
    }

    public void CargarPedidos() {//CARGAMOS LOS PEDIDOS GUARDADOS LLAMANDO AL API 
        new Thread(() -> {//NUEVO HILO 
            try {
                HttpRequest request = HttpRequest.newBuilder()//Creamos http rquest
                        .uri(URI.create(API_URL + "/pedidos"))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,//String respuesta a la peticion
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {//respuesta exitosa
                    Pedido[] orders = gson.fromJson(response.body(), Pedido[].class);//Transforma de JSON a Objeto los pedidos
                    SwingUtilities.invokeLater(() -> {
                        tablaDatosPedidos.setRowCount(0);
                        pedidosActivos.clear();

                        for (Pedido order : orders) {//Cargar los pedidos en la tabla
                            if (!order.estado.equals("Recogido")) {
                                pedidosActivos.add(order);
                                tablaDatosPedidos.addRow(new Object[] {
                                        order.getId(),
                                        order.nombre,
                                        order.productos.size() + " items",
                                        String.format("€%.2f", order.totalPagar),
                                        order.estado,
                                        (int)order.tiempoEstimado/60 + " min",//para que se vea en minutos el tiempo que se tardará en completar el encargo
                                        order.getFormattedTime()
                                });
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void ActualizarEstadoPedido(String nuevoEstado) {
        int selectedRow = tablaPedidos.getSelectedRow();//Selecciona la fila de la tabla
        if (selectedRow == -1) {//Si no hay algo seleccionado
            JOptionPane.showMessageDialog(this,
                    "Seleccione un pedido",
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Pedido order = pedidosActivos.get(selectedRow);//Coge el pedido con el id de la fila correspondiente

        JsonObject statusData = new JsonObject();
        statusData.addProperty("estado", nuevoEstado);//Creamos objeto JSON con la info del estado

        new Thread(() -> {//Nuevo hilo
            try {
                HttpRequest request = HttpRequest.newBuilder()//HTTP Request
                        .uri(URI.create(API_URL + "/pedidos/" + order.getId() + "/estado"))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(statusData.toString()))
                        .build();
                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());//Respuesta

                SwingUtilities.invokeLater(() -> {
                    if (response.statusCode() == 200) {
                        CargarPedidos();//Recargamos pedidos
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void EliminarPedido() {
        int selectedRow = tablaPedidos.getSelectedRow();//selecciona la fila
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un pedido para eliminar",
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Pedido order = pedidosActivos.get(selectedRow);//Recoge el pedido indicado por la fila

        // Confirmación antes de eliminar
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar el pedido de " + order.nombre + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()//Request a la api
                        .uri(URI.create(API_URL + "/pedidos/" + order.getId()))
                        .DELETE()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                SwingUtilities.invokeLater(() -> {
                    if (response.statusCode() == 200) {//SE ELIMINÓ el pedido
                        JOptionPane.showMessageDialog(this,
                                "Pedido eliminado correctamente",
                                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        CargarPedidos();//Recarga tabla
                    } else if (response.statusCode() == 404) {
                        JOptionPane.showMessageDialog(this,
                                "Pedido no encontrado",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Error al eliminar el pedido",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Error de conexión: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
}