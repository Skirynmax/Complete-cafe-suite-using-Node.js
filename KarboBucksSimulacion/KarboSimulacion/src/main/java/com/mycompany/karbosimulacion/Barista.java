package com.mycompany.karbosimulacion;

import java.util.concurrent.Semaphore;

public class Barista implements Runnable {
    private final GestorApi gestorApi;
    private final Semaphore semaforo;

    public Barista(GestorApi gestorApi, Semaphore semaforo) {
        this.gestorApi = gestorApi;
        this.semaforo = semaforo;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Pedido[] pedidos = gestorApi.getPedidos();
                String idActual = KarboSimulacion.idPedidoActual;

                if (idActual == null) {
                    // No hay pedido activo en la simulación, no hacemos nada
                } else {
                    for (Pedido p : pedidos) {
                        if (idActual.equals(p.getId())) {
                            if (p.estado == null || p.estado.equals("Pendiente") || p.estado.equals("Pedido")) {
                                procesarCicloBarista(p);
                            }
                        }
                    }
                }
                Thread.sleep(1000); // Polleo más frecuente para mayor reactividad (1s)
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void procesarCicloBarista(Pedido p) {
        String id = p.getId();
        // El usuario indica que tiempoEstimado está en minutos (Ej: 5 = 5 min = 300s)
        float tiempoSegundos = p.tiempoEstimado > 0 ? p.tiempoEstimado : 300.0f; // Fallback 5 min if no timehay tiempo
        float tiempoMinutos = tiempoSegundos / 60; // Convertir a minutos

        System.out.println("[BARISTA] Nuevo pedido recibido: " + p.nombre + " (" + id + ") - Tiempo estimado: "
                + tiempoMinutos + " min (" + tiempoSegundos + "s)");

        try {
            // 1. En preparación (80% del tiempo total)
            gestorApi.actualizarEstado(id, "En preparación");
            long msPrep = (long) (tiempoSegundos * 1000 * 0.8);
            System.out.println(
                    "[BARISTA] Pedido " + p.nombre + " -> En preparación (Simulando " + (msPrep / 1000.0) + "s)");
            Thread.sleep(msPrep);

            // 2. Listo para recoger (20% del tiempo total)
            gestorApi.actualizarEstado(id, "Listo para recoger");
            long msRecogida = (long) (tiempoSegundos * 1000 * 0.2);
            System.out.println("[BARISTA] Pedido " + p.nombre + " -> Listo para recoger (Simulando "
                    + (msRecogida / 1000.0) + "s)");
            Thread.sleep(msRecogida);

            System.out.println("[BARISTA] Pedido " + p.nombre + " listo.");

        } catch (Exception e) {
            System.err.println(
                    "[BARISTA] Error en el ciclo de preparación para " + p.nombre + " (" + id + "): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
