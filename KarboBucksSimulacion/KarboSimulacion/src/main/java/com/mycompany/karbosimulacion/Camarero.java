package com.mycompany.karbosimulacion;

import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Camarero implements Runnable {
    private final Queue<Cliente> colaClientes;
    private final GestorApi gestorApi;
    private final Semaphore semaforo;

    public Camarero(Queue<Cliente> colaClientes, GestorApi gestorApi, Semaphore semaforo) {
        this.colaClientes = colaClientes;
        this.gestorApi = gestorApi;
        this.semaforo = semaforo;
    }

    @Override
    public void run() {
        while (true) {
            if (!colaClientes.isEmpty()) {
                try {
                    // Adquirir permiso del semáforo para atender al cliente
                    if (semaforo.availablePermits() == 0) {
                        System.out.println(
                                "[CAMARERO] Esperando a que el mostrador quede libre para el siguiente pedido...");
                    }
                    semaforo.acquire();
                    Cliente cliente = colaClientes.poll();
                    ProcesarPedido(cliente);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void ProcesarPedido(Cliente cliente) {
        System.out.println("[CAMARERO] Atendiendo a " + cliente.nombre);

        int numProductos = cliente.pedido.size();
        float tiempoEstimado = gestorApi.EjecutarCalculoTiempo(numProductos);
        System.out.println("[CAMARERO] Tiempo estimado calculado: " + tiempoEstimado);

        if (tiempoEstimado < 0) {
            System.err.println("[CAMARERO] Error calculando tiempo. Cancelando pedido.");
            semaforo.release(); // Liberar para que no se bloquee la cola por un error
            return;
        }

        try {
            System.out.println("[CAMARERO] Enviando pedido a API...");
            String id = gestorApi.CrearPedido(cliente, tiempoEstimado);
            if (id != null) {
                KarboSimulacion.idPedidoActual = id;
                System.out.println("[CAMARERO] Pedido de " + cliente.nombre + " creado con éxito. ID: " + id);
            } else {
                System.err.println("[CAMARERO] Error creando pedido.");
                semaforo.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
