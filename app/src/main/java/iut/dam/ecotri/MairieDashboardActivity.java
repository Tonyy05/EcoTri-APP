package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class MairieDashboardActivity extends AppCompatActivity {

    private MaterialButton btnGestionDemandes, btnGestionCalendrier, btnGestionAvis;

    private SessionManager session;
    private MaterialButton btnCompteMairie, btnDeconnexionMairie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mairie_dashboard);

        session = new SessionManager(this);

        // Sécurité : si un utilisateur normal arrive ici par erreur => déconnexion
        if (!"MAIRIE".equalsIgnoreCase(session.getRole())) {
            session.logout();
            finish();
            return;
        }

        btnGestionDemandes = findViewById(R.id.btnGestionDemandes);
        btnGestionCalendrier = findViewById(R.id.btnGestionCalendrier);
        btnCompteMairie = findViewById(R.id.btnCompteMairie);
        btnDeconnexionMairie = findViewById(R.id.btnDeconnexionMairie);

        MaterialToolbar toolbar = findViewById(R.id.toolbarMairie);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        // ✅ Bouton "Gestion du compte" -> ouvre les réglages / compte
        btnCompteMairie.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // ✅ Bouton "Déconnexion"
        btnDeconnexionMairie.setOnClickListener(v -> {
            session.logout();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        btnGestionDemandes.setOnClickListener(v ->
                startActivity(new Intent(this, GestionDemandesBacActivity.class)));

        btnGestionCalendrier.setOnClickListener(v ->
                startActivity(new Intent(this, CalendarAdminActivity.class)));

        btnGestionAvis = findViewById(R.id.btnGestionAvis);

        btnGestionAvis.setOnClickListener(v -> {
            Intent i = new Intent(MairieDashboardActivity.this, GestionAvisDecheteriesActivity.class);
            startActivity(i);
        });

    }
}
