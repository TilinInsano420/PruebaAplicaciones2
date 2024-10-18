package com.example.arreglado;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Ver_perfil extends Fragment {
    private TextView nombreTextView, correoTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_ver_perfil, container, false);

        // Inicializar las vistas
        nombreTextView = view.findViewById(R.id.NomUs);
        correoTextView = view.findViewById(R.id.Email1);
        Button botonEditarPerfil = view.findViewById(R.id.btnEditarPerfil);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Verificar si hay un usuario autenticado
        if (mAuth.getCurrentUser() != null) {
            cargarDatosUsuario();
        } else {
            Toast.makeText(getActivity(), "No se ha autenticado ningún usuario", Toast.LENGTH_SHORT).show();
        }

        // Configurar el botón para abrir el fragmento de editar perfil
        botonEditarPerfil.setOnClickListener(v -> {
            // Crear una instancia del fragmento Editar_perfil
            Editar_perfil editarPerfilFragment = new Editar_perfil();

            // Reemplazar el fragmento actual con el de editar perfil
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, editarPerfilFragment) // Asegúrate de que 'fragment_container' sea el ID correcto del contenedor de fragmentos
                    .addToBackStack(null) // Permitir que el usuario regrese al fragmento anterior
                    .commit();
        });

        return view;
    }

    private void cargarDatosUsuario() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombre = documentSnapshot.getString("nombre");
                            String correo = documentSnapshot.getString("correo");

                            nombreTextView.setText("Nombre de usuario: " + nombre);
                            correoTextView.setText("Email: " + correo);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error al cargar perfil", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getActivity(), "No se ha autenticado ningún usuario", Toast.LENGTH_SHORT).show();
        }
    }
}
