package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnProfil, btnDecheterie, btnJeu, btnCalendrier;
    private Button btnQuiz, btnScores, btnHistory, btnLeaderboard, btnPlayGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        SessionManager session = new SessionManager(this);
        if (session.getToken() == null || session.getToken().isEmpty()) {
            // Pas connecté → on renvoie vers MenuActivity
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
            return;
        }

        btnProfil     = findViewById(R.id.btnProfil);
        btnDecheterie = findViewById(R.id.btnDecheterie);
        btnJeu        = findViewById(R.id.btnJeu);
        btnCalendrier = findViewById(R.id.btnCalendrier);
        btnQuiz = findViewById(R.id.btnQuiz);

        btnHistory = findViewById(R.id.btnHistory);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);

        btnPlayGame = findViewById(R.id.btnPlayGame);

        MaterialButton btnAssistant = findViewById(R.id.btnAssistant);
        btnAssistant.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ChatBotActivity.class)));

        btnPlayGame.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, JeuExplicationActivity.class));
        });

        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ScoreHistoryActivity.class));
        });

        btnLeaderboard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LeaderboardActivity.class));
        });

        btnQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QuizSelectActivity.class);
            startActivity(intent);
        });


        // Profil
        btnProfil.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        // On branchera ces écrans plus tard
        btnDecheterie.setOnClickListener(v -> {
            startActivity(new Intent(this, DecheteriesActivity.class));
        });



        btnCalendrier = findViewById(R.id.btnCalendrier);
        btnCalendrier.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CalendarActivity.class));
        });

    }

}
