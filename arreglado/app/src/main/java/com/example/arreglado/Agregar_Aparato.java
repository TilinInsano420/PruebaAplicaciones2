package com.example.arreglado;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Agregar_Aparato extends Fragment {

    private EditText nombreAparato, cantidadUsada;
    private RadioGroup radioGroupEnergia;
    private RadioButton radioButtonKwh, radioButtonWatts;
    private ImageView imageViewAparato;
    private Uri imageUri;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private Button agregarButton;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agregar__aparato, container, false);

        nombreAparato = view.findViewById(R.id.InputNomOBJ);
        cantidadUsada = view.findViewById(R.id.InputCantidadUtiliza);
        radioGroupEnergia = view.findViewById(R.id.radioGroup);
        radioButtonKwh = view.findViewById(R.id.radioKwh);
        radioButtonWatts = view.findViewById(R.id.radioWatts);
        imageViewAparato = view.findViewById(R.id.imageViewAparato);
        agregarButton = view.findViewById(R.id.button13);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("aparatos_imagenes");

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imageViewAparato.setImageURI(imageUri);
                    }
                });

        imageViewAparato.setOnClickListener(v -> openImagePicker());

        agregarButton.setOnClickListener(v -> agregarAparato());

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void agregarAparato() {
        String nombre = nombreAparato.getText().toString().trim();
        String cantidadStr = cantidadUsada.getText().toString().trim();
        String tipoEnergia = "";

        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(getActivity(), "Por favor, ingrese el nombre del aparato.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(cantidadStr)) {
            Toast.makeText(getActivity(), "Por favor, ingrese la cantidad utilizada.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (radioGroupEnergia.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getActivity(), "Por favor, seleccione el tipo de energía.", Toast.LENGTH_SHORT).show();
            return;
        }

        agregarButton.setEnabled(false);

        double cantidad = Double.parseDouble(cantidadStr);
        if (radioButtonKwh.isChecked()) {
            tipoEnergia = "kWh";
        } else if (radioButtonWatts.isChecked()) {
            tipoEnergia = "Watts";
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            if (imageUri != null) {
                uploadImageAndSaveData(nombre, cantidad, tipoEnergia, userId);
            } else {
                saveDataToFirestore(nombre, cantidad, tipoEnergia, null, userId);
            }
        } else {
            Toast.makeText(getActivity(), "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            agregarButton.setEnabled(true);
        }
    }

    private void uploadImageAndSaveData(String nombre, double cantidad, String tipoEnergia, String userId) {
        String imageFileName = UUID.randomUUID().toString();

        StorageReference fileReference = storageReference.child(imageFileName);
        fileReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {

                                saveDataToFirestore(nombre, cantidad, tipoEnergia, downloadUri.toString(), userId);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                        agregarButton.setEnabled(true);
                    }
                });
    }

    private void saveDataToFirestore(String nombre, double cantidad, String tipoEnergia, @Nullable String imageUrl, String userId) {
        Map<String, Object> aparatoData = new HashMap<>();
        aparatoData.put("nombre", nombre);
        aparatoData.put("cantidad", cantidad);
        aparatoData.put("tipoEnergia", tipoEnergia);
        aparatoData.put("userId", userId);

        if (imageUrl != null) {
            aparatoData.put("imageUrl", imageUrl);
        }

        db.collection("aparatos")
                .document(UUID.randomUUID().toString())
                .set(aparatoData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Aparato agregado exitosamente", Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                        agregarButton.setEnabled(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error al agregar el aparato", Toast.LENGTH_SHORT).show();
                        agregarButton.setEnabled(true);
                    }
                });
    }

    private void limpiarCampos() {
        nombreAparato.setText("");
        cantidadUsada.setText("");
        radioGroupEnergia.clearCheck();
        imageViewAparato.setImageResource(R.drawable.ic_menu_camera);
    }
}
