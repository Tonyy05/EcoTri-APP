package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
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

public class DecheteriesActivity extends AppCompatActivity {

    // ⚠️ Mets 10.0.2.2 si tu es sur émulateur Android Studio
    private static final String BASE_URL = "http://10.0.2.2:3000";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private DecheterieAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decheteries);

        // ===== RecyclerView Déchèteries =====
        RecyclerView rv = findViewById(R.id.rvDecheteries);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DecheterieAdapter(this);
        rv.setAdapter(adapter);

        loadDecheteriesFromApi();

        // ===== Bottom Navigation =====
        ImageButton btnJeu = findViewById(R.id.btnJeu);
        ImageButton btnCalendrier = findViewById(R.id.btnCalendrier);
        ImageButton btnDecheterie = findViewById(R.id.btnDecheterie);
        ImageButton btnProfil = findViewById(R.id.btnProfil);

        // Jeu / Quiz
        btnJeu.setOnClickListener(v -> {
            Intent i = new Intent(DecheteriesActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        // Calendrier
        btnCalendrier.setOnClickListener(v -> {
            Intent i = new Intent(DecheteriesActivity.this, CalendarActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        // Déchèteries (écran actuel → rien à faire)
        btnDecheterie.setOnClickListener(v -> {
            // Optionnel : refresh
            loadDecheteriesFromApi();
        });

        // Profil
        btnProfil.setOnClickListener(v -> {
            Intent i = new Intent(DecheteriesActivity.this, SettingsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
    }

    private void loadDecheteriesFromApi() {
        String url = BASE_URL + "/api/decheteries";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(DecheteriesActivity.this,
                                "GET decheteries FAIL: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(DecheteriesActivity.this,
                                    "GET " + response.code() + " : " + body,
                                    Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                Type listType = new TypeToken<List<Decheterie>>() {}.getType();
                List<Decheterie> list = gson.fromJson(body, listType);

                runOnUiThread(() -> adapter.setData(list));
            }
        });
    }
}
