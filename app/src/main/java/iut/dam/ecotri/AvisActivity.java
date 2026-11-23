package iut.dam.ecotri;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AvisActivity extends AppCompatActivity implements AvisAdapter.OnAvisActionListener {

    private static final String TAG = "AvisActivity";
    private static final String BASE_URL = "http://10.0.2.2:3000";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private ImageButton btnRetour;
    private Button btnOuvrirForm;
    private androidx.cardview.widget.CardView cardForm;

    private OkHttpClient client;
    private Gson gson;

    private String decheterieId;
    private String decheterieNom;

    private TextView tvTitre;
    private RecyclerView recyclerAvis;
    private EditText etComment;
    private EditText etNote;
    private Switch switchAnonyme;
    private Button btnPublier;

    private AvisAdapter avisAdapter;
    private final List<Avis> avisList = new ArrayList<>();

    private SessionManager session;
    private Integer myUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avis);

        client = new OkHttpClient();
        gson = new Gson();
        session = new SessionManager(this);

        // ✅ IMPORTANT : id du user connecté
        // -> adapte si ta méthode s'appelle autrement
        myUserId = session.getUserId();

        decheterieId = getIntent().getStringExtra("DECHE_ID");
        decheterieNom = getIntent().getStringExtra("DECHE_NOM");

        if (decheterieId == null || decheterieId.trim().isEmpty()) {
            Toast.makeText(this, "ID déchèterie manquant", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvTitre = findViewById(R.id.tvTitre);
        recyclerAvis = findViewById(R.id.recyclerAvis);
        etComment = findViewById(R.id.etComment);
        etNote = findViewById(R.id.etNote);
        switchAnonyme = findViewById(R.id.switchAnonyme);
        btnPublier = findViewById(R.id.btnPublier);

        btnRetour = findViewById(R.id.btnRetour);
        btnOuvrirForm = findViewById(R.id.btnOuvrirForm);
        cardForm = findViewById(R.id.cardForm);

        btnRetour.setOnClickListener(v -> finish());

        btnOuvrirForm.setOnClickListener(v -> {
            if (cardForm.getVisibility() == View.GONE) {
                cardForm.setVisibility(View.VISIBLE);
                btnOuvrirForm.setText("Fermer");
            } else {
                cardForm.setVisibility(View.GONE);
                btnOuvrirForm.setText("Publier un avis");
            }
        });

        tvTitre.setText("Avis - " + (decheterieNom != null ? decheterieNom : ("Déchèterie #" + decheterieId)));

        recyclerAvis.setLayoutManager(new LinearLayoutManager(this));
        avisAdapter = new AvisAdapter(avisList, myUserId, this);
        recyclerAvis.setAdapter(avisAdapter);

        loadAvis();
        btnPublier.setOnClickListener(v -> publishAvis());
    }

    private void loadAvis() {
        String url = BASE_URL + "/api/decheteries/" + decheterieId + "/avis";
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "GET avis onFailure", e);
                runOnUiThread(() ->
                        Toast.makeText(AvisActivity.this, "GET FAIL: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "GET avis code=" + response.code() + " body=" + body);

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(AvisActivity.this, "GET " + response.code() + ": " + body, Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                Type listType = new TypeToken<List<Avis>>() {}.getType();
                List<Avis> list = gson.fromJson(body, listType);

                if (list == null) list = new ArrayList<>();

                // ✅ Trier : mon avis en premier
                Integer finalMyUserId = myUserId;
                Collections.sort(list, (a, b) -> {
                    boolean aMine = finalMyUserId != null && a.userId != null && a.userId.equals(finalMyUserId);
                    boolean bMine = finalMyUserId != null && b.userId != null && b.userId.equals(finalMyUserId);
                    if (aMine && !bMine) return -1;
                    if (!aMine && bMine) return 1;
                    return 0;
                });

                List<Avis> finalList = list;
                runOnUiThread(() -> {
                    avisList.clear();
                    avisList.addAll(finalList);
                    avisAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void publishAvis() {
        String token = session.getToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Tu dois être connecté pour publier un avis.", Toast.LENGTH_LONG).show();
            return;
        }

        String commentaire = etComment.getText().toString().trim();
        String noteStr = etNote.getText().toString().trim();

        if (commentaire.length() < 2) {
            Toast.makeText(this, "Commentaire trop court", Toast.LENGTH_LONG).show();
            return;
        }

        int note;
        try {
            note = Integer.parseInt(noteStr);
        } catch (Exception e) {
            Toast.makeText(this, "Note invalide (1-5)", Toast.LENGTH_LONG).show();
            return;
        }

        if (note < 1 || note > 5) {
            Toast.makeText(this, "La note doit être entre 1 et 5", Toast.LENGTH_LONG).show();
            return;
        }

        boolean anonyme = switchAnonyme != null && switchAnonyme.isChecked();
        String auteur = (session.getFirstName() + " " + session.getLastName()).trim();
        if (auteur.isEmpty()) auteur = "Utilisateur";

        try {
            JSONObject json = new JSONObject();
            json.put("note", note);
            json.put("commentaire", commentaire);
            json.put("anonyme", anonyme);
            if (!anonyme) json.put("auteur", auteur);

            RequestBody requestBody = RequestBody.create(json.toString(), JSON);
            String url = BASE_URL + "/api/decheteries/" + decheterieId + "/avis";

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "POST avis onFailure", e);
                    runOnUiThread(() ->
                            Toast.makeText(AvisActivity.this, "POST FAIL: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "POST avis code=" + response.code() + " body=" + body);

                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            if (response.code() == 409) {
                                Toast.makeText(AvisActivity.this,
                                        "Tu as déjà publié un avis. Tu peux le supprimer puis en refaire un.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(AvisActivity.this, "POST " + response.code() + ": " + body, Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(AvisActivity.this, "Avis publié ✅", Toast.LENGTH_SHORT).show();
                        etComment.setText("");
                        etNote.setText("");
                        if (switchAnonyme != null) switchAnonyme.setChecked(false);
                        cardForm.setVisibility(View.GONE);
                        btnOuvrirForm.setText("Publier un avis");
                    });

                    loadAvis();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "JSON error", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ✅ Callback adapter : suppression de mon avis
    @Override
    public void onDelete(Avis avis) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer votre avis ?")
                .setMessage("Cette action est définitive. Voulez-vous continuer ?")
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    dialog.dismiss();
                    deleteAvis(avis); // ✅ on appelle la vraie suppression
                })
                .show();
    }
    private void deleteAvis(Avis avis) {
        String token = session.getToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Tu dois être connecté.", Toast.LENGTH_LONG).show();
            return;
        }

        String url = BASE_URL + "/api/decheteries/" + decheterieId + "/avis/" + avis.id;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "DELETE avis onFailure", e);
                runOnUiThread(() ->
                        Toast.makeText(AvisActivity.this, "DELETE FAIL: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "DELETE avis code=" + response.code() + " body=" + body);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(AvisActivity.this, "Avis supprimé ✅", Toast.LENGTH_SHORT).show();
                        loadAvis();
                    } else {
                        Toast.makeText(AvisActivity.this, "DELETE " + response.code() + ": " + body, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


}
