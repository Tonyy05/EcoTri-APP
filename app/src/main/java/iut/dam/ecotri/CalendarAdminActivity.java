package iut.dam.ecotri;

import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import iut.dam.ecotri.data.ApiClient;
import iut.dam.ecotri.data.CalendarApi;
import iut.dam.ecotri.data.DayCalendarResponse;
import iut.dam.ecotri.data.SetDayRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarAdminActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvSelectedDate;

    private MaterialButton btnAddCollecte, btnDeleteCollecte;

    // Formulaire ajout
    private View layoutAddForm;
    private TextView tvAddInfo;
    private Spinner spTypeAdd;
    private MaterialButton btnConfirmAdd, btnCancelAdd;

    // Formulaire suppression
    private View layoutDeleteForm;
    private TextView tvDeleteInfo;
    private CheckBox cbVerte, cbJaune, cbVerre, cbVegetaux, cbEncombrants;
    private MaterialButton btnConfirmDelete, btnCancelDelete;

    private String selectedDateIso = null;      // "yyyy-MM-dd"
    private String selectedDateDisplay = "--/--/----";

    // Types actuellement en BDD pour la date sélectionnée
    private Set<String> currentTypesForDay = new HashSet<>();

    private CalendarApi calendarApi;
    private SessionManager session;

    // Liste de base des types + labels
    private static final String[][] ALL_TYPES = new String[][]{
            {"MENAGERES",   "Poubelle verte (MENAGERES)"},
            {"RECYCLABLES", "Poubelle jaune (RECYCLABLES)"},
            {"VERRE",       "Poubelle verre (VERRE)"},
            {"VEGETAUX",    "Déchets verts (VEGETAUX)"},
            {"ENCOMBRANTS", "Encombrants (ENCOMBRANTS)"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_admin);

        session = new SessionManager(this);
        calendarApi = ApiClient.INSTANCE.getCalendarApi();

        // Toolbar
        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tb.setNavigationOnClickListener(v -> finish());

        // --- Récup des vues ---
        calendarView      = findViewById(R.id.calendarView);
        tvSelectedDate    = findViewById(R.id.tvSelectedDate);
        btnAddCollecte    = findViewById(R.id.btnAddCollecte);
        btnDeleteCollecte = findViewById(R.id.btnDeleteCollecte);

        layoutAddForm  = findViewById(R.id.layoutAddForm);
        tvAddInfo      = findViewById(R.id.tvAddInfo);
        spTypeAdd      = findViewById(R.id.spTypeAdd);
        btnConfirmAdd  = findViewById(R.id.btnConfirmAdd);
        btnCancelAdd   = findViewById(R.id.btnCancelAdd);

        layoutDeleteForm = findViewById(R.id.layoutDeleteForm);
        tvDeleteInfo     = findViewById(R.id.tvDeleteInfo);
        cbVerte          = findViewById(R.id.cbVerte);
        cbJaune          = findViewById(R.id.cbJaune);
        cbVerre          = findViewById(R.id.cbVerre);
        cbVegetaux       = findViewById(R.id.cbVegetaux);
        cbEncombrants    = findViewById(R.id.cbEncombrants);
        btnConfirmDelete = findViewById(R.id.btnConfirmDelete);
        btnCancelDelete  = findViewById(R.id.btnCancelDelete);

        // Sélection de date dans le calendrier
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            int realMonth = month + 1;
            String dd = (dayOfMonth < 10 ? "0" : "") + dayOfMonth;
            String mm = (realMonth < 10 ? "0" : "") + realMonth;

            selectedDateIso = String.format(Locale.getDefault(),
                    "%04d-%s-%s", year, mm, dd);
            selectedDateDisplay = dd + "/" + mm + "/" + year;

            tvSelectedDate.setText("Jour sélectionné : " + selectedDateDisplay);

            tvAddInfo.setText("Ajouter une collecte le " + selectedDateDisplay);
            tvDeleteInfo.setText("Collectes prévues le " + selectedDateDisplay);

            // 🔄 On recharge les types existants pour ce jour depuis l'API
            loadDayFromApi();
        });

        // Bouton "Ajouter collecte" -> affiche formulaire d'ajout
        btnAddCollecte.setOnClickListener(v -> {
            if (selectedDateIso == null) {
                Toast.makeText(this,
                        "Sélectionne d'abord une date dans le calendrier",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            layoutDeleteForm.setVisibility(View.GONE);

            // Met à jour les choix possibles en fonction de currentTypesForDay
            setupSpinnerTypes();

            // Si le spinner est désactivé, ça veut dire qu'il n'y a rien à ajouter
            if (!spTypeAdd.isEnabled()) {
                Toast.makeText(this,
                        "Toutes les collectes possibles sont déjà prévues ce jour-là",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            layoutAddForm.setVisibility(View.VISIBLE);
        });

        // Bouton "Supprimer collecte" -> affiche formulaire suppression
        btnDeleteCollecte.setOnClickListener(v -> {
            if (selectedDateIso == null) {
                Toast.makeText(this,
                        "Sélectionne d'abord une date dans le calendrier",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            layoutAddForm.setVisibility(View.GONE);
            layoutDeleteForm.setVisibility(View.VISIBLE);

            // Pré-cocher/afficher les cases en fonction de currentTypesForDay
            updateDeleteCheckboxesFromCurrentTypes();
        });

        // Confirm ajout
        btnConfirmAdd.setOnClickListener(v -> confirmAdd());

        // Annuler ajout
        btnCancelAdd.setOnClickListener(v -> layoutAddForm.setVisibility(View.GONE));

        // Confirm suppression
        btnConfirmDelete.setOnClickListener(v -> confirmDelete());

        // Annuler suppression
        btnCancelDelete.setOnClickListener(v -> layoutDeleteForm.setVisibility(View.GONE));
    }

    /**
     * Charge depuis l'API les collectes prévues sur la date sélectionnée
     */
    private void loadDayFromApi() {
        if (selectedDateIso == null) return;

        calendarApi.getDay("Saint-Cloud", selectedDateIso)
                .enqueue(new Callback<DayCalendarResponse>() {
                    @Override
                    public void onResponse(Call<DayCalendarResponse> call,
                                           Response<DayCalendarResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            DayCalendarResponse day = response.body();
                            currentTypesForDay.clear();
                            currentTypesForDay.addAll(day.getTypes());
                        } else {
                            currentTypesForDay.clear();
                        }
                        // Met à jour le spinner et les checkbox si besoin
                        setupSpinnerTypes();
                        updateDeleteCheckboxesFromCurrentTypes();
                    }

                    @Override
                    public void onFailure(Call<DayCalendarResponse> call, Throwable t) {
                        Toast.makeText(CalendarAdminActivity.this,
                                "Erreur réseau (getDay) : " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Remplit le spinner avec les types qui NE sont PAS encore présents ce jour-là.
     */
    private void setupSpinnerTypes() {
        List<String> labels = new ArrayList<>();

        for (String[] t : ALL_TYPES) {
            String code  = t[0];
            String label = t[1];
            if (!currentTypesForDay.contains(code)) {
                labels.add(label);
            }
        }

        if (labels.isEmpty()) {
            // Tous les types sont déjà présents => pas besoin d'ajouter
            ArrayAdapter<String> adapterEmpty = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    new String[]{"Toutes les collectes sont déjà prévues ce jour-là"}
            );
            adapterEmpty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spTypeAdd.setAdapter(adapterEmpty);
            spTypeAdd.setEnabled(false);
        } else {
            spTypeAdd.setEnabled(true);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    labels
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spTypeAdd.setAdapter(adapter);
        }
    }

    private void confirmAdd() {
        if (selectedDateIso == null) {
            Toast.makeText(this,
                    "Aucune date sélectionnée",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!spTypeAdd.isEnabled()) {
            Toast.makeText(this,
                    "Aucun type de collecte à ajouter ce jour-là",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String label = (String) spTypeAdd.getSelectedItem();
        if (label == null || label.isEmpty()) {
            Toast.makeText(this,
                    "Choisis un type de collecte",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String typeCode = mapLabelToCode(label);

        // ajoute le type à l'ensemble courant
        currentTypesForDay.add(typeCode);

        // on envoie la liste complète à l'API
        sendSetDayRequest(new ArrayList<>(currentTypesForDay),
                "Collecte ajoutée le " + selectedDateDisplay);

        layoutAddForm.setVisibility(View.GONE);
    }

    private void confirmDelete() {
        if (selectedDateIso == null) {
            Toast.makeText(this,
                    "Aucune date sélectionnée",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Liste à supprimer
        List<String> toRemove = new ArrayList<>();
        if (cbVerte.getVisibility() == View.VISIBLE && cbVerte.isChecked())
            toRemove.add("MENAGERES");
        if (cbJaune.getVisibility() == View.VISIBLE && cbJaune.isChecked())
            toRemove.add("RECYCLABLES");
        if (cbVerre.getVisibility() == View.VISIBLE && cbVerre.isChecked())
            toRemove.add("VERRE");
        if (cbVegetaux.getVisibility() == View.VISIBLE && cbVegetaux.isChecked())
            toRemove.add("VEGETAUX");
        if (cbEncombrants.getVisibility() == View.VISIBLE && cbEncombrants.isChecked())
            toRemove.add("ENCOMBRANTS");

        if (toRemove.isEmpty()) {
            Toast.makeText(this,
                    "Sélectionne au moins une collecte à supprimer",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // on retire ces types de l'ensemble courant
        currentTypesForDay.removeAll(toRemove);

        // on envoie la liste restante à l'API
        sendSetDayRequest(new ArrayList<>(currentTypesForDay),
                "Collecte(s) mise(s) à jour le " + selectedDateDisplay);

        layoutDeleteForm.setVisibility(View.GONE);
    }

    private void sendSetDayRequest(List<String> typesToKeep, String successMsg) {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this,
                    "Token manquant, reconnecte-toi",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        SetDayRequest body = new SetDayRequest(selectedDateIso, typesToKeep);

        calendarApi.setDay("Bearer " + token, body)
                .enqueue(new Callback<DayCalendarResponse>() {
                    @Override
                    public void onResponse(Call<DayCalendarResponse> call,
                                           Response<DayCalendarResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            DayCalendarResponse day = response.body();
                            currentTypesForDay.clear();
                            currentTypesForDay.addAll(day.getTypes());

                            Toast.makeText(CalendarAdminActivity.this,
                                    successMsg,
                                    Toast.LENGTH_SHORT).show();

                            // On met à jour l'affichage des formulaires
                            setupSpinnerTypes();
                            updateDeleteCheckboxesFromCurrentTypes();

                        } else if (response.code() == 403) {
                            Toast.makeText(CalendarAdminActivity.this,
                                    "Accès refusé (pas le rôle MAIRIE ?)",
                                    Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 400) {
                            Toast.makeText(CalendarAdminActivity.this,
                                    "Erreur API set-day (400) : vérifier date / types",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CalendarAdminActivity.this,
                                    "Erreur API set-day : " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DayCalendarResponse> call, Throwable t) {
                        Toast.makeText(CalendarAdminActivity.this,
                                "Erreur réseau (setDay) : " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Affiche / masque les checkbox selon les types réellement présents ce jour-là
     */
    private void updateDeleteCheckboxesFromCurrentTypes() {
        // MENAGERES
        if (currentTypesForDay.contains("MENAGERES")) {
            cbVerte.setVisibility(View.VISIBLE);
            cbVerte.setChecked(false);
        } else {
            cbVerte.setVisibility(View.GONE);
        }

        // RECYCLABLES
        if (currentTypesForDay.contains("RECYCLABLES")) {
            cbJaune.setVisibility(View.VISIBLE);
            cbJaune.setChecked(false);
        } else {
            cbJaune.setVisibility(View.GONE);
        }

        // VERRE
        if (currentTypesForDay.contains("VERRE")) {
            cbVerre.setVisibility(View.VISIBLE);
            cbVerre.setChecked(false);
        } else {
            cbVerre.setVisibility(View.GONE);
        }

        // VEGETAUX
        if (currentTypesForDay.contains("VEGETAUX")) {
            cbVegetaux.setVisibility(View.VISIBLE);
            cbVegetaux.setChecked(false);
        } else {
            cbVegetaux.setVisibility(View.GONE);
        }

        // ENCOMBRANTS
        if (currentTypesForDay.contains("ENCOMBRANTS")) {
            cbEncombrants.setVisibility(View.VISIBLE);
            cbEncombrants.setChecked(false);
        } else {
            cbEncombrants.setVisibility(View.GONE);
        }
    }

    private String mapLabelToCode(String label) {
        if (label.contains("MENAGERES"))    return "MENAGERES";
        if (label.contains("RECYCLABLES"))  return "RECYCLABLES";
        if (label.contains("VERRE"))        return "VERRE";
        if (label.contains("VEGETAUX"))     return "VEGETAUX";
        if (label.contains("ENCOMBRANTS"))  return "ENCOMBRANTS";
        return "MENAGERES";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
