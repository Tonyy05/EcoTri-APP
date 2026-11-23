package iut.dam.ecotri;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuizActivity extends AppCompatActivity {

    private TextView txtQuestion;
    private RadioGroup radioGroup;
    private Button btnNext;
    private Button BtnQuit;

    private JSONArray questionsArray;
    private JSONArray userAnswers = new JSONArray();
    private int score = 0;

    private int currentQuestion = 0;
    private int quizId = 1;



    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        txtQuestion = findViewById(R.id.txtQuestion);
        radioGroup = findViewById(R.id.radioGroup);
        btnNext = findViewById(R.id.btnNext);
        BtnQuit = findViewById(R.id.btnQuit);

        quizId = getIntent().getIntExtra("quizId", 1);
        loadQuiz(quizId);

        btnNext.setOnClickListener(v -> showNextQuestion());
        BtnQuit.setOnClickListener(v -> {
            finish();
        });


    }

    private void loadQuiz(int quizId) {
        String url = "http://10.0.2.2:3000/api/quiz/" + quizId;

        SessionManager session = new SessionManager(this);
        String token = session.getToken();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";

                if (response.code() == 429) {
                    String msg = "Déjà fait aujourd'hui.";
                    try {
                        JSONObject o = new JSONObject(body);
                        String next = o.optString("nextAvailable", null);
                        if (next != null) msg = buildRemainingTimeMessage(next); // si tu veux
                        else msg = o.optString("message", msg);
                    } catch (Exception ignored) {}

                    String finalMsg = msg;
                    runOnUiThread(() -> {
                        Toast.makeText(QuizActivity.this, finalMsg, Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(QuizActivity.this, "Erreur quiz (" + response.code() + ")", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                try {
                    JSONObject quiz = new JSONObject(body);
                    questionsArray = quiz.getJSONArray("questions");
                    runOnUiThread(() -> showQuestion(0));
                } catch (Exception e) {
                    Log.e("QUIZ_ERROR", "Parsing quiz: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(QuizActivity.this, "Quiz invalide (questions)", Toast.LENGTH_LONG).show());
                }
            }
        });
    }


    private void showQuestion(int index) {
        try {
            JSONObject questionObj = questionsArray.getJSONObject(index);

            String questionText = questionObj.getString("text");
            txtQuestion.setText(questionText);

            JSONArray answers = questionObj.getJSONArray("answers");

            radioGroup.removeAllViews();

            for (int i = 0; i < answers.length(); i++) {
                JSONObject answer = answers.getJSONObject(i);
                RadioButton rb = new RadioButton(this);
                rb.setText(answer.getString("text"));
                rb.setTag(answer.getInt("id"));
                radioGroup.addView(rb);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitQuiz() {
        try {

            // Récupérer le token ici
            SessionManager session = new SessionManager(this);
            String token = session.getToken();

            Log.d("QUIZ_DEBUG", "Token utilisé = " + token);

            JSONObject body = new JSONObject();
            body.put("answers", userAnswers);

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:3000/api/quiz/" + quizId + "/submit")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(okhttp3.RequestBody.create(
                            body.toString(),
                            okhttp3.MediaType.parse("application/json")
                    ))
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(QuizActivity.this, "Erreur réseau", Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body() != null ? response.body().string() : "";
                    Log.d("QUIZ_DEBUG", "HTTP " + response.code() + " => " + body);

                    if (response.code() == 429) {
                        String msg = "Déjà fait aujourd'hui.";
                        try {
                            JSONObject o = new JSONObject(body);
                            String next = o.optString("nextAvailable", null);
                            if (next != null) msg = buildRemainingTimeMessage(next);
                            else msg = o.optString("message", msg);
                        } catch (Exception ignored) {}

                        String finalMsg = msg;
                        runOnUiThread(() -> Toast.makeText(QuizActivity.this, finalMsg, Toast.LENGTH_LONG).show());
                        return;
                    }

                    if (!response.isSuccessful()) {
                        runOnUiThread(() ->
                                Toast.makeText(QuizActivity.this, "Erreur quiz (" + response.code() + ")", Toast.LENGTH_LONG).show()
                        );
                        return;
                    }

                    try {
                        JSONObject result = new JSONObject(body);
                        int score = result.getInt("score");
                        int total = result.getInt("total");
                        JSONArray results = result.getJSONArray("results");


                        runOnUiThread(() -> {
                            Intent intent = new Intent(QuizActivity.this, QuizResultsActivity.class);
                            intent.putExtra("score", score);
                            intent.putExtra("total", total);
                            intent.putExtra("questions", questionsArray.toString());
                            intent.putExtra("results", results.toString());
                            startActivity(intent);
                            finish();
                        });

                    } catch (Exception e) {
                        Log.e("QUIZ_DEBUG", "Parsing submit: " + e.getMessage());
                        runOnUiThread(() ->
                                Toast.makeText(QuizActivity.this, "Réponse serveur invalide", Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void showNextQuestion() {

        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Sélectionne une réponse", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selected = findViewById(radioGroup.getCheckedRadioButtonId());
        int selectedAnswerId = (int) selected.getTag();

        try {
            JSONObject questionObj = questionsArray.getJSONObject(currentQuestion);
            int questionId = questionObj.getInt("id");

            JSONObject answerObj = new JSONObject();
            answerObj.put("questionId", questionId);
            answerObj.put("answerId", selectedAnswerId);

            userAnswers.put(answerObj);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // On passe à la question suivante
        currentQuestion++;



        // ➤ FIN DU QUIZ ?
        if (currentQuestion >= questionsArray.length()) {
            submitQuiz(); // ← APPEL IMPORTANT
            return;
        }

        showQuestion(currentQuestion);
    }

    private String buildRemainingTimeMessage(String isoDate) {
        try {
            java.text.SimpleDateFormat iso =
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
            iso.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

            java.util.Date next = iso.parse(isoDate);
            long diffMs = next.getTime() - System.currentTimeMillis();

            if (diffMs <= 0) return "Tu peux rejouer maintenant 🎉";

            long minutes = diffMs / 60000;
            long hours = minutes / 60;
            minutes = minutes % 60;

            if (hours > 0) {
                return "⏳ Reviens dans " + hours + "h " + minutes + "min";
            } else {
                return "⏳ Reviens dans " + minutes + "min";
            }
        } catch (Exception e) {
            return "⏳ Reviens plus tard";
        }
    }
}
