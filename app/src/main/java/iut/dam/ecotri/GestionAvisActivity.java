package iut.dam.ecotri;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GestionAvisActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:3000";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private SessionManager session;

    private String decheterieId;
    private String decheterieNom;

    private AvisAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_avis);

        session = new SessionManager(this);

        decheterieId = getIntent().getStringExtra("DECHE_ID");
        decheterieNom = getIntent().getStringExtra("DECHE_NOM");

        if (decheterieId == null || decheterieId.trim().isEmpty()) {
            Toast.makeText(this, "ID déchèterie manquant", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvTitle = findViewById(R.id.tvTitle);
        RecyclerView rv = findViewById(R.id.rvAvis);

        btnBack.setOnClickListener(v -> finish());

        tvTitle.setText("Avis - " + (decheterieNom != null ? decheterieNom : ("Déchèterie #" + decheterieId)));

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AvisAdminAdapter(avis -> deleteAvis(avis));
        rv.setAdapter(adapter);

        loadAvis();
    }

    private void loadAvis() {
        String url = BASE_URL + "/api/decheteries/" + decheterieId + "/avis";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(GestionAvisActivity.this,
                                "GET avis FAIL: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(GestionAvisActivity.this,
                                    "GET " + response.code() + ": " + body,
                                    Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                Type listType = new TypeToken<List<Avis>>() {}.getType();
                List<Avis> list = gson.fromJson(body, listType);

                runOnUiThread(() -> adapter.setData(list));
            }
        });
    }

    private void deleteAvis(Avis avis) {
        String token = session.getToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Token manquant, reconnecte-toi.", Toast.LENGTH_LONG).show();
            return;
        }

        // avis.id doit exister dans ton modèle Avis
        String url = BASE_URL + "/api/decheteries/" + decheterieId + "/avis/" + avis.id;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(GestionAvisActivity.this,
                                "DELETE FAIL: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(GestionAvisActivity.this,
                                    "DELETE " + response.code() + ": " + body,
                                    Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                runOnUiThread(() ->
                        Toast.makeText(GestionAvisActivity.this,
                                "Avis supprimé ✅",
                                Toast.LENGTH_SHORT).show()
                );

                loadAvis();
            }
        });
    }
}
