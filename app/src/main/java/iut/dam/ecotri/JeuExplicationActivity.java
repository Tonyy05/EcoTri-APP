package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class JeuExplicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu_explication);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnUnderstood = findViewById(R.id.btnUnderstood);

        btnBack.setOnClickListener(v -> finish());

        btnUnderstood.setOnClickListener(v -> {
            Intent i = new Intent(JeuExplicationActivity.this, GameActivity.class);
            startActivity(i);
            finish(); // optionnel : retire la page d'explication de la pile
        });
    }
}
