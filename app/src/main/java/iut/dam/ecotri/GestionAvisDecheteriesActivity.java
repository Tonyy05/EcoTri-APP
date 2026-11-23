package iut.dam.ecotri;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GestionAvisDecheteriesActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:3000";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private DecheterieSelectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decheteries_admin); // ✅ layout admin

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // retour dashboard mairie

        RecyclerView rv = findViewById(R.id.rvDecheteries);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DecheterieSelectAdapter(decheterie -> {
            Intent i = new Intent(GestionAvisDecheteriesActivity.this, GestionAvisActivity.class);
            i.putExtra("DECHE_ID", String.valueOf(decheterie.id));
            i.putExtra("DECHE_NOM", decheterie.nom);
            startActivity(i);
        });

        rv.setAdapter(adapter);

        loadDecheteriesFromApi();
    }

    private void loadDecheteriesFromApi() {
        String url = BASE_URL + "/api/decheteries";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(GestionAvisDecheteriesActivity.this,
                                "GET decheteries FAIL: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(GestionAvisDecheteriesActivity.this,
                                    "GET " + response.code() + " : " + body,
                                    Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                Type listType = new TypeToken<List<Decheterie>>() {}.getType();
                List<Decheterie> list = gson.fromJson(body, listType);

                runOnUiThread(() -> adapter.setData(list));
            }
        });
    }
}
