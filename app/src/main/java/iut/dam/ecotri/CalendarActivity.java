package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import iut.dam.ecotri.data.ApiClient;
import iut.dam.ecotri.data.CalendarApi;
import iut.dam.ecotri.data.DayCalendarResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;

    // Légendes (cartes)
    private MaterialCardView legendBacVerte;
    private MaterialCardView legendBacJaune;
    private MaterialCardView legendBacVerre;
    private MaterialCardView legendBacVegetaux;
    private MaterialCardView legendEncombrants;

    // Nav bar
    private ImageButton btnJeu, btnCalendrier, btnDecheterie, btnProfil;

    private SessionManager sessionManager;
    private boolean isSaintCloudUser = false;

    private CalendarApi calendarApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // ----- Session / adresse -----
        sessionManager = new SessionManager(this);
        String adresse = sessionManager.getAdresse();
        isSaintCloudUser = isAdresseInSaintCloud(adresse);

        calendarApi = ApiClient.INSTANCE.getCalendarApi();

        // ----- Vues -----
        calendarView      = findViewById(R.id.calendarView);
        legendBacVerte    = findViewById(R.id.legendBacVerte);
        legendBacJaune    = findViewById(R.id.legendBacJaune);
        legendBacVerre    = findViewById(R.id.legendBacVerre);
        legendBacVegetaux = findViewById(R.id.legendBacVegetaux);
        legendEncombrants = findViewById(R.id.legendEncombrants);

        // Par défaut : on cache tout
        hideAllLegends();

        if (!isSaintCloudUser) {
            Toast.makeText(this,
                    "Vous n'habitez pas dans la ville de cette application",
                    Toast.LENGTH_LONG).show();
        }

        // Listener sur le calendrier
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {

            if (!isSaintCloudUser) {
                // On remonte un petit message et on cache tout
                hideAllLegends();
                Toast.makeText(this,
                        "Vous n'habitez pas dans la ville de cette application",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int realMonth = month + 1;
            String dd = (dayOfMonth < 10 ? "0" : "") + dayOfMonth;
            String mm = (realMonth < 10 ? "0" : "") + realMonth;
            String dateIso = String.format(Locale.getDefault(),
                    "%04d-%s-%s", year, mm, dd);

            loadDayFromApi(dateIso);
        });

        // ----- Barre de navigation (en bas) -----
        btnJeu        = findViewById(R.id.btnJeu);
        btnCalendrier = findViewById(R.id.btnCalendrier);
        btnDecheterie = findViewById(R.id.btnDecheterie);
        btnProfil     = findViewById(R.id.btnProfil);

        Button btnDemanderBac = findViewById(R.id.btnDemandeBac);

        btnDemanderBac.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, DemandeBacActivity.class);
            startActivity(intent);
        });

        btnJeu.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        btnCalendrier.setOnClickListener(v ->
                Toast.makeText(this, "Vous êtes déjà sur le calendrier", Toast.LENGTH_SHORT).show());

        btnDecheterie.setOnClickListener(v -> {
            startActivity(new Intent(this, DecheteriesActivity.class));
        });

        btnProfil.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void loadDayFromApi(String dateIso) {
        calendarApi.getDay("Saint-Cloud", dateIso)
                .enqueue(new Callback<DayCalendarResponse>() {
                    @Override
                    public void onResponse(Call<DayCalendarResponse> call,
                                           Response<DayCalendarResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            DayCalendarResponse body = response.body();
                            Set<String> types = new HashSet<>(body.getTypes());
                            updateLegends(types);
                        } else if (response.code() == 403) {
                            hideAllLegends();
                            Toast.makeText(CalendarActivity.this,
                                    "Calendrier disponible uniquement pour Saint-Cloud",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Pas de collecte ou erreur : on cache tout
                            hideAllLegends();
                        }
                    }

                    @Override
                    public void onFailure(Call<DayCalendarResponse> call, Throwable t) {
                        hideAllLegends();
                        Toast.makeText(CalendarActivity.this,
                                "Erreur réseau calendrier : " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateLegends(Set<String> types) {
        hideAllLegends();

        if (types == null || types.isEmpty()) {
            return; // pas de collecte => rien affiché
        }

        if (types.contains("MENAGERES")) {
            legendBacVerte.setVisibility(View.VISIBLE);
        }
        if (types.contains("RECYCLABLES")) {
            legendBacJaune.setVisibility(View.VISIBLE);
        }
        if (types.contains("VERRE")) {
            legendBacVerre.setVisibility(View.VISIBLE);
        }
        if (types.contains("VEGETAUX")) {
            legendBacVegetaux.setVisibility(View.VISIBLE);
        }
        if (types.contains("ENCOMBRANTS")) {
            legendEncombrants.setVisibility(View.VISIBLE);
        }
    }

    private void hideAllLegends() {
        legendBacVerte.setVisibility(View.GONE);
        legendBacJaune.setVisibility(View.GONE);
        legendBacVerre.setVisibility(View.GONE);
        legendBacVegetaux.setVisibility(View.GONE);
        legendEncombrants.setVisibility(View.GONE);
    }

    /**
     * Retourne true si l'adresse semble être à Saint-Cloud.
     */
    private boolean isAdresseInSaintCloud(String adresse) {
        if (adresse == null) return false;
        String a = adresse.toLowerCase(Locale.FRANCE);

        return a.contains("saint-cloud")
                || a.contains("saint cloud")
                || a.contains("92210");
    }
}
