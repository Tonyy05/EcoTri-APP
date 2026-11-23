package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LeaderboardActivity extends AppCompatActivity {

    private static final String TAG = "LEADERBOARD";
    private static final String BASE_URL = "http://10.0.2.2:3000/api/scores/leaderboard";

    private final OkHttpClient client = new OkHttpClient();

    private Button btnReload, btnBack;
    private RecyclerView recycler;

    private final List<LeaderboardAdapter.Item> items = new ArrayList<>();
    private LeaderboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        btnReload = findViewById(R.id.btnReloadLeaderboard);
        btnBack = findViewById(R.id.btnBackLeaderboard);
        recycler = findViewById(R.id.recyclerLeaderboard);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(items);
        recycler.setAdapter(adapter);


        btnBack.setOnClickListener(v -> finish());
        btnReload.setOnClickListener(v -> loadLeaderboard());
        Button btnBackHome = findViewById(R.id.btnBackHome);

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderboardActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        loadLeaderboard();
    }




    private void loadLeaderboard() {
        SessionManager session = new SessionManager(this);
        String token = session.getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Token manquant (connecte-toi)", Toast.LENGTH_LONG).show();
            return;
        }


        String type;

        type = "all";

        String url = BASE_URL + "?type=" + type + "&limit=50";
        Log.d(TAG, "GET " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token) // si tu rends public tu peux enlever
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Erreur réseau", e);
                runOnUiThread(() -> Toast.makeText(LeaderboardActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String json = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "HTTP " + response.code() + " => " + json);

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(LeaderboardActivity.this, "Erreur serveur : " + response.code(), Toast.LENGTH_LONG).show());
                    return;
                }

                try {
                    JSONObject root = new JSONObject(json);
                    JSONArray arr = root.getJSONArray("scores");

                    List<LeaderboardAdapter.Item> temp = new ArrayList<>();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject s = arr.getJSONObject(i);

                        Object pObj = s.opt("points");
                        int points = 0;
                        if (pObj instanceof Number) points = ((Number) pObj).intValue();
                        else if (pObj != null) {
                            try { points = Integer.parseInt(String.valueOf(pObj)); } catch (Exception ignored) {}
                        }

                        JSONObject user = s.optJSONObject("User");
                        String name = "Joueur";
                        if (user != null) {
                            String first = user.optString("firstName", "");
                            String last = user.optString("lastName", "");
                            String full = (first + " " + last).trim();
                            if (!full.isEmpty()) name = full;
                            else name = user.optString("email", "Joueur");
                        }

                        temp.add(new LeaderboardAdapter.Item(i + 1, name, points));
                    }

                    runOnUiThread(() -> {
                        items.clear();
                        items.addAll(temp);
                        adapter.notifyDataSetChanged();

                        if (items.isEmpty()) {
                            Toast.makeText(LeaderboardActivity.this, "Aucun score", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Erreur parsing", e);
                    runOnUiThread(() -> Toast.makeText(LeaderboardActivity.this, "Erreur parsing leaderboard", Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}
