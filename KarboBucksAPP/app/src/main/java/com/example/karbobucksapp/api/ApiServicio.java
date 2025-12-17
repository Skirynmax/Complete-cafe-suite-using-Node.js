package com.example.karbobucksapp.api;

import com.example.karbobucksapp.models.Pedido;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;



public interface ApiServicio {

    // Endpoints de Pedidos
    @GET("pedidos")
    Call<List<Pedido>> obtenerPedidos();

    @GET("pedidos/satisfaccion/global")
    Call<GlobalSatisfactionResponse> obtenerPromedioGlobal();

    // Endpoint para enviar satisfacción
    @POST("pedidos/satisfaccion")
    Call<SatisfactionResponse> submitSatisfaction(@Body SatisfactionRequest request);

    // Clases Request
    
    class SatisfactionRequest {
        public String nombre;
        public int satisfaccion;

        public SatisfactionRequest(String nombre, int satisfaccion) {
            this.nombre = nombre;
            this.satisfaccion = satisfaccion;
        }
    }


    //Clases de las respuestas
    
    class SatisfactionResponse {
        public String mensaje;
        public SatisfactionData satisfaccion;
        
        // Clase interna para mapear el objeto satisfaccion
        public static class SatisfactionData {
            public String nombre;
            public int satisfaccion;
        }
    }

    class GlobalSatisfactionResponse {
        public String mensaje;
        public double promedio;
    }

}