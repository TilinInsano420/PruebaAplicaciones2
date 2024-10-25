package com.example.arreglado;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.UUID;

public class Editar_Eliminar_Aparato extends Fragment {

    private EditText editTextNombre, editTextCantidad;
    private ImageView imageViewAparato;
    private Button buttonActualizar, buttonCancelar;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private final String aparatoId;
    private String imageUrl;
    private Uri nuevaImagenUri;

    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                            nuevaImagenUri = result.getData().getData();
                            Glide.with(this).load(nuevaImagenUri).into(imageViewAparato);
                        }
                    });

    public Editar_Eliminar_Aparato(String aparatoId) {
        this.aparatoId = aparatoId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar__eliminar__aparato, container, false);

        editTextNombre = view.findViewById(R.id.InputNomOBJ);
        editTextCantidad = view.findViewById(R.id.InputCantidadUtiliza);
        imageViewAparato = view.findViewById(R.id.imageViewAparato);
        buttonActualizar = view.findViewById(R.id.buttonEditarAparato);
        buttonCancelar = view.findViewById(R.id.buttonCancelarEditarAparato);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        cargarDatosAparato();


        imageViewAparato.setOnClickListener(v -> seleccionarImagen());

        buttonCancelar.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        buttonActualizar.setOnClickListener(v -> {
            if (TextUtils.isEmpty(editTextNombre.getText()) || TextUtils.isEmpty(editTextCantidad.getText())) {
                Toast.makeText(getContext(), "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            bloquearBotones(true);
            actualizarAparato();
        });

        return view;
    }

    private void cargarDatosAparato() {
        DocumentReference aparatoRef = firestore.collection("aparatos").document(aparatoId);
        aparatoRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String nombre = documentSnapshot.getString("nombre");
                Double cantidad = documentSnapshot.getDouble("cantidad");
                imageUrl = documentSnapshot.getString("imageUrl");

                editTextNombre.setText(nombre);
                editTextCantidad.setText(String.valueOf(cantidad));

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    if (getContext() != null) {
                        Glide.with(getContext()).load(imageUrl).into(imageViewAparato);
                    }
                }
            } else {
                Toast.makeText(getContext(), "No se encontró el aparato", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Log.e("FirestoreError", "Error al cargar el aparato", e));
    }

    private void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        seleccionarImagenLauncher.launch(Intent.createChooser(intent, "Seleccionar imagen"));
    }

    private void actualizarAparato() {
        String nombre = editTextNombre.getText().toString();
        double cantidad = Double.parseDouble(editTextCantidad.getText().toString());

        if (nuevaImagenUri != null) {
            subirImagen(nombre, cantidad);
        } else {
            actualizarDatosFirestore(nombre, cantidad, imageUrl);
        }
    }

    private void subirImagen(String nombre, double cantidad) {
        StorageReference storageRef = storage.getReference();
        final String fileName = "aparatos/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(nuevaImagenUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            imageUrl = uri.toString();
                            actualizarDatosFirestore(nombre, cantidad, imageUrl);
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    bloquearBotones(false);
                });
    }

    private void actualizarDatosFirestore(String nombre, double cantidad, String imageUrl) {
        DocumentReference aparatoRef = firestore.collection("aparatos").document(aparatoId);
        aparatoRef.update("nombre", nombre, "cantidad", cantidad, "imageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Aparato actualizado", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar aparato", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> bloquearBotones(false));
    }

    private void bloquearBotones(boolean bloquear) {
        buttonActualizar.setEnabled(!bloquear);
        buttonCancelar.setEnabled(!bloquear);
    }
}
