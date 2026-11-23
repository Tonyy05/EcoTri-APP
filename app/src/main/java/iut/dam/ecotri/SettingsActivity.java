package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private MaterialButton btnCompte, btnNotifications;
    private ImageButton navJeu, navCalendrier, navDecheterie, navProfil;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        session = new SessionManager(this);

        // Boutons principaux
        btnCompte = findViewById(R.id.btnCompte);
        btnNotifications = findViewById(R.id.btnNotifications);


        // Barre de nav
        navJeu = findViewById(R.id.navJeu);
        navCalendrier = findViewById(R.id.navCalendrier);
        navDecheterie = findViewById(R.id.navDecheterie);
        navProfil = findViewById(R.id.navProfil);

        // --- Navigation des boutons principaux ---
        btnCompte.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationSettingsActivity.class)));



            // --- Barre de navigation du bas ---
            navJeu.setOnClickListener(v ->
                    startActivity(new Intent(this, MainActivity.class)));

            navCalendrier.setOnClickListener(v ->
                    startActivity(new Intent(this, CalendarActivity.class)));

            navDecheterie.setOnClickListener(v -> {
                startActivity(new Intent(this, DecheteriesActivity.class));

            });

            navProfil.setOnClickListener(v -> {
                // On est déjà dans les réglages, tu peux laisser vide ou relancer SettingsActivity si tu veux
            });

    }
}
