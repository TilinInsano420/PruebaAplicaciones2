package com.example.arreglado;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Ver_Usos extends Fragment {

    private RecyclerView recyclerView;
    private List<Aparato> aparatoList;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ver__usos, container, false);
        context = getContext();

        recyclerView = view.findViewById(R.id.recyclerViewAparatos);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        aparatoList = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        cargarAparatos();

        return view;
    }

    private void cargarAparatos() {
        if (currentUser != null) {
            Query aparatosQuery = firestore.collection("aparatos")
                    .whereEqualTo("userId", currentUser.getUid());

            aparatosQuery.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        aparatoList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Aparato aparato = document.toObject(Aparato.class);
                            if (aparato != null) {
                                aparatoList.add(aparato);
                            }
                        }

                        AparatoAdapter adapter = new AparatoAdapter(context, aparatoList);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(context, "No se encontraron aparatos para este usuario.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error desconocido.";
                    Toast.makeText(context, "Error al cargar los aparatos: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "No estás autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    public class AparatoAdapter extends RecyclerView.Adapter<AparatoAdapter.AparatoViewHolder> {

        private final Context context;
        private final List<Aparato> aparatoList;

        public AparatoAdapter(Context context, List<Aparato> aparatoList) {
            this.context = context;
            this.aparatoList = aparatoList;
        }

        @NonNull
        @Override
        public AparatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_aparato_detallado, parent, false);
            return new AparatoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AparatoViewHolder holder, int position) {
            Aparato aparato = aparatoList.get(position);
            holder.nombreAparato.setText("Nombre del aparato: " + (aparato.getNombre() != null ? aparato.getNombre() : "Sin nombre"));


            holder.cantidadUsada.setText("Cantidad usada: No definido");
            holder.tipoUso.setText("Tipo de uso: No definido");


            firestore.collection("usos")
                    .whereEqualTo("nombreAparato", aparato.getNombre())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot usoDoc = queryDocumentSnapshots.getDocuments().get(0);
                            Uso uso = usoDoc.toObject(Uso.class);
                            if (uso != null) {
                                holder.cantidadUsada.setText("Cantidad usada: " + uso.getCantidad());
                                holder.tipoUso.setText("Tipo de uso: " + uso.getTipoUso());
                            }
                        }
                    });

            holder.itemView.setOnClickListener(v -> {
                Cantidad_usada cantidadUsadaFragment = new Cantidad_usada();
                Bundle bundle = new Bundle();
                bundle.putString("nombre", aparato.getNombre());
                bundle.putInt("cantidadUtiliza", aparato.getCantidad());
                bundle.putString("tipoEnergia", aparato.getTipoEnergia());
                cantidadUsadaFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, cantidadUsadaFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        @Override
        public int getItemCount() {
            return aparatoList.size();
        }

        public class AparatoViewHolder extends RecyclerView.ViewHolder {
            TextView nombreAparato, cantidadUsada, tipoUso;

            public AparatoViewHolder(@NonNull View itemView) {
                super(itemView);
                nombreAparato = itemView.findViewById(R.id.nombreAparato);
                cantidadUsada = itemView.findViewById(R.id.cantidadUsada);
                tipoUso = itemView.findViewById(R.id.tipoUso);
            }
        }
    }

    public static class Aparato {
        private String nombre;
        private int cantidad;
        private String tipoEnergia;

        public Aparato() {}

        public Aparato(String nombre, int cantidad, String tipoEnergia) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.tipoEnergia = tipoEnergia;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public String getTipoEnergia() {
            return tipoEnergia;
        }

        public void setTipoEnergia(String tipoEnergia) {
            this.tipoEnergia = tipoEnergia;
        }
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
