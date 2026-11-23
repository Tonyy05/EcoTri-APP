package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 2000L; // 2 secondes

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Animation logo + texte
        Animation pop = AnimationUtils.loadAnimation(this, R.anim.splash_pop);
        findViewById(R.id.logo).startAnimation(pop);
        findViewById(R.id.appName).startAnimation(pop);

        // Barre de statut (texte sombre sur fond clair) pour Android 11+
        Window w = getWindow();
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = w.getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        }

        new Handler(getMainLooper()).postDelayed(() -> {
            SessionManager session = new SessionManager(SplashActivity.this);

            Intent intent;

            String token = session.getToken();

            if (token != null && !token.isEmpty()) {

                // ✅ AJOUT IMPORTANT : si mustChangePassword = true => on déconnecte
                if (session.getMustChangePassword()) {
                    session.logout();
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                } else {
                    // ✅ Déjà connecté -> on regarde le rôle
                    String role = session.getRole();

                    if ("MAIRIE".equalsIgnoreCase(role)) {
                        intent = new Intent(SplashActivity.this, MairieDashboardActivity.class);
                    } else {
                        intent = new Intent(SplashActivity.this, MainActivity.class);
                    }
                }

            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }, SPLASH_DURATION);
    }
}
