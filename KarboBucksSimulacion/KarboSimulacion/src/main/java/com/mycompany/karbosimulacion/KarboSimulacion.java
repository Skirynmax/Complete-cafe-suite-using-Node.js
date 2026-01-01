package com.mycompany.karbosimulacion;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class KarboSimulacion {

    private static final Queue<Cliente> colaClientes = new ConcurrentLinkedQueue<>();
    private static final GestorApi gestorApi = new GestorApi();
    private static final Semaphore serviceSemaphore = new Semaphore(1);
    private static List<Producto> listaProductosDisponibles = new ArrayList<>();
    public static volatile String idPedidoActual = null;

    public static void main(String[] args) {
        System.out.println("--- Inicio de Simulación KarboBucks (Refactorizada) ---");

        // Cargar productos de la API antes de empezar
        try {
            Producto[] productos = gestorApi.getCarta();
            if (productos.length > 0) {
                listaProductosDisponibles = Arrays.asList(productos);
                System.out.println("[SISTEMA] Cargados " + listaProductosDisponibles.size() + " productos de la API.");
            } else {
                System.err.println(
                        "[SISTEMA] ADVERTENCIA: No se pudieron cargar productos de la API. Usando productos de reserva.");
                listaProductosDisponibles.add(new Producto(0, "Café Genérico", "Café de prueba", 2.0, null, null));
            }
        } catch (Exception e) {
            System.err.println("[SISTEMA] Error cargando carta: " + e.getMessage());
            listaProductosDisponibles.add(new Producto(-1, "Café Error", "Error de conexión", 0.0, null, null));
        }

        // Iniciar hilos
        Thread generadorClientes = new Thread(new GeneradorClientes());
        Thread camarero = new Thread(new Camarero(colaClientes, gestorApi, serviceSemaphore));
        Thread barista = new Thread(new Barista(gestorApi, serviceSemaphore));
        Thread camarero2 = new Thread(new CamareroBarra2(gestorApi, serviceSemaphore));

        generadorClientes.start();
        camarero.start();
        barista.start();
        camarero2.start();

        try {
            generadorClientes.join();
            camarero.join();
            barista.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // El generador de clientes se mantiene aquí como clase interna o podría
    // separarse
    static class GeneradorClientes implements Runnable {
        @Override
        public void run() {
            String[] nombres = { "Ana", "Carlos", "Beatriz", "David", "Elena", "Fernando", "Isabel", "Hugo" };
            Random random = new Random();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Simular tiempo entre llegada de clientes (15 a 50 segundos)
                    Thread.sleep(random.nextInt(50000) + 15000);

                    String nombre = nombres[random.nextInt(nombres.length)];
                    Cliente cliente = new Cliente(nombre);

                    // Crear un pedido aleatorio (1 a 3 productos) de la lista de productos reales
                    int numItems = random.nextInt(3) + 1;
                    for (int i = 0; i < numItems; i++) {
                        Producto pRandom = listaProductosDisponibles
                                .get(random.nextInt(listaProductosDisponibles.size()));
                        cliente.pedido.add(new Producto(pRandom.id, pRandom.Nombre, pRandom.Descripcion, pRandom.Precio,
                                pRandom.Foto_1, pRandom.Foto_2));
                    }

                    colaClientes.add(cliente);
                    System.out.println("[CLIENTE] " + nombre + " ha llegado y está en cola.");

                } catch (InterruptedException e) {
                    System.out.println("[CLIENTE] Generador de clientes detenido.");
                    break;
                }
            }
        }
    }
}
