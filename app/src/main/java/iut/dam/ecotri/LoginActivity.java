package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import iut.dam.ecotri.data.ApiClient;
import iut.dam.ecotri.data.AuthApi;
import iut.dam.ecotri.data.LoginRequest;
import iut.dam.ecotri.data.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnSubmit, btnForgot, btnGoRegister;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setTitle("");
        tb.setNavigationOnClickListener(v -> finish());

        sessionManager = new SessionManager(this);

        etEmail       = findViewById(R.id.etEmail);
        etPassword    = findViewById(R.id.etPassword);
        btnSubmit     = findViewById(R.id.btnSubmit);
        btnForgot     = findViewById(R.id.btnForgot);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        btnForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnSubmit.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String email    = getTextSafe(etEmail);
        String password = getTextSafe(etPassword);

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Remplis Email et Mot de passe",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AuthApi api = ApiClient.INSTANCE.getAuthApi();
        LoginRequest body = new LoginRequest(email, password);

        api.login(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call,
                                   Response<LoginResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();

                    // 🔹 On enregistre TOUT dans la session
                    sessionManager.saveLogin(res);

                    if (res.getMustChangePassword()) {
                        Intent i = new Intent(LoginActivity.this, ChangePassActivity.class);
                        i.putExtra("FORCED_CHANGE", true); // ✅ mode forcé
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        return;
                    }

                    String firstName = res.getUser().getFirstName();
                    String lastName  = res.getUser().getLastName();
                    String role      = res.getUser().getRole(); // <-- important

                    Toast.makeText(LoginActivity.this,
                            "Connecté en tant que " + firstName + " " + lastName,
                            Toast.LENGTH_SHORT).show();

                    // 🔀 Redirection selon le rôle
                    Intent i;
                    if ("MAIRIE".equalsIgnoreCase(role)) {
                        i = new Intent(LoginActivity.this, MairieDashboardActivity.class);
                    } else {
                        i = new Intent(LoginActivity.this, MainActivity.class);
                    }

                    // On nettoie la backstack
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Email ou mot de passe invalide",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "Erreur réseau: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getTextSafe(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
