package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone, tvAdresse, tvDob;
    private MaterialButton btnChangePassword, btnLogout;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tb.setNavigationOnClickListener(v -> finish());

        tvName    = findViewById(R.id.tvName);
        tvEmail   = findViewById(R.id.tvEmail);
        tvPhone   = findViewById(R.id.tvPhone);
        tvAdresse = findViewById(R.id.tvAdresse);
        tvDob     = findViewById(R.id.tvDob);

        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout         = findViewById(R.id.btnLogout);

        // Remplir les infos depuis la session
        String fullName = session.getFirstName() + " " + session.getLastName();
        tvName.setText(fullName);

        tvEmail.setText("Email : " + session.getEmail());
        tvPhone.setText("Téléphone : " + session.getPhone());
        tvAdresse.setText("Adresse : " + session.getAdresse());
        tvDob.setText("Date de naissance : " + session.getDob());

        btnChangePassword.setOnClickListener(v -> {
            // Pour le moment on réutilise ForgotPasswordActivity
            Intent i = new Intent(this, ChangePasswordActivity.class);
            startActivity(i);
        });

        btnLogout.setOnClickListener(v -> {
            session.clear();
            Toast.makeText(this, "Déconnexion", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });

        MaterialButton btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            Intent i = new Intent(this, EditProfileActivity.class);
            startActivity(i);
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
