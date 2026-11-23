package iut.dam.ecotri;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class CorrectionDetailActivity extends AppCompatActivity {

    private TextView txtTitle, txtYourAnswer, txtGoodAnswer, txtExplanation;
    private Button btnPrev, btnNext, btnBack;

    private JSONArray resultsArr;
    private JSONArray questionsArr;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correction_detail);

        txtTitle = findViewById(R.id.txtTitle);
        txtYourAnswer = findViewById(R.id.txtYourAnswer);
        txtGoodAnswer = findViewById(R.id.txtGoodAnswer);
        txtExplanation = findViewById(R.id.txtExplanation);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        index = getIntent().getIntExtra("index", 0);

        try {
            String resultsStr = getIntent().getStringExtra("results");
            String questionsStr = getIntent().getStringExtra("questions");
            resultsArr = resultsStr != null ? new JSONArray(resultsStr) : new JSONArray();
            questionsArr = questionsStr != null ? new JSONArray(questionsStr) : new JSONArray();
        } catch (Exception e) {
            resultsArr = new JSONArray();
            questionsArr = new JSONArray();
        }

        render();

        btnPrev.setOnClickListener(v -> {
            if (index > 0) {
                index--;
                render();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (index < resultsArr.length() - 1) {
                index++;
                render();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void render() {
        try {
            JSONObject res = resultsArr.getJSONObject(index);

            int questionId = res.optInt("questionId", -1);
            int userAnswerId = res.optInt("userAnswerId", -1);
            int correctAnswerId = res.optInt("correctAnswerId", -1);

            JSONObject q = findQuestionById(questionId);
            String questionText = q != null ? q.optString("text", "") : "";
            txtTitle.setText((index + 1) + ". " + questionText);

            // ✅ Réponse choisie + bonne réponse (dans q.answers)
            String yourAnswerText = findAnswerText(q, userAnswerId);
            String goodAnswerText = findAnswerText(q, correctAnswerId);

            txtYourAnswer.setText("Votre réponse : " + (yourAnswerText.isEmpty() ? "Aucune" : yourAnswerText));
            txtGoodAnswer.setText("Bonne réponse : " + (goodAnswerText.isEmpty() ? "Inconnue" : goodAnswerText));

            // ✅ Explication depuis la BDD (questions.explanation)
            String explanation = q != null ? q.optString("explanation", "") : "";
            if (explanation == null || explanation.trim().isEmpty() || "null".equalsIgnoreCase(explanation.trim())) {
                txtExplanation.setText(""); // ou "Explication : (à venir)"
            } else {
                txtExplanation.setText("Explication : " + explanation);
            }

            if (res.optBoolean("isCorrect")) {
                txtYourAnswer.setTextColor(getColor(R.color.eco_green));
            } else {
                txtYourAnswer.setTextColor(getColor(R.color.eco_red));
            }

            // ✅ Activer/désactiver boutons
            btnPrev.setEnabled(index > 0);
            btnNext.setEnabled(index < resultsArr.length() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject findQuestionById(int questionId) {
        try {
            for (int i = 0; i < questionsArr.length(); i++) {
                JSONObject q = questionsArr.getJSONObject(i);
                if (q.optInt("id", -1) == questionId) return q;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String findAnswerText(JSONObject question, int answerId) {
        if (question == null || answerId == -1) return "";
        try {
            JSONArray answers = question.getJSONArray("answers");
            for (int i = 0; i < answers.length(); i++) {
                JSONObject a = answers.getJSONObject(i);
                if (a.optInt("id", -1) == answerId) return a.optString("text", "");
            }
        } catch (Exception ignored) {}
        return "";
    }
}
