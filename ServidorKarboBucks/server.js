// servidor-node.js
const express = require('express');
const http = require('http');
const { Server } = require('socket.io');

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
    cors: {
        origin: "*", // Permitir conexiones desde cualquier origen (Android app)
        methods: ["GET", "POST", "PATCH", "DELETE"]
    }
});

const PORT = process.env.PORT || 6001;

// Middlewares
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Middleware de logging simple
app.use((req, res, next) => {
    console.log(`${req.method} ${req.url}`);
    next();
});

// Configuración de Socket.IO
io.on('connection', (socket) => {
    console.log('✅ Cliente conectado:', socket.id);//Cliente conectado mostrar en consola

    socket.on('disconnect', () => {//Cliente desconectado mostrar en consola
        console.log('❌ Cliente desconectado:', socket.id);
    });

    // Evento opcional para que el cliente se identifique
    socket.on('subscribe-orders', () => {//Cliente suscrito a actualizaciones de pedidos mostrar en consola
        console.log('📋 Cliente suscrito a actualizaciones de pedidos:', socket.id);
        socket.join('orders-room');//Unir al cliente a la sala de pedidos
    });
});

// Exportar io para usarlo en las rutas
global.io = io;

// Importar rutas
const rutasCarta = require('./rutas/carta');
const rutasPedidos = require('./rutas/pedidos');

// Usar rutas
app.use('/api/carta', rutasCarta);
app.use('/api/pedidos', rutasPedidos);


// Ruta raíz
app.get('/', (req, res) => {
    res.json({
        mensaje: 'API REST con Express y Socket.IO',
        version: '1.0.0',
        endpoints: ['/api/carta', '/api/pedidos'],
        socketio: 'Habilitado para actualizaciones en tiempo real'
    });
});

// Manejo de errores global
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ error: 'Algo salió mal!' });
});

// Ruta no encontrada
app.use((req, res) => {
    res.status(404).json({ error: 'Endpoint no encontrado' });
});


server.listen(PORT, () => {//Servidor escuchando en el puerto 6001
    console.log(`🚀 Servidor corriendo en http://localhost:${PORT}`);
    console.log(`🔌 Socket.IO habilitado para actualizaciones en tiempo real`);
});