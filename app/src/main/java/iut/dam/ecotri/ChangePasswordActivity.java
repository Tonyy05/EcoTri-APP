package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import iut.dam.ecotri.data.ApiClient;
import iut.dam.ecotri.data.ApiMessage;
import iut.dam.ecotri.data.AuthApi;
import iut.dam.ecotri.data.ChangePasswordRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout tilOldPassword, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etOldPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnValidate, btnBackChange;

    private ImageButton navJeu, navCalendrier, navDecheterie, navProfil;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        session = new SessionManager(this);

        // ===== Toolbar =====
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener(v -> finish());

        // ===== Views =====
        tilOldPassword = findViewById(R.id.tilOldPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnValidate = findViewById(R.id.btnValidate);
        btnBackChange = findViewById(R.id.btnBackChange);

        btnValidate.setOnClickListener(v -> changePassword());
        btnBackChange.setOnClickListener(v -> finish());

        // ===== Bottom navigation =====
        navJeu = findViewById(R.id.navJeu);
        navCalendrier = findViewById(R.id.navCalendrier);
        navDecheterie = findViewById(R.id.navDecheterie);
        navProfil = findViewById(R.id.navProfil);

        navJeu.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        navCalendrier.setOnClickListener(v ->
                Toast.makeText(this, "Calendrier bientôt dispo", Toast.LENGTH_SHORT).show());

        navDecheterie.setOnClickListener(v ->
                Toast.makeText(this, "Déchèterie bientôt dispo", Toast.LENGTH_SHORT).show());

        navProfil.setOnClickListener(v -> {
            // Déjà sur Compte
        });
    }

    private void changePassword() {
        clearErrors();

        String oldPwd = getTextSafe(etOldPassword);
        String newPwd = getTextSafe(etNewPassword);
        String confirmPwd = getTextSafe(etConfirmPassword);

        boolean ok = true;

        if (oldPwd.isEmpty()) {
            tilOldPassword.setError("Champ obligatoire");
            ok = false;
        }

        if (newPwd.isEmpty()) {
            tilNewPassword.setError("Champ obligatoire");
            ok = false;
        } else if (newPwd.length() < 6) {
            tilNewPassword.setError("Minimum 6 caractères");
            ok = false;
        } else if (newPwd.equals(oldPwd)) {
            tilNewPassword.setError("Le nouveau mot de passe doit être différent");
            ok = false;
        }

        if (confirmPwd.isEmpty()) {
            tilConfirmPassword.setError("Champ obligatoire");
            ok = false;
        } else if (!confirmPwd.equals(newPwd)) {
            tilConfirmPassword.setError("Les mots de passe ne correspondent pas");
            ok = false;
        }

        if (!ok) return;

        String email = session.getEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(this,
                    "Erreur : email introuvable, reconnecte-toi.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnValidate.setEnabled(false);

        AuthApi api = ApiClient.INSTANCE.getAuthApi();
        ChangePasswordRequest body = new ChangePasswordRequest(email, oldPwd, newPwd);

        api.changePassword(body).enqueue(new Callback<ApiMessage>() {
            @Override
            public void onResponse(Call<ApiMessage> call, Response<ApiMessage> response) {
                btnValidate.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ChangePasswordActivity.this,
                            response.body().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // Optionnel: si ton backend renvoie 400/401 quand l'ancien mdp est faux
                    if (response.code() == 400 || response.code() == 401) {
                        tilOldPassword.setError("Ancien mot de passe incorrect");
                    } else {
                        Toast.makeText(ChangePasswordActivity.this,
                                "Erreur : " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiMessage> call, Throwable t) {
                btnValidate.setEnabled(true);
                Toast.makeText(ChangePasswordActivity.this,
                        "Erreur réseau : " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearErrors() {
        tilOldPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private String getTextSafe(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
