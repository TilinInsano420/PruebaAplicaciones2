package com.example.arreglado;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class Ver_gastos_mes extends Fragment {

    private RecyclerView recyclerView;
    private AparatoAdapter aparatoAdapter;
    private List<Aparato> aparatoList;
    private TextView totalCostTextView;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ver_gastos_mes, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAparatos);
        totalCostTextView = view.findViewById(R.id.txt_valor_aparatos);
        aparatoList = new ArrayList<>();
        aparatoAdapter = new AparatoAdapter(aparatoList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(aparatoAdapter);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        cargarAparatos();

        return view;
    }

    private void cargarAparatos() {
        String userId = null;
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        if (userId != null) {
            firestore.collection("usuarios").document(userId).collection("aparatos")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        aparatoList.clear();
                        double totalCost = 0;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Aparato aparato = document.toObject(Aparato.class);
                            aparatoList.add(aparato);
                            totalCost += aparato.getCantidad();
                        }
                        aparatoAdapter.notifyDataSetChanged();
                        totalCostTextView.setText("Costo total: $" + totalCost);
                    });
        } else {
            totalCostTextView.setText("Usuario no autenticado");
        }
    }

    public static class Aparato {
        private String nombre;
        private double cantidad;
        private String tipoEnergia;
        private String imageUrl;

        public Aparato() {
        }

        public Aparato(String nombre, double cantidad, String tipoEnergia, String imageUrl) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.tipoEnergia = tipoEnergia;
            this.imageUrl = imageUrl;
        }

        public String getNombre() {
            return nombre;
        }

        public double getCantidad() {
            return cantidad;
        }

        public String getTipoEnergia() {
            return tipoEnergia;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

    public static class AparatoAdapter extends RecyclerView.Adapter<AparatoAdapter.AparatoViewHolder> {

        private final List<Aparato> aparatoList;

        public AparatoAdapter(List<Aparato> aparatoList) {
            this.aparatoList = aparatoList;
        }

        @NonNull
        @Override
        public AparatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aparato, parent, false);
            return new AparatoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AparatoViewHolder holder, int position) {
            Aparato aparato = aparatoList.get(position);
            holder.nombreTextView.setText(aparato.getNombre());
            holder.cantidadTextView.setText(String.valueOf(aparato.getCantidad()));
            holder.tipoEnergiaTextView.setText(aparato.getTipoEnergia());

            Glide.with(holder.itemView.getContext())
                    .load(aparato.getImageUrl())
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return aparatoList.size();
        }

        public static class AparatoViewHolder extends RecyclerView.ViewHolder {

            TextView nombreTextView;
            TextView cantidadTextView;
            TextView tipoEnergiaTextView;
            ImageView imageView;

            public AparatoViewHolder(@NonNull View itemView) {
                super(itemView);
                nombreTextView = itemView.findViewById(R.id.nombreAparato);
                cantidadTextView = itemView.findViewById(R.id.cantidadAparato);
                tipoEnergiaTextView = itemView.findViewById(R.id.tipoGastoAparato);
                imageView = itemView.findViewById(R.id.imagenAparato);
            }
        }
    }
}