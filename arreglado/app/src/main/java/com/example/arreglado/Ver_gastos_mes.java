package com.example.arreglado;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class Ver_gastos_mes extends Fragment {

    private TextView txtMontoAhorrado, txtValorBoleta, txtValorAparatos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ver_gastos_mes, container, false);

        txtMontoAhorrado = view.findViewById(R.id.txt_monto_ahorrado);
        txtValorBoleta = view.findViewById(R.id.txt_valor_boleta);
        txtValorAparatos = view.findViewById(R.id.txt_valor_aparatos);

        txtMontoAhorrado.setText("2000 CLP");
        txtValorBoleta.setText("5000 CLP");
        txtValorAparatos.setText("3000 CLP");

        return view;
    }
}
