package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ScoreHistoryActivity extends AppCompatActivity {

    private static final String TAG = "HISTORY_DEBUG";
    private static final String BASE_URL = "http://10.0.2.2:3000/api/scores/history";

    private final OkHttpClient client = new OkHttpClient();

    private RecyclerView recycler;
    private ScoreHistoryAdapter adapter;
    private final List<ScoreHistoryAdapter.Row> rows = new ArrayList<>();

    private Spinner spinnerQuiz, spinnerSort, spinnerOrder, spinnerType;
    private SwitchCompat switchGroupByQuiz;
    private Button btnApply, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);

        recycler = findViewById(R.id.recyclerScores);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScoreHistoryAdapter(rows);
        recycler.setAdapter(adapter);

        spinnerQuiz = findViewById(R.id.spinnerQuiz);
        spinnerSort = findViewById(R.id.spinnerSort);
        spinnerOrder = findViewById(R.id.spinnerOrder);
        spinnerType = findViewById(R.id.spinnerType);
        switchGroupByQuiz = findViewById(R.id.switchGroupByQuiz);
        btnApply = findViewById(R.id.btnApply);

        Button btnBackHome = findViewById(R.id.btnBackHome);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(ScoreHistoryActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        setupSpinners();

        btnApply.setOnClickListener(v -> loadScores());

        // Chargement initial
        loadScores();
    }

    private void setupSpinners() {
        // Quiz : ids 1/2/3 + "Tous"
        List<String> quizOptions = new ArrayList<>();
        quizOptions.add("Tous les quiz");
        quizOptions.add("Quiz facile (1)");
        quizOptions.add("Quiz moyen (2)");
        quizOptions.add("Quiz difficile (3)");

        List<String> typeOptions = new ArrayList<>();
        typeOptions.add("Quiz");
        typeOptions.add("Jeu de tri");

        spinnerType.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                typeOptions
        ));

        spinnerQuiz.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                quizOptions
        ));

        // Tri
        List<String> sortOptions = new ArrayList<>();
        sortOptions.add("Date");
        sortOptions.add("Score");

        spinnerSort.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                sortOptions
        ));

        // Ordre
        List<String> orderOptions = new ArrayList<>();
        orderOptions.add("Décroissant");
        orderOptions.add("Croissant");

        spinnerOrder.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                orderOptions
        ));

        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                boolean isQuiz = (position == 0);

                // Si "Jeu de tri" → pas de filtre quiz et pas de groupBy quiz
                spinnerQuiz.setEnabled(isQuiz);
                spinnerQuiz.setAlpha(isQuiz ? 1f : 0.4f);

                switchGroupByQuiz.setEnabled(isQuiz);
                switchGroupByQuiz.setChecked(isQuiz && switchGroupByQuiz.isChecked());
                switchGroupByQuiz.setAlpha(isQuiz ? 1f : 0.4f);

                // Optionnel : recharge auto
                loadScores();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadScores() {
        SessionManager session = new SessionManager(this);
        String token = session.getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Token manquant (connecte-toi)", Toast.LENGTH_LONG).show();
            return;
        }

        String url = buildUrlFromFilters();
        Log.d(TAG, "GET " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Erreur réseau", e);
                runOnUiThread(() ->
                        Toast.makeText(ScoreHistoryActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "HTTP " + response.code() + " => " + json);

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(ScoreHistoryActivity.this,
                                    "Erreur serveur : " + response.code(),
                                    Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                try {
                    JSONObject root = new JSONObject(json);
                    boolean grouped = root.optBoolean("grouped", false);

                    List<ScoreHistoryAdapter.Row> temp = new ArrayList<>();

                    if (grouped) {
                        JSONArray groups = root.getJSONArray("groups");

                        for (int g = 0; g < groups.length(); g++) {
                            JSONObject group = groups.getJSONObject(g);
                            String quizTitle = group.optString("quizTitle", "Quiz");

                            temp.add(new ScoreHistoryAdapter.HeaderRow(quizTitle));

                            JSONArray scores = group.getJSONArray("scores");
                            for (int i = 0; i < scores.length(); i++) {
                                JSONObject s = scores.getJSONObject(i);

                                int points = s.optInt("points", 0);
                                String date = formatDate(s.optString("datePartie", "N/A"));

                                // On affiche aussi le titre dans la ligne score (ou tu peux mettre "—")
                                temp.add(new ScoreHistoryAdapter.ScoreRow(quizTitle, points, date));
                            }
                        }
                    } else {
                        JSONArray scores = root.getJSONArray("scores");

                        for (int i = 0; i < scores.length(); i++) {
                            JSONObject s = scores.getJSONObject(i);

                            int points = s.optInt("points", 0);
                            String date = formatDate(s.optString("datePartie", "N/A"));

                            String quizTitle = "Jeu de tri";
                            JSONObject quizObj = s.optJSONObject("Quiz");
                            if (quizObj != null) quizTitle = quizObj.optString("title", "Quiz");

                            temp.add(new ScoreHistoryAdapter.ScoreRow(quizTitle, points, date));

                        }
                    }

                    runOnUiThread(() -> {
                        rows.clear();
                        rows.addAll(temp);
                        adapter.notifyDataSetChanged();

                        if (rows.isEmpty()) {
                            Toast.makeText(ScoreHistoryActivity.this, "Aucun score", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Erreur parsing", e);
                    runOnUiThread(() ->
                            Toast.makeText(ScoreHistoryActivity.this,
                                    "Erreur parsing historique",
                                    Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private String buildUrlFromFilters() {
        // base
        StringBuilder sb = new StringBuilder(BASE_URL);

        // params
        boolean isQuiz = (spinnerType.getSelectedItemPosition() == 0);
        sb.append("?type=").append(isQuiz ? "quiz" : "game");

        // quizId

        if (isQuiz) {
            // quizId
            int quizPos = spinnerQuiz.getSelectedItemPosition();
            if (quizPos == 1) sb.append("&quizId=1");
            if (quizPos == 2) sb.append("&quizId=2");
            if (quizPos == 3) sb.append("&quizId=3");

            // groupBy
            if (switchGroupByQuiz.isChecked()) sb.append("&groupBy=quiz");
            else sb.append("&groupBy=none");
        } else {
            // game : pas de groupBy quiz
            sb.append("&groupBy=none");
        }


        // sort
        int sortPos = spinnerSort.getSelectedItemPosition();
        if (sortPos == 1) sb.append("&sort=score");
        else sb.append("&sort=date");

        // order
        int orderPos = spinnerOrder.getSelectedItemPosition();
        if (orderPos == 1) sb.append("&order=asc");
        else sb.append("&order=desc");

        return sb.toString();
    }

    private String formatDate(String isoDate) {
        try {
            // ISO reçu : 2026-01-11T16:06:37.000Z
            SimpleDateFormat isoFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = isoFormat.parse(isoDate);

            // Format affiché : dd/MM/yyyy HH:mm
            SimpleDateFormat displayFormat =
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            return displayFormat.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }
}
