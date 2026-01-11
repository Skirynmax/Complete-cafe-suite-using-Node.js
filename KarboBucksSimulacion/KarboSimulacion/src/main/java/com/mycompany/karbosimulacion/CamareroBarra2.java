package com.mycompany.karbosimulacion;

import java.util.concurrent.Semaphore;

public class CamareroBarra2 implements Runnable {
    private final GestorApi gestorApi;
    private final Semaphore semaforo;

    public CamareroBarra2(GestorApi gestorApi, Semaphore semaforo) {
        this.gestorApi = gestorApi;
        this.semaforo = semaforo;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Pedido[] pedidos = gestorApi.ObtenerPedidos();
                for (Pedido p : pedidos) {
                    // Solo procesar si es el pedido activo y está listo para recoger
                    if (p.ObtenerId().equals(KarboSimulacion.idPedidoActual) && "Listo para recoger".equals(p.estado)) {
                        ProcesarPagoYEntrega(p);
                        KarboSimulacion.idPedidoActual = null; // Limpiar para el siguiente
                        break;
                    }
                }
                Thread.sleep(1500); // Polleo para la entrega
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ProcesarPagoYEntrega(Pedido p) {
        String id = p.ObtenerId();
        System.out.println("[CAMARERO 2] Iniciando cobro para " + p.nombre + " (Total: €"
                + String.format("%.2f", p.totalPagar) + ")");

        try {
            // Simular tiempo de cobro (3-5 segundos)
            Thread.sleep(3000);
            System.out.println("[CAMARERO 2] Pago recibido de " + p.nombre + ".");

            // Finalizar pedido
            gestorApi.ActualizarEstado(id, "Recogido");
            System.out.println("[CAMARERO 2] Pedido " + p.nombre + " -> Recogido y entregado.");

            // Liberar permiso del semáforo para permitir el siguiente cliente
            System.out.println("[SISTEMA] Cliente finalizado. Liberando puesto...");
            semaforo.release();
        } catch (Exception e) {
            System.err.println("[CAMARERO 2] Error en cobro/entrega: " + e.getMessage());
            semaforo.release(); // Evitar bloqueo
        }
    }
}
