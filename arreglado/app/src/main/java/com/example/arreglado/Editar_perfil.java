package com.example.arreglado;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Editar_perfil extends Fragment {
    private EditText nombreEditText, correoEditText, nuevaContrasenaEditText, confirmarContrasenaEditText, contrasenaActualEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar_perfil, container, false);

        nombreEditText = view.findViewById(R.id.Eusuario);
        correoEditText = view.findViewById(R.id.Eemail);
        nuevaContrasenaEditText = view.findViewById(R.id.Epassword);
        confirmarContrasenaEditText = view.findViewById(R.id.EpasswordConfirm);
        contrasenaActualEditText = view.findViewById(R.id.EpasswordActual);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            cargarDatosUsuario();
        } else {
            Toast.makeText(getContext(), "No se ha autenticado ningún usuario", Toast.LENGTH_SHORT).show();
        }

        Button botonGuardar = view.findViewById(R.id.botEditar);
        botonGuardar.setOnClickListener(v -> actualizarPerfil());

        Button botonCancelar = view.findViewById(R.id.botCancelar);
        botonCancelar.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new Ver_perfil()).commit();
        });

        return view;
    }

    private void cargarDatosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombre = documentSnapshot.getString("nombre");
                            String correo = documentSnapshot.getString("correo");

                            nombreEditText.setText(nombre);
                            correoEditText.setText(correo);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "No se ha autenticado ningún usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarPerfil() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            String nuevoNombre = nombreEditText.getText().toString().trim();
            String nuevoCorreo = correoEditText.getText().toString().trim();
            String nuevaContrasena = nuevaContrasenaEditText.getText().toString().trim();
            String confirmarContrasena = confirmarContrasenaEditText.getText().toString().trim();
            String contrasenaActual = contrasenaActualEditText.getText().toString().trim();

            if (TextUtils.isEmpty(contrasenaActual)) {
                Toast.makeText(getContext(), "Debes ingresar tu contraseña actual para actualizar", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.isEmpty(nuevaContrasena) && !nuevaContrasena.equals(confirmarContrasena)) {
                Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = user.getEmail();
            if (email != null) {
                AuthCredential credential = EmailAuthProvider.getCredential(email, contrasenaActual);
                user.reauthenticate(credential).addOnSuccessListener(aVoid -> {

                    Map<String, Object> usuarioActualizado = new HashMap<>();
                    usuarioActualizado.put("nombre", nuevoNombre);
                    usuarioActualizado.put("correo", nuevoCorreo);

                    db.collection("usuarios").document(uid)
                            .update(usuarioActualizado)
                            .addOnSuccessListener(a -> Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar perfil", Toast.LENGTH_SHORT).show());

                    if (!TextUtils.isEmpty(nuevoCorreo) && !nuevoCorreo.equals(email)) {
                        user.verifyBeforeUpdateEmail(nuevoCorreo)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Correo actualizado. Verifica el correo enviado para confirmar", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Error al actualizar el correo", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    if (!TextUtils.isEmpty(nuevaContrasena)) {
                        user.updatePassword(nuevaContrasena)
                                .addOnSuccessListener(a -> Toast.makeText(getContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show());
                    }

                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, new Ver_perfil()).commit();

                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error en la reautenticación", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getContext(), "Correo electrónico no disponible", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No se ha autenticado ningún usuario", Toast.LENGTH_SHORT).show();
        }
    }
}
