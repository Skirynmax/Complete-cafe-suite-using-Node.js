const express = require('express');
const router = express.Router();
const mysql = require('mysql');

let connection;

// Configuración de conexión a MySQL
async function ConexioBaseDatos() {
    connection = mysql.createConnection({
        host: 'dam2.colexio-karbo.com',
        port: 3333,
        user: 'dam2', 
        password: 'Ka3b0134679',
        database: 'karbo_jalban'
    });
    
    connection.connect((error) => {//Se intenta conectar a la base de datos
        if (error) {//Si hay un error se muestra en la consola
            console.error('Error al conectar con MySQL:', error);
            return;
        }
        console.log('MySQL conectado a karbo_jalban');
    });
}

ConexioBaseDatos();

// GET todos los platos (CON CALLBACKS)
router.get('/', (req, res) => {
    connection.query('SELECT * FROM ExamenMoncho1_Carta', (error, rows) => {//query para obtener todos los platos
        if (error) {//Si hay un error se muestra en la consola
            console.error('Error al obtener platos:', error);
            return res.status(500).json({ error: 'Error al obtener los platos' });
        }
        
        res.json(rows);//Devolvemos la respuesta en JSON
    });
});


module.exports = router;