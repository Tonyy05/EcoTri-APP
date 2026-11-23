package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

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

public class ChangePassActivity extends AppCompatActivity {

    private TextInputLayout tilOldPassword, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etOldPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnValidate;

    private boolean forced;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepass);

        session = new SessionManager(this);

        // ✅ Mode forcé (après mot de passe oublié)
        forced = getIntent().getBooleanExtra("FORCED_CHANGE", false);

        // ✅ Si mode forcé et pas connecté => retour login
        if (forced && !session.isLoggedIn()) {
            goLogin();
            return;
        }

        // ===== Views (IDs de ton XML) =====
        tilOldPassword = findViewById(R.id.tilOldPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnValidate = findViewById(R.id.btnValidate);

        // ✅ Bloquer le BACK Android si forced
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (forced) {
                    Toast.makeText(ChangePassActivity.this,
                            "Tu dois changer ton mot de passe pour continuer",
                            Toast.LENGTH_SHORT).show();
                } else {
                    setEnabled(false);
                    ChangePassActivity.super.onBackPressed();
                }
            }
        });

        btnValidate.setOnClickListener(v -> changePassword());
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
                    "Erreur : email de session introuvable, reconnecte-toi.",
                    Toast.LENGTH_SHORT).show();
            if (forced) {
                session.logout();
                goLogin();
            }
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

                    Toast.makeText(ChangePassActivity.this,
                            response.body().getMessage(),
                            Toast.LENGTH_SHORT).show();

                    // ✅ Débloque l'utilisateur côté app (ton API met aussi la colonne à false)
                    session.setMustChangePassword(false);

                    if (forced) {
                        redirectAfterForcedChange();
                    } else {
                        finish();
                    }

                } else {
                    // Ici tu peux adapter selon tes codes backend (ex: 400 ancien mdp faux)
                    if (response.code() == 400 || response.code() == 401) {
                        tilOldPassword.setError("Ancien mot de passe incorrect");
                    } else {
                        Toast.makeText(ChangePassActivity.this,
                                "Erreur : " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiMessage> call, Throwable t) {
                btnValidate.setEnabled(true);
                Toast.makeText(ChangePassActivity.this,
                        "Erreur réseau : " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectAfterForcedChange() {
        String role = session.getRole();
        Intent i;

        if ("MAIRIE".equalsIgnoreCase(role)) {
            i = new Intent(this, MairieDashboardActivity.class);
        } else {
            i = new Intent(this, MainActivity.class);
        }

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void goLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
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
