package iut.dam.ecotri;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class GameActivity extends AppCompatActivity {

    private ImageView imgWaste;
    private TextView txtWasteName;
    private TextView txtRound;
    private JSONArray gameResults = new JSONArray(); // ✅ détail des manches


    // ID poubelles (comme dans ta DB / API)
    private static final int BIN_YELLOW = 1;
    private static final int BIN_GREEN  = 2;
    private static final int BIN_BROWN  = 3;
    private static final int BIN_BLUE   = 4;

    private ArrayList<Waste> batchWastes = new ArrayList<>();
    private int currentIndex = 0;
    // Déchet courant (ex: reçu via API)
    private int currentWasteId = 0;
    private int currentCorrectBinId = BIN_YELLOW; // à remplir via API "answer" ou via logique locale
    private String currentWasteName = "Canette";
    private int score = 0;
    private int roundsPlayed = 0;
    private static final int MAX_ROUNDS = 20;

    private TextView txtScore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        imgWaste = findViewById(R.id.imgWaste);
        txtWasteName = findViewById(R.id.txtWasteName);
        txtRound = findViewById(R.id.txtRound);

        ImageView binYellow = findViewById(R.id.binYellow);
        ImageView binGreen  = findViewById(R.id.binGreen);
        ImageView binBrown  = findViewById(R.id.binBrown);
        ImageView binBlue   = findViewById(R.id.binBlue);

        // 1) rendre le déchet draggable
        imgWaste.setOnLongClickListener(v -> {
            // on met waste_id dans le drag (ici en texte, simple)
            ClipData data = ClipData.newPlainText("waste_id", String.valueOf(currentWasteId));
            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadow, null, 0);
            return true;
        });

        // 2) rendre chaque poubelle droppable
        setBinDrop(binYellow, BIN_YELLOW);
        setBinDrop(binGreen,  BIN_GREEN);
        setBinDrop(binBrown,  BIN_BROWN);
        setBinDrop(binBlue,   BIN_BLUE);

        // 3) Charger un premier déchet
        loadBatchWastes();

        txtScore = findViewById(R.id.txtScore);
        updateScoreUI();
        findViewById(R.id.btnBack).setOnClickListener(v -> showExitDialog());


    }

    private void showExitDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Quitter le jeu ?")
                .setMessage("Êtes-vous sûr de vouloir quitter ?\nToute progression sera perdue.")
                .setPositiveButton("Oui", (dialog, which) -> finish())
                .setNegativeButton("Non", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateScoreUI() {
        txtScore.setText("Score : " + score);

        if (txtRound != null) {
            int shownRound = Math.min(currentIndex + 1, MAX_ROUNDS);
            txtRound.setText("Manche : " + shownRound + " / " + MAX_ROUNDS);
        }
    }

    private void setBinDrop(View binView, int binId) {
        binView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setAlpha(0.7f);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    v.setAlpha(1f);
                    return true;

                case DragEvent.ACTION_DROP:
                    v.setAlpha(1f);
                    onWasteDropped(binId);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    v.setAlpha(1f);
                    return true;
            }
            return true;
        });
    }

    private void onWasteDropped(int chosenBinId) {
        checkAnswerOnServer(currentWasteId, chosenBinId);
    }

    private void loadBatchWastes() {
        String token = getTokenFromSession();
        String authHeader = buildAuthHeader(token);

        Log.d("AUTH", "Token brut = [" + token + "]");
        Log.d("AUTH", "Header final = [" + authHeader + "]");

        if (authHeader == null || authHeader.trim().isEmpty()) {
            Toast.makeText(this, "Token manquant", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String urlStr = "http://10.0.2.2:3000/api/waste/random-batch?count=" + MAX_ROUNDS;
                URL url = new URL(urlStr);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", authHeader);

                int code = conn.getResponseCode();
                Log.d("GAME", "Batch HTTP code = " + code);

                // ---- Lire body d'erreur si >= 400
                String errorBody = null;
                if (code >= 400 && conn.getErrorStream() != null) {
                    BufferedReader brErr = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder sbErr = new StringBuilder();
                    String l;
                    while ((l = brErr.readLine()) != null) sbErr.append(l);
                    brErr.close();
                    errorBody = sbErr.toString();
                    Log.e("GAME", "Batch error body = " + errorBody);
                }

                if (code == 401) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Session expirée, reconnecte-toi", Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                // ✅ IMPORTANT: gérer 429 AVANT le code != 200
                if (code == 429) {
                    String msg = "Déjà joué aujourd’hui ⏳";
                    try {
                        if (errorBody != null) {
                            JSONObject o = new JSONObject(errorBody);
                            String next = o.optString("nextAvailable", null);
                            if (next != null) msg = buildRemainingTimeMessage(next);
                            else msg = o.optString("message", msg);
                        }
                    } catch (Exception ignored) {}

                    String finalMsg = msg;
                    runOnUiThread(() -> {
                        Toast.makeText(this, finalMsg, Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                if (code != 200) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Erreur batch (code " + code + ")", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                // ---- Lire réponse OK
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject obj = new JSONObject(sb.toString());
                JSONArray wastes = obj.getJSONArray("wastes");

                ArrayList<Waste> tmp = new ArrayList<>();
                for (int i = 0; i < wastes.length(); i++) {
                    JSONObject w = wastes.getJSONObject(i);
                    tmp.add(new Waste(
                            w.getInt("id"),
                            w.getString("name"),
                            w.optString("image_url", "")
                    ));
                }

                runOnUiThread(() -> {
                    batchWastes.clear();
                    batchWastes.addAll(tmp);
                    currentIndex = 0;
                    score = 0;
                    roundsPlayed = 0;
                    gameResults = new JSONArray();


                    showWasteAt(0);
                    updateScoreUI();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur réseau batch: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void showWasteAt(int index) {
        if (batchWastes == null || batchWastes.isEmpty()) return;
        if (index < 0 || index >= batchWastes.size()) return;

        Waste w = batchWastes.get(index);
        currentWasteId = w.id;
        currentWasteName = w.name;

        txtWasteName.setText(currentWasteName);
        String url = w.imageUrl;
        Log.d("WASTE_IMG", "ShowWaste id=" + w.id + " name=" + w.name + " url=" + url);

        if (url != null && !url.trim().isEmpty() && !"null".equalsIgnoreCase(url.trim())) {
            Glide.with(this)
                    .load(url.trim())
                    .placeholder(R.drawable.ic_waste_placeholder)
                    .error(R.drawable.ic_waste_placeholder)
                    .into(imgWaste);
        } else {
            imgWaste.setImageResource(R.drawable.ic_waste_placeholder);
        }
    }

    private void checkAnswerOnServer(int wasteId, int chosenBinId) {
        String token = getTokenFromSession();
        String authHeader = buildAuthHeader(token);

        Log.d("AUTH", "Header answer = [" + authHeader + "]");

        if (authHeader == null || authHeader.trim().isEmpty()) {
            Toast.makeText(this, "Token manquant", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String urlStr = "http://10.0.2.2:3000/api/waste/answer";
                URL url = new URL(urlStr);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", authHeader);
                conn.setDoOutput(true);

                String json = "{\"wasteId\":" + wasteId + ",\"binId\":" + chosenBinId + "}";

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("UTF-8"));
                }

                int code = conn.getResponseCode();
                Log.d("GAME", "Answer HTTP code = " + code);

                if (code != 200) {
                    BufferedReader brErr = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder sbErr = new StringBuilder();
                    String l;
                    while ((l = brErr.readLine()) != null) sbErr.append(l);
                    brErr.close();

                    Log.e("GAME", "Erreur answer body = " + sbErr);

                    runOnUiThread(() ->
                            Toast.makeText(this, "Erreur answer (code " + code + ")", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                String response = sb.toString();
                Log.d("GAME", "Response JSON = " + response);

                JSONObject obj = new JSONObject(response);
                boolean correct = obj.getBoolean("correct");
                String explanation = obj.optString("explanation", "");
                int correctBinId = obj.optInt("correctBinId", -1);

                JSONObject round = new JSONObject();
                round.put("wasteId", wasteId);
                round.put("chosenBinId", chosenBinId);
                round.put("correctBinId", correctBinId);
                round.put("isCorrect", correct);
                round.put("explanation", explanation);

                gameResults.put(round);


                runOnUiThread(() -> {
                    if (correct) {
                        score++;
                    }

                    roundsPlayed++;
                    updateScoreUI();

                    currentIndex++;

                    if (currentIndex >= MAX_ROUNDS) {
                        endGameAndSaveScoreAndShowResults();
                    } else {
                        showWasteAt(currentIndex);
                        updateScoreUI();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur réseau answer: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private String getTokenFromSession() {
        SessionManager session = new SessionManager(this);
        return session.getToken();
    }

    private void endGameAndSaveScoreAndShowResults() {
        String token = getTokenFromSession();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Utilisateur non connecté (token manquant)", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        new Thread(() -> {
            HttpURLConnection conn = null;
            int code = 0;

            try {
                String urlStr = "http://10.0.2.2:3000/api/scores/game";
                URL url = new URL(urlStr);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);

                String json = "{\"points\":" + score + "}";

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("UTF-8"));
                }

                code = conn.getResponseCode();

            } catch (Exception e) {
                int finalCode = code;
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur réseau: " + e.getMessage() + " (code " + finalCode + ")", Toast.LENGTH_LONG).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }

            int finalCode1 = code;
            runOnUiThread(() -> {
                if (finalCode1 == 201 || finalCode1 == 200) {
                    Toast.makeText(this, "Score enregistré ✅", Toast.LENGTH_SHORT).show();
                }

                // ✅ construire la liste des déchets pour retrouver les noms au détail
                JSONArray wastesArr = new JSONArray();
                try {
                    for (Waste w : batchWastes) {
                        JSONObject o = new JSONObject();
                        o.put("id", w.id);
                        o.put("name", w.name);
                        o.put("imageUrl", w.imageUrl);
                        wastesArr.put(o);
                    }
                } catch (Exception ignored) {}

                Intent i = new Intent(GameActivity.this, GameScoreResultsActivity.class);
                i.putExtra("score", score);
                i.putExtra("total", MAX_ROUNDS);
                i.putExtra("gameResults", gameResults.toString());
                i.putExtra("wastes", wastesArr.toString());
                startActivity(i);
                finish();
            });

        }).start();
    }

    private String buildRemainingTimeMessage(String isoDate) {
        try {
            SimpleDateFormat iso =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            iso.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date next = iso.parse(isoDate);
            long diffMs = next.getTime() - System.currentTimeMillis();

            if (diffMs <= 0) return "Tu peux rejouer maintenant 🎉";

            long minutes = diffMs / 60000;
            long hours = minutes / 60;
            minutes = minutes % 60;

            if (hours > 0) {
                return "⏳ Reviens dans " + hours + "h " + minutes + "min";
            } else {
                return "⏳ Reviens dans " + minutes + "min";
            }

        } catch (Exception e) {
            return "⏳ Reviens plus tard";
        }
    }

    private String buildAuthHeader(String token) {
        if (token == null) return null;
        token = token.trim();
        if (token.toLowerCase().startsWith("bearer ")) {
            return token; // déjà ok
        }
        return "Bearer " + token;
    }
}
