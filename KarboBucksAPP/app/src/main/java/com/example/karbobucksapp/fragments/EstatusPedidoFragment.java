package com.example.karbobucksapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.karbobucksapp.R;
import com.example.karbobucksapp.api.RetrofitCliente;
import com.example.karbobucksapp.api.SocketManager;
import com.example.karbobucksapp.models.Pedido;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstatusPedidoFragment extends Fragment {

    private static final String TAG = "EstatusPedidoFragment";

    private TextView textEstadoPedido, txtTiempoEstimado, txtProgreso, txtNoPedidos;
    private ProgressBar progressBarOrder;
    private Button btnViewDetails;
    private SocketManager socketManager;
    private Pedido pedidoActual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estatus_pedido, container, false);

        InicializarViews(view);
        setupSocketIO();
        CargarUltimoPedido();

        btnViewDetails.setOnClickListener(v -> {
            if (pedidoActual != null) {
                Bundle args = new Bundle();
                args.putSerializable("order", pedidoActual);
                Navigation.findNavController(v).navigate(R.id.action_estatusPedido_to_detallesPedido, args);
            }
        });

        return view;
    }

    private void InicializarViews(View view) {
        textEstadoPedido = view.findViewById(R.id.tv_order_status);
        txtTiempoEstimado = view.findViewById(R.id.tv_estimated_time);
        txtProgreso = view.findViewById(R.id.tv_progress_text);
        txtNoPedidos = view.findViewById(R.id.tv_no_order);
        progressBarOrder = view.findViewById(R.id.progress_bar_order);
        btnViewDetails = view.findViewById(R.id.btn_view_details);
    }

    private void setupSocketIO() {
        socketManager = SocketManager.getInstance();
        socketManager.connect();

        // Los listeners se registran ANTES de suscribirse
        socketManager.on("new-order", onNewOrder);
        socketManager.on("order-status-updated", onOrderStatusUpdated);
        socketManager.on("order-deleted", onOrderDeleted);

        Log.d(TAG, "Socket.IO configurado y listeners registrados");
    }

    private void CargarUltimoPedido() {
        Log.d(TAG, "Cargando último pedido...");

        RetrofitCliente.getApiService().obtenerPedidos().enqueue(new Callback<List<Pedido>>() {
            @Override
            public void onResponse(Call<List<Pedido>> call, Response<List<Pedido>> response) {
                Log.d(TAG, "Respuesta recibida - Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Pedido> orders = response.body();
                    Log.d(TAG, "Número de pedidos recibidos: " + orders.size());

                    if (!orders.isEmpty()) {
                        // CORRECCIÓN 1: Ordenar por fecha de creación (más reciente primero)
                        Collections.sort(orders, new Comparator<Pedido>() {
                            @Override
                            public int compare(Pedido o1, Pedido o2) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                            Locale.US);
                                    Date date1 = sdf.parse(o1.getFechaCreado());
                                    Date date2 = sdf.parse(o2.getFechaCreado());
                                    // Orden descendente (más reciente primero)
                                    return date2.compareTo(date1);
                                } catch (ParseException e) {
                                    Log.e(TAG, "Error al parsear fechas", e);
                                    return 0;
                                }
                            }
                        });

                        // Obtener el pedido más reciente (primero después de ordenar)
                        pedidoActual = orders.get(0);
                        Log.d(TAG, "Pedido más reciente - ID: " + pedidoActual.getId() +
                                ", Estado: " + pedidoActual.getEstado() +
                                ", Fecha: " + pedidoActual.getFechaCreado());

                        ActualizarUI(pedidoActual);
                    } else {
                        Log.d(TAG, "La lista de pedidos está vacía");
                        MostrarNoHayPedido();
                    }
                } else {
                    Log.e(TAG, "Respuesta no exitosa o body nulo");
                    MostrarNoHayPedido();
                }
            }

            @Override
            public void onFailure(Call<List<Pedido>> call, Throwable t) {
                Log.e(TAG, "Error al cargar pedidos: " + t.getMessage(), t);
                MostrarNoHayPedido();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(),
                            "Error de conexión: " + t.getMessage(),
                            Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void ActualizarUI(Pedido order) {
        if (getActivity() == null)
            return;

        getActivity().runOnUiThread(() -> {
            Log.d(TAG, "Actualizando UI con pedido: " + order.getId());

            // Verificar si el pedido está recogido y navegar a satisfacción
            if (order.getEstado() != null && order.getEstado().equalsIgnoreCase("Recogido")) {
                Log.d(TAG, "Pedido recogido, navegando a SatisfactionFragment");

                // Verificar que la vista esté disponible antes de navegar
                if (getView() != null && isAdded()) {
                    try {
                        Bundle args = new Bundle();
                        args.putString("nombreCliente", order.getNombre());
                        Navigation.findNavController(getView()).navigate(
                                R.id.action_estatusPedido_to_satisfaction, args);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Error al navegar: " + e.getMessage());
                        // Si falla la navegación, simplemente no hacemos nada
                        // Esto puede ocurrir si ya estamos en otro fragmento
                    }
                }
                return;
            }

            txtNoPedidos.setVisibility(View.GONE);
            textEstadoPedido.setVisibility(View.VISIBLE);
            txtTiempoEstimado.setVisibility(View.VISIBLE);
            progressBarOrder.setVisibility(View.VISIBLE);
            txtProgreso.setVisibility(View.VISIBLE);
            btnViewDetails.setVisibility(View.VISIBLE);

            textEstadoPedido.setText("Estado: " + order.getEstado());

            // CORRECCIÓN 2: Convertir tiempoEstimado de segundos a minutos
            int minutos = order.getTiempoEstimado() / 60;
            txtTiempoEstimado.setText("Tiempo estimado: " + minutos + " min");

            // Actualizar progreso según el estado
            int progress = ConseguirProgresoParaEstado(order.getEstado());
            progressBarOrder.setProgress(progress);

            String message = GetMensajeParaEstadoPedido(order.getEstado());
            txtProgreso.setText(message);

            Log.d(TAG, "UI actualizada - Progreso: " + progress + ", Mensaje: " + message);
        });
    }

    private void MostrarNoHayPedido() {
        if (getActivity() == null)
            return;

        getActivity().runOnUiThread(() -> {
            Log.d(TAG, "Mostrando mensaje de 'sin pedidos'");

            txtNoPedidos.setVisibility(View.VISIBLE);
            textEstadoPedido.setVisibility(View.GONE);
            txtTiempoEstimado.setVisibility(View.GONE);
            progressBarOrder.setVisibility(View.GONE);
            txtProgreso.setVisibility(View.GONE);
            btnViewDetails.setVisibility(View.GONE);
        });
    }

    // Estados sincronizados con PanelBarista.java
    private int ConseguirProgresoParaEstado(String status) {
        if (status == null)
            return 0;

        switch (status.toLowerCase().trim()) {
            case "pedido":
                return 25;
            case "en preparación":
                return 50;
            case "listo para recoger":
                return 75;
            case "recogido":
                return 100;
            default:
                Log.w(TAG, "Estado desconocido: " + status);
                return 10; // Progreso mínimo para estados desconocidos
        }
    }

    // Estados sincronizados con PanelBarista.java
    private String GetMensajeParaEstadoPedido(String status) {
        if (status == null)
            return "Estado desconocido";

        switch (status.toLowerCase().trim()) {
            case "pedido":
                return "Tu pedido ha sido recibido y está en cola";
            case "en preparación":
                return "Tu pedido está siendo preparado...";
            case "listo para recoger":
                return "¡Tu pedido está listo para recoger!";
            case "recogido":
                return "Pedido recogido. ¡Gracias por tu compra!";
            default:
                return "Estado: " + status;
        }
    }

    private Emitter.Listener onNewOrder = args -> {
        if (args.length > 0) {
            try {
                JSONObject data = (JSONObject) args[0];
                JSONObject orderJson = data.getJSONObject("order");

                Log.d(TAG, "🔔 Nuevo pedido recibido vía Socket: " + orderJson.toString());

                // Recargar pedidos después de un breve delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    CargarUltimoPedido();
                    Toast.makeText(getContext(), "¡Nuevo pedido recibido!", Toast.LENGTH_SHORT).show();
                }, 500);

            } catch (Exception e) {
                Log.e(TAG, "Error al procesar nuevo pedido: " + e.getMessage(), e);
            }
        }
    };

    private Emitter.Listener onOrderStatusUpdated = args -> {
        if (args.length > 0) {
            try {
                JSONObject data = (JSONObject) args[0];
                String orderId = data.getString("orderId");
                String newStatus = data.getString("newStatus");

                Log.d(TAG, "🔔 Estado actualizado vía Socket - ID: " + orderId + ", Estado: " + newStatus);

                // Si es nuestro pedido actual, actualizar UI
                if (pedidoActual != null && pedidoActual.getId().equals(orderId)) {
                    pedidoActual.setEstado(newStatus);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        ActualizarUI(pedidoActual);
                        Toast.makeText(getContext(),
                                "Estado actualizado: " + newStatus,
                                Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Si no es nuestro pedido actual, recargar por si acaso
                    new Handler(Looper.getMainLooper()).post(() -> CargarUltimoPedido());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error al procesar actualización de estado: " + e.getMessage(), e);
            }
        }
    };

    // Listener para pedidos eliminados
    private Emitter.Listener onOrderDeleted = args -> {
        if (args.length > 0) {
            try {
                JSONObject data = (JSONObject) args[0];
                String orderId = data.getString("orderId");

                Log.d(TAG, "🔔 Pedido eliminado vía Socket - ID: " + orderId);

                // Si es nuestro pedido actual, recargar la lista
                if (pedidoActual != null && pedidoActual.getId().equals(orderId)) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        CargarUltimoPedido();
                        Toast.makeText(getContext(),
                                "El pedido ha sido eliminado",
                                Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Error al procesar pedido eliminado: " + e.getMessage(), e);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Recargando pedidos");
        CargarUltimoPedido();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (socketManager != null) {
            socketManager.off("new-order", onNewOrder);
            socketManager.off("order-status-updated", onOrderStatusUpdated);
            socketManager.off("order-deleted", onOrderDeleted);
        }
        Log.d(TAG, "Vista destruida - Listeners eliminados");
    }
}