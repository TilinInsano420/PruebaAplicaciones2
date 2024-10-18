package com.example.arreglado;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    // Declaración de FirebaseAuth
    private FirebaseAuth mAuth;

    // Referencias a los campos de texto
    private EditText nombreUsuario, contrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Asegúrate de que tu archivo XML sea 'main_activity.xml'

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Referencias a los campos del layout
        nombreUsuario = findViewById(R.id.nombreus);
        contrasena = findViewById(R.id.Contra);

        // Acción del botón de inicio de sesión
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion();
            }
        });

        // Acción del botón de registro (lleva a la pantalla de registro)
        findViewById(R.id.Registrarse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Registro_Usuario.class)); // Redirige al activity de registro
            }
        });
    }

    private void iniciarSesion() {
        // Obtención de los valores ingresados por el usuario
        String email = nombreUsuario.getText().toString().trim();
        String password = contrasena.getText().toString().trim();

        // Validaciones básicas
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            nombreUsuario.setError("Ingresa un correo electrónico válido");
            nombreUsuario.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            contrasena.setError("Ingresa una contraseña");
            contrasena.requestFocus();
            return;
        }

        // Autenticación del usuario con Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inicio de sesión exitoso, redirigir a la siguiente actividad (ejemplo: pantalla principal)
                        Toast.makeText(MainActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, PantallaPrincipal.class)); // Redirige a otra pantalla después del inicio de sesión
                        finish(); // Finaliza la actividad actual para evitar volver con el botón de retroceso
                    } else {
                        // Si falla, mostrar un mensaje de error
                        Toast.makeText(MainActivity.this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
