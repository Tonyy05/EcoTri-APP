package iut.dam.ecotri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class GameResultsActivity extends AppCompatActivity {

    private Button btnToQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_results);

        TextView txtTitle = findViewById(R.id.txtTitle);
        TextView txtScore = findViewById(R.id.txtScoreResult);
        TextView txtSubtitle = findViewById(R.id.txtSubtitle);

        btnToQuiz = findViewById(R.id.btnToQuiz);
        Button btnReplay = findViewById(R.id.btnReplay);

        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 20);

        txtTitle.setText("Bravo !");
        txtScore.setText("Votre score : " + score + " / " + total);

        if (score == total) txtSubtitle.setText("Parfait ! Aucun déchet mal trié ✅");
        else if (score >= (total * 0.7)) txtSubtitle.setText("Très bien ! Continue comme ça 💪");
        else txtSubtitle.setText("Bien joué ! Tu peux encore t’améliorer 😄");

        showQuizChoiceDialog();

        btnToQuiz.setOnClickListener(v -> {
            Intent i = new Intent(GameResultsActivity.this, QuizActivity.class);
            startActivity(i);
            finish();
        });

        btnReplay.setOnClickListener(v -> {
            Intent i = new Intent(GameResultsActivity.this, GameActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void showQuizChoiceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Quiz bonus ?")
                .setMessage("Tu veux faire le quiz maintenant pour tester tes connaissances ?")
                .setCancelable(false) // empêche de fermer en cliquant hors du popup
                .setPositiveButton("Oui", (dialog, which) -> {
                    Intent i = new Intent(GameResultsActivity.this, QuizActivity.class);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("Non", (dialog, which) -> {
                    dialog.dismiss(); // on reste sur la page résultats
                    btnToQuiz.setEnabled(false);
                    btnToQuiz.setAlpha(0.5f);
                })
                .show();
    }
}
