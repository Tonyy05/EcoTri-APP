package iut.dam.ecotri;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import iut.dam.ecotri.data.ApiClient;
import iut.dam.ecotri.data.DemandeBacApi;
import iut.dam.ecotri.data.DemandeBacRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Écran pour envoyer une demande de bac
public class DemandeBacActivity extends AppCompatActivity {

    private TextInputEditText etAdresse, etCommentaire;
    private AutoCompleteTextView autoTypeBac;
    private AutoCompleteTextView autoRaison;
    private MaterialButton btnEnvoyer;

    private SessionManager session;
    private DemandeBacApi demandeBacApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demande_bac);

        session = new SessionManager(this);
        demandeBacApi = ApiClient.INSTANCE.getDemandeBacApi();

        // ----- Toolbar + flèche retour -----
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ----- Vues -----
        etAdresse = findViewById(R.id.etAdresse);
        etCommentaire = findViewById(R.id.etCommentaire);
        autoTypeBac = findViewById(R.id.autoTypeBac);
        autoRaison = findViewById(R.id.autoRaison);
        btnEnvoyer = findViewById(R.id.btnEnvoyerDemande);

        String preType = getIntent().getStringExtra("prefill_typeBac");
        String preRaison = getIntent().getStringExtra("prefill_raison");
        String preCom = getIntent().getStringExtra("prefill_commentaire");

        // typeBac -> label UI
        if (preType != null) {
            switch (preType) {
                case "MENAGERES": autoTypeBac.setText("Bac ménagères (vert)", false); break;
                case "RECYCLABLES": autoTypeBac.setText("Bac recyclables (jaune)", false); break;
                case "VERRE": autoTypeBac.setText("Bac verre", false); break;
                case "VEGETAUX": autoTypeBac.setText("Bac végétaux", false); break;
                case "ENCOMBRANTS": autoTypeBac.setText("Bac encombrants", false); break;
            }
        }

        if (preRaison != null) {
            switch (preRaison) {
                case "CASSE": autoRaison.setText("Casse", false); break;
                case "VOLE": autoRaison.setText("Vol", false); break;
                case "PREMIER_BAC": autoRaison.setText("Premier bac", false); break;
            }
        }

        if (preCom != null && !preCom.trim().isEmpty()) {
            etCommentaire.setText(preCom);
        }

        // Adresse pré-remplie à partir du compte (même si on ne l'envoie plus à l'API)
        String adresse = session.getAdresse();
        if (!TextUtils.isEmpty(adresse)) {
            etAdresse.setText(adresse);
        }

        // ----- Liste déroulante : type de bac -----
        String[] typesBacLabels = new String[]{
                "Bac ménagères (vert)",
                "Bac recyclables (jaune)",
                "Bac verre",
                "Bac végétaux",
                "Bac encombrants"
        };

        ArrayAdapter<String> adapterTypes = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                typesBacLabels
        );
        autoTypeBac.setAdapter(adapterTypes);

        // ----- Liste déroulante : raison -----
        String[] raisonsLabels = new String[]{
                "Casse",
                "Vol",
                "Premier bac"
        };

        ArrayAdapter<String> adapterRaisons = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                raisonsLabels
        );
        autoRaison.setAdapter(adapterRaisons);

        // ----- Clic sur "Envoyer la demande" -----
        btnEnvoyer.setOnClickListener(v -> sendDemande());
    }

    private void sendDemande() {
        String adresse = etAdresse.getText() != null ? etAdresse.getText().toString().trim() : "";
        String labelType = autoTypeBac.getText() != null ? autoTypeBac.getText().toString().trim() : "";
        String labelRaison = autoRaison.getText() != null ? autoRaison.getText().toString().trim() : "";
        String commentaireLibre = etCommentaire.getText() != null ? etCommentaire.getText().toString().trim() : "";

        // ----- Vérifs locales -----
        if (TextUtils.isEmpty(adresse)) {
            Toast.makeText(this, "Adresse manquante dans votre profil", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(labelType)) {
            Toast.makeText(this, "Choisissez un type de bac", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(labelRaison)) {
            Toast.makeText(this, "Choisissez une raison", Toast.LENGTH_SHORT).show();
            return;
        }

        // ----- Mapping typeBac → valeur API -----
        String typeBacApi;
        switch (labelType) {
            case "Bac ménagères (vert)":
                typeBacApi = "MENAGERES";
                break;
            case "Bac recyclables (jaune)":
                typeBacApi = "RECYCLABLES";
                break;
            case "Bac verre":
                typeBacApi = "VERRE";
                break;
            case "Bac végétaux":
                typeBacApi = "VEGETAUX";
                break;
            case "Bac encombrants":
                typeBacApi = "ENCOMBRANTS";
                break;
            default:
                Toast.makeText(this, "Type de bac invalide", Toast.LENGTH_SHORT).show();
                return;
        }

        // ----- Mapping raison → valeur API -----
        String raisonApi;
        switch (labelRaison) {
            case "Casse":
                raisonApi = "CASSE";
                break;
            case "Vol":
                raisonApi = "VOLE";
                break;
            case "Premier bac":
                raisonApi = "PREMIER_BAC";
                break;
            default:
                Toast.makeText(this, "Raison invalide", Toast.LENGTH_SHORT).show();
                return;
        }

        // ----- Composition du commentaire pour l'API -----
        String commentaireApi = "Raison : " + labelRaison;
        if (!TextUtils.isEmpty(commentaireLibre)) {
            commentaireApi += " | Détails : " + commentaireLibre;
        }

        // Récupération du token pour l'Authorization
        String token = session.getToken();
        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Erreur : utilisateur non connecté (token manquant)", Toast.LENGTH_LONG).show();
            return;
        }

        // Construction du body pour l'API (typeBac, raison, commentaire)
        DemandeBacRequest body = new DemandeBacRequest(typeBacApi, raisonApi, commentaireApi);

        // Appel API via Retrofit
        Call<Void> call = demandeBacApi.createDemande("Bearer " + token, body);
        btnEnvoyer.setEnabled(false);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnEnvoyer.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(DemandeBacActivity.this,
                            "Demande envoyée avec succès",
                            Toast.LENGTH_LONG).show();
                    finish(); // revenir à l'écran précédent
                } else {
                    String msg = "Erreur " + response.code();
                    Toast.makeText(DemandeBacActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnEnvoyer.setEnabled(true);
                Toast.makeText(DemandeBacActivity.this,
                        "Erreur réseau : " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
