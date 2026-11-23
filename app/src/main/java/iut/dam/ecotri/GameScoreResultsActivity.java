package iut.dam.ecotri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GameScoreResultsActivity extends AppCompatActivity {

    private TextView txtScore;
    private Button btnRetry, btnHome;
    private RecyclerView rvCorrections;

    private JSONArray resultsArr; // [{wasteId, chosenBinId, correctBinId, isCorrect, explanation}]
    private JSONArray wastesArr;  // [{id,name,imageUrl}]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_results); // ✅ on réutilise le layout du quiz

        txtScore = findViewById(R.id.txtScore);
        btnRetry = findViewById(R.id.btnRetry);
        btnHome = findViewById(R.id.btnHome);
        rvCorrections = findViewById(R.id.rvCorrections);

        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 20);
        txtScore.setText("Votre score : " + score + " / " + total);

        // ✅ grille (mets 5 pour que ça prenne l’écran et fasse des lignes)
        rvCorrections.setLayoutManager(new GridLayoutManager(this, 5));

        String resultsStr = getIntent().getStringExtra("gameResults");
        String wastesStr  = getIntent().getStringExtra("wastes");

        try {
            resultsArr = new JSONArray(resultsStr == null ? "[]" : resultsStr);
            wastesArr  = new JSONArray(wastesStr == null ? "[]" : wastesStr);
        } catch (Exception e) {
            resultsArr = new JSONArray();
            wastesArr = new JSONArray();
        }

        ArrayList<QuizCorrectionAdapter.Item> items = new ArrayList<>();
        for (int i = 0; i < resultsArr.length(); i++) {
            JSONObject o = resultsArr.optJSONObject(i);
            boolean isCorrect = o != null && o.optBoolean("isCorrect", false);
            items.add(new QuizCorrectionAdapter.Item(i + 1, isCorrect));
        }

        QuizCorrectionAdapter adapter = new QuizCorrectionAdapter(items, position -> {
            Intent i = new Intent(GameScoreResultsActivity.this, GameCorrectionDetailActivity.class);
            i.putExtra("index", position);
            i.putExtra("gameResults", resultsArr.toString());
            i.putExtra("wastes", wastesArr.toString());
            startActivity(i);
        });

        rvCorrections.setAdapter(adapter);

        btnRetry.setOnClickListener(v -> {
            startActivity(new Intent(GameScoreResultsActivity.this, GameActivity.class));
            finish();
        });

        btnHome.setOnClickListener(v -> {
            Intent i = new Intent(GameScoreResultsActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}
