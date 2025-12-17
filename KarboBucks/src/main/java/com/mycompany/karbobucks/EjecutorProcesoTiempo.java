package com.mycompany.karbobucks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

// Clase completa que asegura ejecución sincrónica
public class EjecutorProcesoTiempo {
    
    // MÉTODO SINCRÓNICO - Espera a que termine el proceso
    public float getTiempoCalculado(int numeroProductos) {
        System.out.println("Iniciando cálculo de tiempo para " + numeroProductos + " productos...");
        float resultado = ejecutarCalculoTiempo(numeroProductos);
        System.out.println("Cálculo completado. Resultado: " + resultado);
        return resultado;
    }
    
    private float ejecutarCalculoTiempo(int numeroProductos) {
        try {
            // Obtener la ruta del proyecto actual (KarboBucks)
            String rutaClaseActual = this.getClass().getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();

            File archivoActual = new File(rutaClaseActual);
            File directorioKarboBucks;

            // Si es un JAR o clases compiladas
            if (archivoActual.isFile()) {
                directorioKarboBucks = archivoActual.getParentFile();
            } else {
                directorioKarboBucks = archivoActual;
            }

            // Subir hasta encontrar el directorio que contiene TiempoCalcular.class
            File directorioRaiz = directorioKarboBucks;
            
            while (directorioRaiz != null && !new File(directorioRaiz, "TiempoCalcular.class").exists()) {
                directorioRaiz = directorioRaiz.getParentFile();//Se consigue el directorio padre para seguir ascendiendo
            }

            if (directorioRaiz == null) {
                System.err.println("ERROR: No se pudo encontrar el directorio con TiempoCalcular.class");
                return -1;
            }

            System.out.println("Directorio encontrado: " + directorioRaiz.getAbsolutePath());//Se muestra en  que  directorio está la clase encargada de calcular el tiempo de pedido

            ProcessBuilder processBuilder;

            // El classpath debe apuntar al directorio raíz donde está TiempoCalcular
            //El processBuilder para ejecutar el proceso para calcular el tiempo del pedido
            processBuilder = new ProcessBuilder(
                    "java", "-cp", directorioRaiz.getAbsolutePath(),
                    "TiempoCalcular",
                    String.valueOf(numeroProductos)
            );
            

            // Establecer el directorio de trabajo en la raíz
            processBuilder.directory(directorioRaiz);
            

            // INICIAR EL PROCESO
            Process proceso = processBuilder.start();

            // Leer salida y errores en hilos separados para evitar deadlock
            StringBuilder salidaBuilder = new StringBuilder();
            StringBuilder errorBuilder = new StringBuilder();
            
            // Hilo para leer salida estándar
            Thread hiloSalida = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(proceso.getInputStream()))) {
                    String linea;
                    while ((linea = reader.readLine()) != null) {//RESULTADO LEíDO
                        System.out.println("TiempoCalcular salida: " + linea);
                        salidaBuilder.append(linea).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            
            // Hilo para leer errores
            Thread hiloError = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(proceso.getErrorStream()))) {
                    String linea;
                    while ((linea = reader.readLine()) != null) {//ERROR depuraci
                        System.err.println("TiempoCalcular error: " + linea);
                        errorBuilder.append(linea).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            
            hiloSalida.start();
            hiloError.start();

            // ESPERAR A QUE EL PROCESO TERMINE (BLOQUEANTE)
            int status = proceso.waitFor();
            System.out.println("Proceso terminado con código: " + status);
            
            // Esperar a que los hilos de lectura terminen
            hiloSalida.join();
            hiloError.join();

            if (status == 0) {//ÉXITO de ejecucción
                String salida = salidaBuilder.toString().trim();
                
                if (!salida.isEmpty()) {
                    // Obtener la última línea que debe contener el resultado
                    String[] lineas = salida.split("\n");
                    String ultimaLinea = lineas[lineas.length - 1].trim();
                    
                    try {
                        float resultado = Float.parseFloat(ultimaLinea);
                        System.out.println("Resultado parseado exitosamente: " + resultado);
                        return resultado;//DEVUELVE EL RESULTADO CALCULADO POR EL PROCESO EXTERNO
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear resultado: " + ultimaLinea);
                        return -1;
                    }
                } else {
                    System.err.println("No se recibió salida del proceso");
                    return -1;
                }
            } else {
                System.err.println("Proceso terminó con error. Código: " + status);
                if (errorBuilder.length() > 0) {
                    System.err.println("Errores:\n" + errorBuilder);
                }
                return -1;
            }

        } catch (Exception e) {
            System.err.println("Excepción al ejecutar TiempoCalcular:");
            e.printStackTrace();
            return -1;
        }
    }
}