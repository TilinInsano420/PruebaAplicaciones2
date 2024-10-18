package com.example.arreglado;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Registro_Usuario extends AppCompatActivity {
    private EditText nombreEditText, correoEditText, contrasenaEditText, confirmarContrasenaEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuario); // XML de registro de usuario

        nombreEditText = findViewById(R.id.nombreus2);
        correoEditText = findViewById(R.id.Correoelectronico);
        contrasenaEditText = findViewById(R.id.Contra);
        confirmarContrasenaEditText = findViewById(R.id.Confirmar_contra1);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Button botonRegistro = findViewById(R.id.butRegistro);
        botonRegistro.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = nombreEditText.getText().toString().trim();
        String correo = correoEditText.getText().toString().trim();
        String contrasena = contrasenaEditText.getText().toString().trim();
        String confirmarContrasena = confirmarContrasenaEditText.getText().toString().trim();

        if (contrasena.equals(confirmarContrasena)) {
            mAuth.createUserWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Verifica que el usuario se ha autenticado correctamente
                            if (mAuth.getCurrentUser() != null) {
                                // Guardar información en Firestore
                                String uid = mAuth.getCurrentUser().getUid(); // Obtén el UID sin riesgos de NullPointerException
                                Map<String, Object> usuario = new HashMap<>();
                                usuario.put("nombre", nombre);
                                usuario.put("correo", correo);

                                db.collection("usuarios").document(uid)
                                        .set(usuario)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(Registro_Usuario.this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(Registro_Usuario.this, MainActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(Registro_Usuario.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                // Maneja el caso en que no se haya autenticado correctamente
                                Toast.makeText(Registro_Usuario.this, "Error al obtener datos de usuario", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Registro_Usuario.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
        }
    }
}
