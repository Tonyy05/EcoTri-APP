package iut.dam.ecotri;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuizSelectActivity extends AppCompatActivity {

    private Spinner spinnerQuiz;
    private Button btnStart, btnLastResult, btnBackHome;

    private OkHttpClient client = new OkHttpClient();
    private final ArrayList<Integer> quizIds = new ArrayList<>();
    private final ArrayList<String> quizTitles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_select);

        spinnerQuiz = findViewById(R.id.spinnerQuiz);
        btnStart = findViewById(R.id.btnStart);
        btnLastResult = findViewById(R.id.btnLastResult);
        btnBackHome = findViewById(R.id.btnBackHome);

        loadQuizzes();

        btnStart.setOnClickListener(v -> {
            int pos = spinnerQuiz.getSelectedItemPosition();
            if (pos < 0 || pos >= quizIds.size()) {
                Toast.makeText(this, "Aucun quiz disponible", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedQuizId = quizIds.get(pos);
            Intent i = new Intent(QuizSelectActivity.this, QuizActivity.class);
            i.putExtra("quizId", selectedQuizId);
            startActivity(i);
        });

        // ✅ Aller à QuizResultsActivity depuis QuizSelectActivity (dernier score enregistré)
        btnLastResult.setOnClickListener(v -> {
            Intent i = new Intent(QuizSelectActivity.this, ScoreHistoryActivity.class);
            i.putExtra("mode", "history");
            startActivity(i);
        });

        btnBackHome.setOnClickListener(v -> finish());
    }

    private void loadQuizzes() {
        String url = "http://10.0.2.2:3000/api/quiz";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(QuizSelectActivity.this, "Erreur réseau", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                try {
                    JSONArray arr = new JSONArray(json);

                    quizIds.clear();
                    quizTitles.clear();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject q = arr.getJSONObject(i);
                        int id = q.getInt("id");
                        String title = q.optString("title", "Quiz " + id);
                        boolean isActive = q.optInt("isActive", 1) == 1;

                        // Option : ignorer les quiz inactifs
                        if (!isActive) continue;

                        quizIds.add(id);
                        quizTitles.add(title);
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                QuizSelectActivity.this,
                                android.R.layout.simple_spinner_item,
                                quizTitles
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerQuiz.setAdapter(adapter);
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(QuizSelectActivity.this, "Erreur lecture quiz", Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
}
