package com.example.arreglado;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class Editar_Eliminar_Aparato extends Fragment {

    private final String aparatoId;
    private EditText inputNombre, inputCantidad;
    private RadioGroup radioGroup;
    private RadioButton radioButtonKwh, radioButtonWatts;
    private FirebaseFirestore firestore;

    public Editar_Eliminar_Aparato(String aparatoId) {
        this.aparatoId = aparatoId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar__eliminar__aparato, container, false);

        inputNombre = view.findViewById(R.id.InputNomOBJ);
        inputCantidad = view.findViewById(R.id.InputCantidadUtiliza);
        radioGroup = view.findViewById(R.id.radioGroup);
        radioButtonKwh = view.findViewById(R.id.radioButtonKwh);
        radioButtonWatts = view.findViewById(R.id.radioButtonWatts);
        Button buttonEditar = view.findViewById(R.id.buttonEditarAparato);
        Button buttonCancelar = view.findViewById(R.id.buttonCancelarEditarAparato);

        firestore = FirebaseFirestore.getInstance();

        buttonEditar.setOnClickListener(v -> editarAparato());
        buttonCancelar.setOnClickListener(v -> cancelarEdicion());

        return view;
    }

    private void editarAparato() {
        String nombre = inputNombre.getText().toString().trim();
        String cantidadStr = inputCantidad.getText().toString().trim();
        double cantidad;

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(cantidadStr)) {
            Toast.makeText(getContext(), "Debes completar todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            cantidad = Double.parseDouble(cantidadStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "La cantidad debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipoEnergia;
        if (radioButtonKwh.isChecked()) {
            tipoEnergia = "kWh";
        } else if (radioButtonWatts.isChecked()) {
            tipoEnergia = "Watts";
        } else {
            Toast.makeText(getContext(), "Debes seleccionar una unidad de medida", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("aparatos").document(aparatoId)
                .update("nombre", nombre, "cantidad", cantidad, "tipoEnergia", tipoEnergia)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Aparato actualizado", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cancelarEdicion() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
