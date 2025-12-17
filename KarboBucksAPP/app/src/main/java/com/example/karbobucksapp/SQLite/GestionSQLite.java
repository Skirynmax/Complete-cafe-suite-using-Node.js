package com.example.karbobucksapp.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.karbobucksapp.models.Usuario;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GestionSQLite extends SQLiteOpenHelper {
    private static final String TAG = "GestionSQLite";
    private static final String DATABASE_NAME = "karbobucks.db";
    private static final int VERSIO_BD = 1;

    // Tabla de usuarios
    private static final String TABLA_USUARIOS = "usuarios";
    private static final String COLUMNA_ID = "id";
    private static final String COLUMNA_NOMBRE = "nombre";
    private static final String COLUMNa_EMAIL = "email";
    private static final String COLUMNA_TELEFONO = "telefono";
    private static final String COLUMNA_PASSWORD = "password";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLA_USUARIOS + " (" +
            COLUMNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMNA_NOMBRE + " TEXT NOT NULL, " +
            COLUMNa_EMAIL + " TEXT UNIQUE NOT NULL, " +
            COLUMNA_TELEFONO + " TEXT, " +
            COLUMNA_PASSWORD + " TEXT NOT NULL" +
            ")";

    public GestionSQLite(Context context) {
        super(context, DATABASE_NAME, null, VERSIO_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        Log.d(TAG, "Base de datos creada");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_USUARIOS);
        onCreate(db);
    }

    // Registrar nuevo usuario
    public long registrarUsuario(Usuario usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMNA_NOMBRE, usuario.getNombre());
        values.put(COLUMNa_EMAIL, usuario.getEmail());
        values.put(COLUMNA_TELEFONO, usuario.getTelefono());
        values.put(COLUMNA_PASSWORD, hashContrasena(usuario.getPassword()));

        long id = db.insert(TABLA_USUARIOS, null, values);
        db.close();

        if (id != -1) {
            Log.d(TAG, "Usuario registrado con ID: " + id);
        } else {
            Log.e(TAG, "Error al registrar usuario");
        }

        return id;
    }

    // Iniciar sesión
    public Usuario iniciarSesion(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashContrasena(password);

        Cursor cursor = db.query(
                TABLA_USUARIOS,
                null,
                COLUMNa_EMAIL + "=? AND " + COLUMNA_PASSWORD + "=?",
                new String[] { email, hashedPassword },
                null, null, null);

        Usuario user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new Usuario(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMNA_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_NOMBRE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMNa_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_TELEFONO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_PASSWORD)));
            cursor.close();
            Log.d(TAG, "Inicio de sesión exitoso para: " + email);
        } else {
            Log.d(TAG, "Credenciales incorrectas para: " + email);
        }

        db.close();
        return user;
    }

    // Obtener usuario por ID
    public Usuario obtenerUsuario(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLA_USUARIOS,
                null,
                COLUMNA_ID + "=?",
                new String[] { String.valueOf(id) },
                null, null, null);

        Usuario usuario = null;
        if (cursor != null && cursor.moveToFirst()) {
            usuario = new Usuario(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMNA_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_NOMBRE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMNa_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_TELEFONO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_PASSWORD)));
            cursor.close();
        }

        db.close();
        return usuario;
    }

    // Actualizar usuario
    public int actualizarUsuario(Usuario usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMNA_NOMBRE, usuario.getNombre());
        values.put(COLUMNa_EMAIL, usuario.getEmail());
        values.put(COLUMNA_TELEFONO,usuario.getTelefono());

        // Solo actualizar password si no está vacío
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            values.put(COLUMNA_PASSWORD, hashContrasena(usuario.getPassword()));
        }

        int rowsAffected = db.update(
                TABLA_USUARIOS,
                values,
                COLUMNA_ID + "=?",
                new String[] { String.valueOf(usuario.getId()) });

        db.close();
        Log.d(TAG, "Usuario actualizado. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }

    // Verificar si el email ya existe
    public boolean usuarioExiste(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLA_USUARIOS,
                new String[] {COLUMNA_ID},
                COLUMNa_EMAIL + "=?",
                new String[] { email },
                null, null, null);

        boolean existe = cursor != null && cursor.getCount() > 0;

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return existe;
    }

    // Hash de contraseña simple (MD5)
    private String hashContrasena(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error al hashear contraseña: " + e.getMessage());
            return password; // Fallback (no recomendado en producción)
        }
    }
}
