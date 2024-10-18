package com.example.arreglado;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Agregar_Boleta extends Fragment {

    private EditText adminServicioInput, transporteInput, consumoInput, valorKwhInput;
    private TextView tvFechaSeleccionada;
    private Button botSelectDate, botAgregarBoleta;
    private String fechaSeleccionada;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private static final String CHANNEL_ID = "BoletaChannel";
    private static final int NOTIFICATION_ID = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agregar__boleta, container, false);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        adminServicioInput = view.findViewById(R.id.AdminServicioinput);
        transporteInput = view.findViewById(R.id.Transporteinput);
        consumoInput = view.findViewById(R.id.Consumoinput);
        valorKwhInput = view.findViewById(R.id.ValorKwhinput);
        tvFechaSeleccionada = view.findViewById(R.id.tvFechaSeleccionada);
        botSelectDate = view.findViewById(R.id.botSelectDate);
        botAgregarBoleta = view.findViewById(R.id.botAgregarBoleta);

        botSelectDate.setOnClickListener(v -> mostrarDatePicker());
        botAgregarBoleta.setOnClickListener(v -> guardarBoleta());

        createNotificationChannel();
        return view;
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (getContext() == null) {
            Toast.makeText(getActivity(), "Error al obtener contexto", Toast.LENGTH_SHORT).show();
            return;
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            fechaSeleccionada = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            tvFechaSeleccionada.setText("Fecha: " + fechaSeleccionada);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void guardarBoleta() {
        String adminServicio = adminServicioInput.getText().toString().trim();
        String transporte = transporteInput.getText().toString().trim();
        String consumo = consumoInput.getText().toString().trim();
        String valorKwh = valorKwhInput.getText().toString().trim();

        if (adminServicio.isEmpty() || transporte.isEmpty() || consumo.isEmpty() || valorKwh.isEmpty() || fechaSeleccionada == null) {
            Toast.makeText(getContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Bloquear el botón mientras se guarda la boleta
        botAgregarBoleta.setEnabled(false);
        botAgregarBoleta.setText("Guardando...");

        Map<String, Object> boletaData = new HashMap<>();
        boletaData.put("adminServicio", adminServicio);
        boletaData.put("transporte", transporte);
        boletaData.put("consumo", consumo);
        boletaData.put("valorKwh", valorKwh);
        boletaData.put("fecha", fechaSeleccionada);

        firestore.collection("users").document(userId).collection("boletas").add(boletaData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Boleta guardada exitosamente", Toast.LENGTH_SHORT).show();
                    mostrarNotificacion("Boleta agregada", "La boleta se guardó correctamente.");
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al guardar boleta", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    // Reactivar el botón después de intentar guardar
                    botAgregarBoleta.setEnabled(true);
                    botAgregarBoleta.setText("Agregar Boleta");
                });
    }

    private void limpiarCampos() {
        adminServicioInput.setText("");
        transporteInput.setText("");
        consumoInput.setText("");
        valorKwhInput.setText("");
        tvFechaSeleccionada.setText("Fecha: No seleccionada");
    }

    private void createNotificationChannel() {
        Context context = getContext();
        if (context == null) {
            Toast.makeText(getActivity(), "Error al crear canal de notificación", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Boleta Channel";
            String description = "Notificaciones de boleta";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void mostrarNotificacion(String title, String content) {
        Context context = getContext();
        if (context == null) {
            Toast.makeText(getActivity(), "Error al mostrar notificación", Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // Cambia el ícono aquí
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
                return;
            }
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
