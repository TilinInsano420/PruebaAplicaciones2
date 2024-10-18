package com.example.arreglado;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class Historial extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        ConstraintLayout cuadroResumen = view.findViewById(R.id.cuadroResumen);

        cuadroResumen.setOnClickListener(v -> {
            Historial_resumen historialResumenFragment = new Historial_resumen();

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, historialResumenFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
