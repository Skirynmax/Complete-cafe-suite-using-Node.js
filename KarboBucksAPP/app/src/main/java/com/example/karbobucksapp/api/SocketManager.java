package com.example.karbobucksapp.api;

import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {
    private static final String TAG = "SocketManager";
    private static SocketManager instance;
    private Socket socket;
    private boolean estaSubscrito = false;

    private SocketManager() {
        try {
            Log.d(TAG, "🔧 Inicializando Socket.IO con URL: " + ApiConfig.SOCKET_URL);

            // Configuración de Socket.IO
            IO.Options options = new IO.Options();
            options.forceNew = false; // Cambio: permitir reutilizar conexión
            options.reconnection = true;
            options.reconnectionAttempts = Integer.MAX_VALUE;
            options.reconnectionDelay = 1000;
            options.reconnectionDelayMax = 5000;
            options.timeout = 20000;
            options.transports = new String[]{"websocket"}; // Forzar WebSocket

            socket = IO.socket(ApiConfig.SOCKET_URL, options);

            setupSocketListeners();

            Log.d(TAG, "✅ Socket.IO inicializado correctamente");

        } catch (URISyntaxException e) {
            Log.e(TAG, "❌ Error al crear socket: " + e.getMessage(), e);
        }
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    private void setupSocketListeners() {
        if (socket == null) return;

        // Conexión exitosa
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "✅ Socket conectado exitosamente");
                Log.d(TAG, "   Socket ID: " + socket.id());

                // Auto-suscribirse cuando se conecta
                if (!estaSubscrito) {
                    subscribeToOrders();
                }
            }
        });

        // Error de conexión
        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "❌ Error de conexión");
                if (args.length > 0) {
                    Log.e(TAG, "   Detalles: " + args[0].toString());
                }
                estaSubscrito = false;
            }
        });

        // Desconexión
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String reason = args.length > 0 ? args[0].toString() : "desconocida";
                Log.w(TAG, "⚠️ Socket desconectado. Razón: " + reason);
                estaSubscrito = false;
            }
        });

        // Reconexión exitosa
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int attempt = args.length > 0 ? (int) args[0] : 0;
                Log.d(TAG, "🔄 Reconectado después de " + attempt + " intentos");

                // Re-suscribirse después de reconectar
                if (!estaSubscrito) {
                    subscribeToOrders();
                }
            }
        });

    }

    public void connect() {
        if (socket == null) {
            Log.e(TAG, "❌ Socket es null, no se puede conectar");
            return;
        }

        if (!socket.connected()) {
            Log.d(TAG, "🔌 Conectando socket...");
            socket.connect();
        } else {
            Log.d(TAG, "✅ Socket ya está conectado (ID: " + socket.id() + ")");
            // Si ya está conectado y no suscrito, suscribirse
            if (!estaSubscrito) {
                subscribeToOrders();
            }
        }
    }

    public void disconnect() {
        if (socket != null && socket.connected()) {
            Log.d(TAG, "🔌 Desconectando socket...");
            estaSubscrito = false;
            socket.disconnect();
        }
    }

    public void on(String event, Emitter.Listener listener) {
        if (socket != null) {
            Log.d(TAG, "📡 Registrando listener para evento: '" + event + "'");
            socket.on(event, listener);
        } else {
            Log.e(TAG, "❌ Socket es null, no se puede registrar listener para: " + event);
        }
    }

    public void off(String event, Emitter.Listener listener) {
        if (socket != null) {
            Log.d(TAG, "📡 Removiendo listener específico para: '" + event + "'");
            socket.off(event, listener);
        }
    }


     // Suscribirse a la sala de pedidos en el servidor

    public void subscribeToOrders() {
        if (socket == null) {
            Log.e(TAG, "❌ Socket es null, no se puede suscribir");
            return;
        }

        if (socket.connected()) {
            Log.d(TAG, "📢 Suscribiéndose a actualizaciones de pedidos...");
            socket.emit("subscribe-orders");
            estaSubscrito = true;
            Log.d(TAG, "✅ Suscripción enviada al servidor");
        } else {
            Log.w(TAG, "⚠️ Socket no conectado, esperando conexión para suscribirse...");
            estaSubscrito = false;
        }
    }

}