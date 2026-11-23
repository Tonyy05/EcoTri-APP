package iut.dam.ecotri;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import iut.dam.ecotri.data.ApiClient;
import iut.dam.ecotri.data.ApiMessage;
import iut.dam.ecotri.data.AuthApi;
import iut.dam.ecotri.data.ForgotPasswordRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private MaterialButton btnSend, btnGoLogin, btnGoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Toolbar avec flèche retour
        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setTitle("");
        tb.setNavigationOnClickListener(v -> finish());

        // Récupération des vues
        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSend);
        btnGoLogin   = findViewById(R.id.btnGoLogin);
        btnGoRegister= findViewById(R.id.btnGoRegister);

        // Clic sur "Envoyer une adresse e-mail de récupération"
        btnSend.setOnClickListener(v -> sendForgotPassword());

        // Clic "Connexion" qui ramène vers LoginActivity
        btnGoLogin.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, LoginActivity.class)));

        // Clic "Inscription" qui ramène vers RegisterActivity
        btnGoRegister.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, RegisterActivity.class)));
    }

    private void sendForgotPassword() {
        String email = getTextSafe(etEmail);

        if (email.isEmpty()) {
            Toast.makeText(this, "Merci de saisir ton e-mail", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthApi api = ApiClient.INSTANCE.getAuthApi();
        ForgotPasswordRequest body = new ForgotPasswordRequest(email);

        // Désactiver le bouton pour éviter les doubles clics
        btnSend.setEnabled(false);

        api.forgotPassword(body).enqueue(new Callback<ApiMessage>() {
            @Override
            public void onResponse(Call<ApiMessage> call, Response<ApiMessage> response) {
                btnSend.setEnabled(true);

                if (response.isSuccessful()) {
                    String msg = "Si un compte existe, un e-mail a été envoyé.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }

                    Toast.makeText(ForgotPasswordActivity.this, msg, Toast.LENGTH_LONG).show();
                    finish(); // retour à l'écran de login
                } else {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Erreur serveur, réessaie plus tard",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<ApiMessage> call, Throwable t) {
                btnSend.setEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this,
                        "Erreur réseau : " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private String getTextSafe(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
