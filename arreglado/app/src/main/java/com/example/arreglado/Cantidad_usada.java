package com.example.arreglado;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class Cantidad_usada extends Fragment {

    private EditText editTextCantidad;
    private RadioGroup radioGroupTipoUso;
    private Button buttonAgregarCantidad, buttonCancelar;
    private FirebaseFirestore db;

    private String nombreAparato;
    private int cantidadUtiliza;
    private String tipoEnergia;

    public Cantidad_usada() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cantidad_usada, container, false);

        editTextCantidad = view.findViewById(R.id.editTextCantidad);
        radioGroupTipoUso = view.findViewById(R.id.radioGroupTipoUso);
        buttonAgregarCantidad = view.findViewById(R.id.buttonAgregarCantidad);
        buttonCancelar = view.findViewById(R.id.buttonCancelar);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            nombreAparato = getArguments().getString("nombre");
            cantidadUtiliza = getArguments().getInt("cantidadUtiliza");
            tipoEnergia = getArguments().getString("tipoEnergia");

            ((TextView) view.findViewById(R.id.textViewNombreAparato)).setText("Aparato: " + nombreAparato);
            ((TextView) view.findViewById(R.id.textViewCantidadUtiliza)).setText("Cantidad que utiliza: " + cantidadUtiliza);
            ((TextView) view.findViewById(R.id.textViewTipoEnergia)).setText("Tipo de energía: " + tipoEnergia);
        }

        buttonAgregarCantidad.setOnClickListener(v -> agregarCantidad());

        buttonCancelar.setOnClickListener(v -> cancelar());

        return view;
    }

    private void agregarCantidad() {
        String cantidadStr = editTextCantidad.getText().toString();
        if (!cantidadStr.isEmpty()) {
            int cantidad = Integer.parseInt(cantidadStr);

            int selectedId = radioGroupTipoUso.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRadioButton = requireView().findViewById(selectedId);
                String tipoUso = selectedRadioButton.getText().toString();

                db.collection("usos").add(new Uso(nombreAparato, cantidad, tipoUso))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getActivity(), "Cantidad agregada correctamente", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error al agregar cantidad: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getActivity(), "Seleccione un tipo de uso", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelar() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    public static class Uso {
        private String nombreAparato;
        private int cantidad;
        private String tipoUso;

        public Uso() {
        }

        public Uso(String nombreAparato, int cantidad, String tipoUso) {
            this.nombreAparato = nombreAparato;
            this.cantidad = cantidad;
            this.tipoUso = tipoUso;
        }

        public String getNombreAparato() {
            return nombreAparato;
        }

        public void setNombreAparato(String nombreAparato) {
            this.nombreAparato = nombreAparato;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public String getTipoUso() {
            return tipoUso;
        }

        public void setTipoUso(String tipoUso) {
            this.tipoUso = tipoUso;
        }
    }
}
