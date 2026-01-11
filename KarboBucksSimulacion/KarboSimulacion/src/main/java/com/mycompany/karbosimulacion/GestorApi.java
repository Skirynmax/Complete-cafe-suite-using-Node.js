package com.mycompany.karbosimulacion;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GestorApi {
    private static final String API_URL = "http://dam2.colexio-karbo.com:6001/api";
    private final HttpClient httpClient;
    private final Gson gson;

    public GestorApi() {//Preparar la version httpclient v1.1 para evitar fallos al conectarse a la api
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public Pedido[] ObtenerPedidos() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/pedidos"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), Pedido[].class);
        }
        return new Pedido[0];
    }

    public Producto[] ObtenerCarta() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/carta"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), Producto[].class);
        }
        return new Producto[0];
    }

    public String CrearPedido(Cliente cliente, float tiempoEstimado) throws Exception {
        JsonObject orderData = new JsonObject();
        orderData.addProperty("tiempoEntrega", tiempoEstimado);
        orderData.addProperty("nombre", cliente.nombre);

        double total = 0;
        for (Producto p : cliente.pedido) {
            total += p.Precio;
        }
        orderData.addProperty("totalPagar", total);

        JsonArray itemsArray = gson.toJsonTree(cliente.pedido).getAsJsonArray();
        orderData.add("productos", itemsArray);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/pedidos/crear"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(orderData.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            // El API devuelve {"mensaje": "...", "id": "...", "pedido": {...}}
            JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
            if (responseJson.has("id")) {
                return responseJson.get("id").getAsString();
            }
        }
        return null;
    }

    public void ActualizarEstado(String id, String estado) throws Exception {
        JsonObject statusData = new JsonObject();
        statusData.addProperty("estado", estado);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/pedidos/" + id + "/estado"))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(statusData.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println(
                    "[API] Error actualizando estado de " + id + " a " + estado + ". Status: " + response.statusCode());
        }
    }

    public float EjecutarCalculoTiempo(int numeroProductos) {
        System.out.println("Iniciando cálculo de tiempo para " + numeroProductos + " productos...");
        try {
            String rutaClaseActual = GestorApi.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();

            File archivoActual = new File(rutaClaseActual);
            File directorioKarboBucks;

            if (archivoActual.isFile()) {
                directorioKarboBucks = archivoActual.getParentFile();
            } else {
                directorioKarboBucks = archivoActual;
            }

            File directorioRaiz = directorioKarboBucks;
            int maxUp = 5;
            while (directorioRaiz != null && maxUp > 0 && !new File(directorioRaiz, "TiempoCalcular.class").exists()) {
                directorioRaiz = directorioRaiz.getParentFile();
                maxUp--;
            }

            if (directorioRaiz == null || !new File(directorioRaiz, "TiempoCalcular.class").exists()) {
                System.err.println("ADVERTENCIA: No encontrado TiempoCalcular.class en ruta relativa standard.");
            }

            if (directorioRaiz == null) {
                System.err.println("ERROR: No se pudo encontrar el directorio con TiempoCalcular.class");
                return -1;
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java", "-cp", directorioRaiz.getAbsolutePath(),
                    "TiempoCalcular",
                    String.valueOf(numeroProductos));

            processBuilder.directory(directorioRaiz);
            Process proceso = processBuilder.start();

            StringBuilder salidaBuilder = new StringBuilder();
            Thread hiloSalida = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()))) {
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        salidaBuilder.append(linea).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            hiloSalida.start();
            int status = proceso.waitFor();
            hiloSalida.join();

            if (status == 0) {
                String salida = salidaBuilder.toString().trim();
                if (!salida.isEmpty()) {
                    String[] lineas = salida.split("\n");
                    String ultimaLinea = lineas[lineas.length - 1].trim();
                    try {
                        return Float.parseFloat(ultimaLinea);
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                }
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
