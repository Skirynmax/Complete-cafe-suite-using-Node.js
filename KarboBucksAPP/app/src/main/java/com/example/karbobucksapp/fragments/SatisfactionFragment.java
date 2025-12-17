package com.example.karbobucksapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.karbobucksapp.api.ApiServicio;
import com.example.karbobucksapp.api.RetrofitCliente;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SatisfactionFragment extends Fragment {

    private static final String TAG = "SatisfactionFragment";

    private SeekBar seekBarRating;
    private TextView txtRatingValor;
    private ImageView ivEmoji;
    private Button btnSubmit;
    private String nombreCliente;
    private ApiServicio apiServicio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_satisfaction, container, false);

        // Inicializar vistas
        seekBarRating = view.findViewById(R.id.seekbar_rating);
        txtRatingValor = view.findViewById(R.id.tv_rating_value);
        ivEmoji = view.findViewById(R.id.iv_emoji);
        btnSubmit = view.findViewById(R.id.btn_submit);

        // Inicializar API service
        apiServicio = RetrofitCliente.getClient().create(ApiServicio.class);

        if (getArguments() != null) {
            nombreCliente = getArguments().getString("nombreCliente", "");
            Log.d(TAG, "Recibido nombreCliente: " + nombreCliente);
        }

        SetUpSeekBar();
        btnSubmit.setOnClickListener(v -> enviarSatisfaccion());

        return view;
    }

    private void SetUpSeekBar() {
        seekBarRating.setMax(10);
        seekBarRating.setProgress(5);
        ActualizarRatingDisplay(5);

        seekBarRating.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ActualizarRatingDisplay(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void ActualizarRatingDisplay(int rating) {
        txtRatingValor.setText(rating + "/10");

        if (rating <= 2) {
            ivEmoji.setImageResource(R.drawable.emoji_very_bad);
        } else if (rating <= 4) {
            ivEmoji.setImageResource(R.drawable.emoji_bad);
        } else if (rating <= 6) {
            ivEmoji.setImageResource(R.drawable.emoji_neutral);
        } else if (rating <= 8) {
            ivEmoji.setImageResource(R.drawable.emoji_good);
        } else {
            ivEmoji.setImageResource(R.drawable.emoji_excellent);
        }
    }

    private void enviarSatisfaccion() {
        // Usar el nombre del cliente del pedido
        String nombre = (nombreCliente != null && !nombreCliente.isEmpty()) ? nombreCliente : "Cliente";
        int calificacion = seekBarRating.getProgress();

        // Log para debugging
        Log.d(TAG, "Enviando - Nombre: " + nombre + ", Calificación: " + calificacion);

        // Validación solo de calificación
        if (calificacion < 1) {
            Toast.makeText(getContext(),
                    "Por favor selecciona una calificación",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón mientras se envía
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Enviando...");

        // Crear request
        ApiServicio.SatisfactionRequest request = new ApiServicio.SatisfactionRequest(nombre, calificacion);

        // Hacer llamada a la API
        Call<ApiServicio.SatisfactionResponse> call = apiServicio.submitSatisfaction(request);

        call.enqueue(new Callback<ApiServicio.SatisfactionResponse>() {
            @Override
            public void onResponse(Call<ApiServicio.SatisfactionResponse> call,
                    Response<ApiServicio.SatisfactionResponse> response) {

                // Rehabilitar botón
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Enviar");

                Log.d(TAG, "Código de respuesta: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiServicio.SatisfactionResponse respuesta = response.body();
                    Log.d(TAG, "Respuesta exitosa: " + respuesta.mensaje);

                    Toast.makeText(getContext(),
                            "¡Gracias por tu opinión!",
                            Toast.LENGTH_LONG).show();

                    // Resetear seekbar
                    seekBarRating.setProgress(5);

                    // volver atrás
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                } else {
                    Log.e(TAG, "Error en respuesta: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string()
                                : "Sin detalles";
                        Log.e(TAG, "Error body: " + errorBody);
                        Toast.makeText(getContext(),
                                "Error: " + errorBody,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer errorBody", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiServicio.SatisfactionResponse> call, Throwable t) {
                // Rehabilitar botón
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Enviar");

                Log.e(TAG, "Error de conexión", t);
                Toast.makeText(getContext(),
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
