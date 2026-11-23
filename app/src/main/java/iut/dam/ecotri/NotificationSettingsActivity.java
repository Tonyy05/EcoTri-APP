package iut.dam.ecotri;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationSettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchEmailNotif;
    private MaterialButton btnSaveNotif;
    private SessionManager session;

    // ⚠️ adapte l'URL selon ton cas (émulateur = 10.0.2.2)
    private static final String BASE_URL = "http://10.0.2.2:3000";
    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tb.setNavigationOnClickListener(v -> finish());

        session = new SessionManager(this);

        switchEmailNotif = findViewById(R.id.switchEmailNotif);
        btnSaveNotif = findViewById(R.id.btnSaveNotif);

        // 1️⃣ État initial du switch = ce qu'on a en SessionManager
        boolean current = session.getNotificationsEmail();
        switchEmailNotif.setChecked(current);

        // 2️⃣ Quand on clique sur "Enregistrer"
        btnSaveNotif.setOnClickListener(v -> {
            boolean enabled = switchEmailNotif.isChecked();
            updateNotificationPreference(enabled);
        });
    }

    private void updateNotificationPreference(boolean enabled) {
        String email = session.getEmail(); // ✅ ton SessionManager a getEmail()

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email utilisateur introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Préparation JSON pour l'API /api/auth/update-profile
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("notificationsEnabled", enabled);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(BASE_URL + "/api/auth/update-profile")
                    .post(body)
                    .build();

            // Appel réseau dans un thread séparé
            new Thread(() -> {
                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    runOnUiThread(() -> {
                        if (!response.isSuccessful()) {
                            Toast.makeText(
                                    NotificationSettingsActivity.this,
                                    "Erreur " + response.code() + " : " + responseBody,
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            // ✅ On met à jour la valeur locale dans SessionManager
                            session.setNotificationsEmail(enabled);

                            Toast.makeText(
                                    NotificationSettingsActivity.this,
                                    "Préférences mises à jour",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // On ferme l'écran et on revient aux paramètres
                            finish();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(
                                    NotificationSettingsActivity.this,
                                    "Erreur réseau : " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur JSON : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
