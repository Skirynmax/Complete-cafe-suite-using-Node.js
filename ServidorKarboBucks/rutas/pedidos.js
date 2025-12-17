const express = require('express');
const router = express.Router();
const { MongoClient, ObjectId } = require('mongodb');

let db;


function getFechaEspana() {
    return new Date(Date.now() + 3600000);//Devuelve la fecha actual en España con un retraso de 1 hora porque la new date daba una hora antes
}

// Configuración de conexión a MongoDB
async function ConexioBaseDatos() {
    const uri = "mongodb://admin:Ka3b0134679@dam2.colexio-karbo.com:57017?authSource=admin";
    const client = new MongoClient(uri);

    try {//Se intenta conectar a la base de datos
        await client.connect();
        db = client.db('jalban');
        console.log('MongoDB conectado a jalban');
    } catch (error) {//Si hay un error se muestra en la consola
        console.error('Error al conectar con MongoDB:', error);
    }
}

ConexioBaseDatos().catch(console.error);



// GET - Obtener todos los pedidos
router.get('/', async (req, res) => {

    try {//Se intenta obtener todos los pedidos
        const pedidos = await db.collection('Pedidos').find({}).toArray();
        res.json(pedidos);
    } catch (error) {//Si hay un error se muestra en la consola
        console.error('Error al obtener pedidos:', error);
        res.status(500).json({ error: 'Error al obtener los pedidos' });
    }
});

// GET - Obtener un pedido por ID
router.get('/:id', async (req, res) => {
    try {//Se intenta obtener un pedido por ID
        // Validar que el ID sea válido
        if (!ObjectId.isValid(req.params._id)) {
            return res.status(400).json({ error: 'ID no válido' });
        }

        const pedido = await db.collection('Pedidos').findOne({//Se intenta obtener un pedido por ID
            _id: new ObjectId(req.params._id)
        });

        if (!pedido) {//Si no se encuentra el pedido
            return res.status(404).json({ error: 'Pedido no encontrado' });
        }

        res.json({//Devolvemos la respuesta en JSON
            mensaje: 'Pedido obtenido correctamente',
            pedido: pedido
        });
    } catch (error) {//Si hay un error se muestra en la consola
        console.error('Error al obtener pedido:', error);
        res.status(500).json({ error: 'Error al obtener el pedido' });
    }
});

//POST -Enviar satisfaccion a la coleccion Satisfaccion
router.post('/satisfaccion', async (req, res) => {
    const { nombre, satisfaccion } = req.body;//obtener los datos del pedido enviados al POST

    // Validaciones
    if (!nombre || !satisfaccion) {//Validar que los campos obligatorios esten presentes
        return res.status(400).json({
            error: 'Los campos nombre y satisfaccion son obligatorios'
        });
    }

    try {

        const nuevaSatisfaccion = {//Creamos el json para la satisfaccion
            nombre: nombre,
            satisfaccion: satisfaccion  
          
        };

        const result = await db.collection('Satisfaccion').insertOne(nuevaSatisfaccion);//Insertar la satisfaccion en la base de datos en la coleccion Satisfaccion

        res.json({//Devolvemos la respuesta en JSON
            mensaje: 'Satisfaccion actualizada correctamente',
            satisfaccion: nuevaSatisfaccion
        });
    } catch (error) {//si hay un error
        console.error('Error al actualizar satisfaccion:', error);
        res.status(500).json({ error: 'Error al actualizar la satisfaccion' });
    }
});



// POST - Crear un nuevo pedido
router.post('/crear', async (req, res) => {
    const { nombre, productos, totalPagar, tiempoEntrega } = req.body;//obtener los datos del pedido enviados al POST

    // Validacionescrear
    if (!nombre || !productos || !totalPagar) {//Validar que los campos obligatorios esten presentes
        return res.status(400).json({
            error: 'Los campos cliente, productos y total son obligatorios'
        });
    }

    if (!Array.isArray(productos) || productos.length === 0) {//Validar que los productos sean un array con al menos un producto
        return res.status(400).json({
            error: 'Productos debe ser un array con al menos un producto'
        });
    }

    try {
        const fechaActual = getFechaEspana();//obtener la fecha actual

        const nuevoPedido = {//Creamos el json para el pedido
            nombre: nombre,//nombre del cliente
            productos: productos,//productos del pedido
            totalPagar: totalPagar,//total a pagar
            estado: 'Pedido', // Estado por defecto
            fechaCreado: fechaActual,//fecha de creacion
            tiempoEstimado: tiempoEntrega//tiempo estimado de entrega
        };

        const result = await db.collection('Pedidos').insertOne(nuevoPedido);//Insertar el pedido en la base de datos

        const pedidoCompleto = {//Creamos el json para el pedido completo
            _id: result.insertedId,
            ...nuevoPedido
        };

        // Emitir evento Socket.IO para nuevo pedido
        if (global.io) {
            global.io.emit('new-order', {//emitimos el evento new-order
                order: pedidoCompleto
            });
            console.log('🔔 Evento emitido: new-order para pedido', result.insertedId);
        }

        res.status(201).json({//Devolvemos la respuesta en JSON
            mensaje: 'Pedido creado correctamente',
            id: result.insertedId,
            pedido: pedidoCompleto
        });
    } catch (error) {//si hay un error
        console.error('Error al crear pedido:', error);
        res.status(500).json({ error: 'Error al crear el pedido' });
    }
});


// PATCH - Actualizar solo el estado del pedido
router.patch('/:id/estado', async (req, res) => {
    try {

        console.log("ID recibido:", req.params.id);//mostrar el id recibido
        console.log("Estado recibido:", req.body.estado);//mostrar el estado recibido

        if (!ObjectId.isValid(req.params.id)) {//Validar que el id sea válido
            return res.status(400).json({ error: 'ID no válido' });
        }

        const estado = req.body.estado;//obtener el estado

        if (!estado) {//Validar que el estado sea válido
            return res.status(400).json({ error: 'El campo estado es obligatorio' });
        }

        const result = await db.collection('Pedidos').updateOne(
            { _id: new ObjectId(req.params.id) },//Actualizar el pedido
            {
                $set: {
                    estado: estado,
                    fechaActualizacion: getFechaEspana()
                }
            }
        );

        if (result.matchedCount === 0) {//Validar que el pedido exista
            return res.status(404).json({ error: 'Pedido no encontrado' });
        }

        const pedidoActualizado = await db.collection('Pedidos').findOne({//Buscar el pedido actualizado
            _id: new ObjectId(req.params.id)
        });

        // Emitir evento Socket.IO para actualizaciones en tiempo real
        if (global.io) {
            global.io.emit('order-status-updated', {
                orderId: req.params.id,
                newStatus: estado,
                order: pedidoActualizado
            });
            console.log('🔔 Evento emitido: order-status-updated para pedido', req.params.id);
        }

        res.json({//Devolver la respuesta en json
            mensaje: 'Estado del pedido actualizado correctamente',
            pedido: pedidoActualizado
        });
    } catch (error) {//si hay un error
        console.error('Error al actualizar estado:', error);
        res.status(500).json({ error: 'Error al actualizar el estado' });
    }
});

// DELETE - Eliminar un pedido
router.delete('/:id', async (req, res) => {
    try {
        if (!ObjectId.isValid(req.params.id)) {//Validar que el id sea válido
            return res.status(400).json({ error: 'ID no válido' });
        }

        // Obtener el pedido antes de eliminarlo
        const pedido = await db.collection('Pedidos').findOne({
            _id: new ObjectId(req.params.id)
        });

        if (!pedido) {//si no se encuentra el pedido
            return res.status(404).json({ error: 'Pedido no encontrado' });
        }

        const result = await db.collection('Pedidos').deleteOne({//Eliminar el pedido
            _id: new ObjectId(req.params.id)
        });

        // Emitir evento Socket.IO para pedido eliminado
        if (global.io) {
            global.io.emit('order-deleted', {//Emitir evento Socket.IO para pedido eliminado
                orderId: req.params.id,
                order: pedido
            });
            console.log('🔔 Evento emitido: order-deleted para pedido', req.params.id);
        }

        res.json({//Devolver la respuesta en json
            mensaje: 'Pedido eliminado correctamente',
            eliminados: result.deletedCount,
            pedido: pedido
        });
    } catch (error) {//si hay un error
        console.error('Error al eliminar pedido:', error);
        res.status(500).json({ error: 'Error al eliminar el pedido' });
    }
});



// GET - Obtener estadísticas de pedidos
router.get('/agregacion/resumen', async (req, res) => {
    try {
        const totalPedidos = await db.collection('Pedidos').countDocuments();//contar los pedidos con la función count

        const pedidosPorEstado = await db.collection('Pedidos').aggregate([//agregación de pedidos por estado
            {
                $group: {
                    _id: '$estado',//agrupar por estado
                    cantidad: { $sum: 1 },//contar la cantidad de pedidos
                    totalVentas: { $sum: '$totalPagar' }//sumar el total de las ventas
                }
            }
        ]).toArray();

        const ventaTotal = await db.collection('Pedidos').aggregate([//agregación de ventas totales
            {
                $group: {
                    _id: null,//agrupar por null
                    total: { $sum: '$totalPagar' }//sumar el total de las ventas
                }
            }
        ]).toArray();

        res.json({//devolver las estadísticas en json
            mensaje: 'Estadísticas obtenidas correctamente',
            totalPedidos,
            ventaTotal: ventaTotal[0]?.total || 0,//devolver el total de las ventas si no 0
            pedidosPorEstado
        });
    } catch (error) {
        console.error('Error al obtener estadísticas:', error);
        res.status(500).json({ error: 'Error al obtener estadísticas' });
    }
});

// Agregación del promedio de satisfacción global
router.get('/satisfaccion/global', async (req, res) => {
    try {
        //operación de agregación para calcular el promedio global de satisfacción
        const global = await db.collection('Satisfaccion').aggregate([
            {
                $group: { 
                    _id: null,  // Agrupa todos los documentos
                    promedio: { $avg: '$satisfaccion' }  // Campo con mayúscula
                }
            }
        ]).toArray();

        res.json({
            mensaje: 'Satisfacción global obtenida correctamente',
            promedio: global[0]?.promedio || 0  // Ahora coincide con el campo
        });
    } catch (error) {
        console.error('Error al obtener satisfacción global:', error);
        res.status(500).json({ error: 'Error al obtener satisfacción global' });
    }
});


// Productos más vendidos
router.get('/agregacion/productos-mas-vendidos', async (req, res) => {
    try {
        const productosMasVendidos = await db.collection('Pedidos').aggregate([//consulta de agregación a mongodb
            {
                // Descomponer array de productos
                $unwind: '$productos'
            },
            {
                // Agrupar por nombre de producto (con mayúscula)
                $group: {
                    _id: '$productos.Nombre',
                    cantidadVendida: { $sum: 1 }, // Contar cada ocurrencia del producto
                    vecesOrdenado: { $sum: 1 },//contar las veces que se ordeno
                    ingresoTotal: { $sum: '$productos.Precio' }, // Sumar precios directamente
                    precioPromedio: { $avg: '$productos.Precio' }//calcular el precio promedio
                }
            },
            {
                // Ordenar por cantidad vendida
                $sort: { cantidadVendida: -1 }
            },
            {
                // Formatear salida
                $project: {
                    _id: 0,
                    producto: '$_id',//nombre del producto
                    cantidadVendida: 1,//cantidad vendida
                    vecesOrdenado: 1,//veces ordenado
                    ingresoTotal: { $round: ['$ingresoTotal', 2] },//ingreso total
                    precioPromedio: { $round: ['$precioPromedio', 2] }//precio promedio
                }
            }
        ]).toArray();

        res.json({//Devolvemos la respuesta en JSON
            mensaje: 'Productos más vendidos obtenidos correctamente',
            explicacion: 'Análisis de popularidad de productos para optimizar inventario y estrategia de menú',
            total: productosMasVendidos.length,
            datos: productosMasVendidos
        });
    } catch (error) {
        console.error('Error en agregación productos:', error);
        res.status(500).json({ error: 'Error al procesar la agregación' });
    }
});

module.exports = router;