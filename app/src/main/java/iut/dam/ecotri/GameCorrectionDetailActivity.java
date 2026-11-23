package iut.dam.ecotri;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class GameCorrectionDetailActivity extends AppCompatActivity {

    private TextView txtTitle, txtYourAnswer, txtGoodAnswer, txtExplanation;
    private Button btnPrev, btnNext, btnBack;

    private JSONArray resultsArr;
    private JSONArray wastesArr;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_correction_detail);

        txtTitle = findViewById(R.id.txtTitle);
        txtYourAnswer = findViewById(R.id.txtYourAnswer);
        txtGoodAnswer = findViewById(R.id.txtGoodAnswer);
        txtExplanation = findViewById(R.id.txtExplanation);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        index = getIntent().getIntExtra("index", 0);

        try {
            resultsArr = new JSONArray(getIntent().getStringExtra("gameResults"));
            wastesArr  = new JSONArray(getIntent().getStringExtra("wastes"));
        } catch (Exception e) {
            resultsArr = new JSONArray();
            wastesArr = new JSONArray();
        }

        render();

        btnPrev.setOnClickListener(v -> {
            if (index > 0) { index--; render(); }
        });

        btnNext.setOnClickListener(v -> {
            if (index < resultsArr.length() - 1) { index++; render(); }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void render() {
        try {
            JSONObject res = resultsArr.getJSONObject(index);

            int wasteId = res.optInt("wasteId", -1);
            int chosenBinId = res.optInt("chosenBinId", -1);
            int correctBinId = res.optInt("correctBinId", -1);
            String explanation = res.optString("explanation", "");

            String wasteName = findWasteName(wasteId);

            txtTitle.setText((index + 1) + ". " + (wasteName.isEmpty() ? "Déchet" : wasteName));
            txtYourAnswer.setText("Votre poubelle : " + binLabel(chosenBinId));
            txtGoodAnswer.setText("Bonne poubelle : " + binLabel(correctBinId));
            txtExplanation.setText(explanation.isEmpty() ? "Explication : (à venir)" : "Explication : " + explanation);

            btnPrev.setEnabled(index > 0);
            btnNext.setEnabled(index < resultsArr.length() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String findWasteName(int wasteId) {
        try {
            for (int i = 0; i < wastesArr.length(); i++) {
                JSONObject w = wastesArr.getJSONObject(i);
                if (w.optInt("id", -1) == wasteId) return w.optString("name", "");
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String binLabel(int binId) {
        switch (binId) {
            case 1: return "Poubelle jaune (recyclables)";
            case 2: return "Poubelle verte (ordures)";
            case 3: return "Poubelle marron (bio-déchets)";
            case 4: return "Poubelle bleue (verre)";
            default: return "Inconnue";
        }
    }
}
