package com.example.karbobucksapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private TextView txtNombreCafeteria, txtDireccion, txtHorario, txtTelefono, txtSatisfaction;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        txtNombreCafeteria = view.findViewById(R.id.tv_store_name);
        txtDireccion = view.findViewById(R.id.tv_address);
        txtHorario = view.findViewById(R.id.tv_schedule);
        txtTelefono = view.findViewById(R.id.tv_phone);
        txtSatisfaction = view.findViewById(R.id.tv_satisfaction);
        progressBar = view.findViewById(R.id.progress_bar);

        // Mostrar información por defecto
        MostrarInformacionDefault();

        // Cargar promedio global de satisfacción
        CargarCalificacionGlobal();

        return view;
    }

    private void CargarCalificacionGlobal() {
        progressBar.setVisibility(View.VISIBLE);

        RetrofitCliente.getApiService().obtenerPromedioGlobal()
                .enqueue(new Callback<ApiServicio.GlobalSatisfactionResponse>() {
                    @Override
                    public void onResponse(Call<ApiServicio.GlobalSatisfactionResponse> call,
                            Response<ApiServicio.GlobalSatisfactionResponse> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            double promedio = response.body().promedio;
                            Log.d(TAG, "Promedio global recibido: " + promedio);
                            txtSatisfaction.setText(String.format("⭐ Satisfacción: %.1f/10", promedio));
                        } else {
                            Log.e(TAG, "Error en respuesta: " + response.code());
                            txtSatisfaction.setText("⭐ Satisfacción: N/A");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiServicio.GlobalSatisfactionResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Error al cargar satisfacción: " + t.getMessage(), t);
                        Toast.makeText(getContext(), "Error al cargar satisfacción: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        txtSatisfaction.setText("⭐ Satisfacción: N/A");
                    }
                });
    }

    private void MostrarInformacionDefault() {
        txtNombreCafeteria.setText("KarboBucks");
        txtDireccion.setText("📍 Calle Principal 123");
        txtHorario.setText("🕒 Lunes a Domingo: 10:00 - 22:00");
        txtTelefono.setText("📞 +34 123 456 789");
        txtSatisfaction.setText("⭐ Satisfacción: Cargando...");
    }
}