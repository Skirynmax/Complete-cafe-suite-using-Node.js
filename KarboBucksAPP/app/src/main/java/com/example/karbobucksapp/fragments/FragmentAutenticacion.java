package com.example.karbobucksapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.karbobucksapp.R;
import com.example.karbobucksapp.SQLite.GestionSQLite;
import com.example.karbobucksapp.models.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class FragmentAutenticacion extends Fragment {

    private TextInputEditText etNombre, etEmail, etTelefono, etPassword;
    private TextInputLayout tilNombre, tilTelefono;
    private TextView tvTitle, tvError, tvToggleMode;
    private Button btnSubmit;
    private GestionSQLite dbHelper;
    private SharedPreferences prefs;

    private boolean isLoginMode = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_autenticacion, container, false);

        initViews(view);
        dbHelper = new GestionSQLite(getContext());
        prefs = getActivity().getSharedPreferences("KarboBucksPrefs", Context.MODE_PRIVATE);

        checkIfLoggedIn();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        etNombre = view.findViewById(R.id.et_nombre);
        etEmail = view.findViewById(R.id.et_email);
        etTelefono = view.findViewById(R.id.et_telefono);
        etPassword = view.findViewById(R.id.et_password);
        tilNombre = view.findViewById(R.id.til_nombre);
        tilTelefono = view.findViewById(R.id.til_telefono);
        tvError = view.findViewById(R.id.tv_error);
        btnSubmit = view.findViewById(R.id.btn_submit);
        tvToggleMode = view.findViewById(R.id.tv_toggle_mode);
    }

    private void checkIfLoggedIn() {
        int userId = prefs.getInt("userId", -1);
        if (userId != -1) {
            Usuario user = dbHelper.obtenerUsuario(userId);
            if (user != null) {
                showUserInfo(user);
            }
        }
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> {
            if (isLoginMode) {
                handleLogin();
            } else {
                handleRegister();
            }
        });

        tvToggleMode.setOnClickListener(v -> toggleMode());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor completa todos los campos");
            return;
        }

        Usuario usuario = dbHelper.iniciarSesion(email, password);

        if (usuario != null) {
            prefs.edit().putInt("userId", usuario.getId()).apply();
            showUserInfo(usuario);
            Toast.makeText(getContext(), "Bienvenido " + usuario.getNombre(), Toast.LENGTH_SHORT).show();
        } else {
            showError("Email o contraseña incorrectos");
        }
    }

    private void handleRegister() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Por favor completa todos los campos obligatorios");
            return;
        }

        if (dbHelper.usuarioExiste(email)) {
            showError("Este email ya está registrado");
            return;
        }
                
        Usuario newUser = new Usuario(nombre, email, telefono, password);
        long userId = dbHelper.registrarUsuario(newUser);

        if (userId != -1) {
            prefs.edit().putInt("userId", (int) userId).apply();
            newUser.setId((int) userId);
            showUserInfo(newUser);
            Toast.makeText(getContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();
        } else {
            showError("Error al registrar usuario");
        }
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;

        if (isLoginMode) {
            tvTitle.setText("Iniciar Sesión");
            btnSubmit.setText("Iniciar Sesión");
            tvToggleMode.setText("¿No tienes cuenta? Regístrate");
            tilNombre.setVisibility(View.GONE);
            tilTelefono.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Registro");
            btnSubmit.setText("Registrarse");
            tvToggleMode.setText("¿Ya tienes cuenta? Inicia sesión");
            tilNombre.setVisibility(View.VISIBLE);
            tilTelefono.setVisibility(View.VISIBLE);
        }

        clearFields();
        hideError();
    }

    private void showUserInfo(Usuario usuario) {
        tvTitle.setText("Mi Cuenta");
        etNombre.setText(usuario.getNombre());
        etEmail.setText(usuario.getEmail());
        etTelefono.setText(usuario.getTelefono());
        etPassword.setText("");

        tilNombre.setVisibility(View.VISIBLE);
        tilTelefono.setVisibility(View.VISIBLE);
        btnSubmit.setText("Actualizar Datos");
        tvToggleMode.setText("Cerrar Sesión");

        btnSubmit.setOnClickListener(v -> updateUser(usuario));
        tvToggleMode.setOnClickListener(v -> logout());
    }

    private void updateUser(Usuario usuario) {
        usuario.setNombre(etNombre.getText().toString().trim());
        usuario.setEmail(etEmail.getText().toString().trim());
        usuario.setTelefono(etTelefono.getText().toString().trim());

        String newPassword = etPassword.getText().toString().trim();
        if (!newPassword.isEmpty()) {
            usuario.setPassword(newPassword);
        }

        int updated = dbHelper.actualizarUsuario(usuario);
        if (updated > 0) {
            Toast.makeText(getContext(), "Datos actualizados", Toast.LENGTH_SHORT).show();
        } else {
            showError("Error al actualizar datos");
        }
    }

    private void logout() {
        prefs.edit().remove("userId").apply();
        isLoginMode = true;
        toggleMode();
        Toast.makeText(getContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private void clearFields() {
        etNombre.setText("");
        etEmail.setText("");
        etTelefono.setText("");
        etPassword.setText("");
    }
}