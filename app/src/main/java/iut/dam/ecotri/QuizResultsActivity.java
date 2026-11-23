package iut.dam.ecotri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.JustifyContent;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class QuizResultsActivity extends AppCompatActivity {

    private TextView txtScore;
    private Button btnRetry, btnHome;
    private RecyclerView rvCorrections;

    private JSONArray resultsArr;
    private JSONArray questionsArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_results);

        txtScore = findViewById(R.id.txtScore);
        btnRetry = findViewById(R.id.btnRetry);
        btnHome = findViewById(R.id.btnHome);
        rvCorrections = findViewById(R.id.rvCorrections);

        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 0);
        txtScore.setText("Votre score : " + score + " / " + total);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND); // ou SPACE_BETWEEN

        rvCorrections.setLayoutManager(layoutManager);


        ArrayList<QuizCorrectionAdapter.Item> items = new ArrayList<>();

        String resultsStr = getIntent().getStringExtra("results");
        String questionsStr = getIntent().getStringExtra("questions");

        try {
            if (resultsStr != null) resultsArr = new JSONArray(resultsStr);
            if (questionsStr != null) questionsArr = new JSONArray(questionsStr);

            if (resultsArr != null) {
                for (int i = 0; i < resultsArr.length(); i++) {
                    JSONObject o = resultsArr.getJSONObject(i);
                    boolean isCorrect = o.optBoolean("isCorrect", false);
                    items.add(new QuizCorrectionAdapter.Item(i + 1, isCorrect));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        QuizCorrectionAdapter corrAdapter = new QuizCorrectionAdapter(items, position -> {
            Intent i = new Intent(QuizResultsActivity.this, CorrectionDetailActivity.class);
            i.putExtra("index", position); // ✅ OK maintenant
            i.putExtra("results", resultsArr != null ? resultsArr.toString() : "[]");
            i.putExtra("questions", questionsArr != null ? questionsArr.toString() : "[]");
            startActivity(i);
        });

        rvCorrections.setAdapter(corrAdapter);

        btnRetry.setOnClickListener(v -> {
            Intent i = new Intent(QuizResultsActivity.this, QuizSelectActivity.class);
            startActivity(i);
            finish();
        });

        btnHome.setOnClickListener(v -> {
            Intent i = new Intent(QuizResultsActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}
