package com.example.arreglado;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Editar_Eliminar extends Fragment {

    private RecyclerView recyclerView;
    private List<Aparato> aparatoList;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar__eliminar, container, false);
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
                                aparato.setId(document.getId());
                                aparatoList.add(aparato);
                            }
                        }
                        AparatoRecyclerAdapter adapter = new AparatoRecyclerAdapter(context, aparatoList);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(context, "No se encontraron aparatos para este usuario.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error desconocido.";
                    Log.e("FirestoreError", "Error al obtener los aparatos: " + errorMessage);
                    Toast.makeText(context, "Error al cargar los aparatos: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "No estás autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    public class AparatoRecyclerAdapter extends RecyclerView.Adapter<AparatoRecyclerAdapter.AparatoViewHolder> {

        private final Context context;
        private final List<Aparato> aparatoList;

        public AparatoRecyclerAdapter(Context context, List<Aparato> aparatoList) {
            this.context = context;
            this.aparatoList = aparatoList;
        }

        @NonNull
        @Override
        public AparatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_aparato, parent, false);
            return new AparatoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AparatoViewHolder holder, int position) {
            Aparato aparato = aparatoList.get(position);
            holder.nombreAparato.setText("Nombre del aparato: " + (aparato.getNombre() != null ? aparato.getNombre() : "Sin nombre"));
            holder.cantidadAparato.setText("Cantidad utilizada: " + (aparato.getCantidad() > 0 ? String.valueOf(aparato.getCantidad()) : "N/A"));
            holder.tipoGasto.setText("Tipo de energía: " + (aparato.getTipoEnergia() != null ? aparato.getTipoEnergia() : "No especificado"));

            if (aparato.getImageUrl() != null && !aparato.getImageUrl().isEmpty()) {
                Glide.with(context).load(aparato.getImageUrl()).into(holder.imagenAparato);
            } else {
                holder.imagenAparato.setImageResource(R.drawable.ic_menu_camera);
            }

            holder.itemView.setOnClickListener(v -> mostrarOpciones(aparato));
        }

        @Override
        public int getItemCount() {
            return aparatoList.size();
        }

        public class AparatoViewHolder extends RecyclerView.ViewHolder {
            TextView nombreAparato, cantidadAparato, tipoGasto;
            ImageView imagenAparato;

            public AparatoViewHolder(@NonNull View itemView) {
                super(itemView);
                nombreAparato = itemView.findViewById(R.id.nombreAparato);
                cantidadAparato = itemView.findViewById(R.id.cantidadAparato);
                tipoGasto = itemView.findViewById(R.id.tipoGastoAparato);
                imagenAparato = itemView.findViewById(R.id.imagenAparato);
            }
        }

        private void mostrarOpciones(Aparato aparato) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Seleccionar opción")
                    .setMessage("¿Qué deseas hacer con este aparato?")
                    .setPositiveButton("Eliminar", (dialog, which) -> eliminarAparato(aparato))
                    .setNeutralButton("Editar", (dialog, which) -> {
                        Fragment editarEliminarAparatoFragment = new Editar_Eliminar_Aparato(aparato.getId());
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, editarEliminarAparatoFragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }

        private void eliminarAparato(Aparato aparato) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Confirmar eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar este aparato y su imagen?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        String aparatoId = aparato.getId();
                        String imageUrl = aparato.getImageUrl();

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                            storageReference.delete().addOnSuccessListener(aVoid -> eliminarDocumentoDeFirestore(aparatoId))
                                    .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            eliminarDocumentoDeFirestore(aparatoId);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        private void eliminarDocumentoDeFirestore(String aparatoId) {
            firestore.collection("aparatos").document(aparatoId).delete()
                    .addOnSuccessListener(aVoid -> {
                        for (Aparato aparato : aparatoList) {
                            if (aparato.getId().equals(aparatoId)) {
                                aparatoList.remove(aparato);
                                break;
                            }
                        }
                        notifyDataSetChanged();
                        Toast.makeText(context, "Aparato y su imagen eliminados", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar el aparato: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    public static class Aparato {
        private double cantidad;
        private String imageUrl;
        private String nombre;
        private String tipoEnergia;
        private String id;

        public Aparato() {
        }

        public Aparato(double cantidad, String imageUrl, String nombre, String tipoEnergia, String id) {
            this.cantidad = cantidad;
            this.imageUrl = imageUrl;
            this.nombre = nombre;
            this.tipoEnergia = tipoEnergia;
            this.id = id;
        }

        public double getCantidad() {
            return cantidad;
        }

        public void setCantidad(double cantidad) {
            this.cantidad = cantidad;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getTipoEnergia() {
            return tipoEnergia;
        }

        public void setTipoEnergia(String tipoEnergia) {
            this.tipoEnergia = tipoEnergia;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
