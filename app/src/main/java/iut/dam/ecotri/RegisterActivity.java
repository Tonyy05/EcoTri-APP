package iut.dam.ecotri;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iut.dam.ecotri.data.ApiClient;
import iut.dam.ecotri.data.AuthApi;
import iut.dam.ecotri.data.RegisterRequest;
import iut.dam.ecotri.data.UserDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etEmail, etDob,
            etPassword, etPhone;

    // Adresse avec autocomplétion
    private MaterialAutoCompleteTextView etAdresse;
    private TextInputLayout tilAdresse;

    private Button btnRegister, btnGoLogin;

    // Autocomplete
    private ArrayAdapter<String> addressAdapter;
    private final Handler addressHandler = new Handler();
    private Runnable addressRunnable;

    private final Map<String, BanFeature> featureByLabel = new HashMap<>();
    private BanFeature selectedFeature = null;

    // 🔑 Fix bug sélection
    private boolean isSelectingAddress = false;

    private BanApi banApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Toolbar
        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tb.setNavigationOnClickListener(v -> finish());

        // Vues
        etFirstName = findViewById(R.id.etFirstName);
        etLastName  = findViewById(R.id.etLastName);
        etEmail     = findViewById(R.id.etEmail);
        etDob       = findViewById(R.id.etDob);
        etPassword  = findViewById(R.id.etPassword);
        etPhone     = findViewById(R.id.etPhone);

        etAdresse   = findViewById(R.id.etAdresse);
        tilAdresse  = findViewById(R.id.tilAdresse);

        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin  = findViewById(R.id.btnGoLogin);

        // Date picker
        etDob.setOnClickListener(v -> showDobPicker(etDob));

        // Init BAN + autocomplete
        initBanApi();
        initAdresseAutocomplete();

        btnRegister.setOnClickListener(v -> doRegister());
        btnGoLogin.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, LoginActivity.class)));
    }

    private void initBanApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api-adresse.data.gouv.fr/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        banApi = retrofit.create(BanApi.class);
    }

    private void initAdresseAutocomplete() {
        addressAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        etAdresse.setAdapter(addressAdapter);

        etAdresse.setOnItemClickListener((parent, view, position, id) -> {
            String label = (String) parent.getItemAtPosition(position);
            selectedFeature = featureByLabel.get(label);

            isSelectingAddress = true;
            etAdresse.setText(label);
            etAdresse.setSelection(label.length());
            isSelectingAddress = false;

            if (tilAdresse != null) tilAdresse.setError(null);
        });

        etAdresse.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isSelectingAddress) return;

                selectedFeature = null;
                if (tilAdresse != null) tilAdresse.setError(null);

                String query = s.toString().trim();

                if (addressRunnable != null) {
                    addressHandler.removeCallbacks(addressRunnable);
                }

                if (query.length() < 3) {
                    featureByLabel.clear();
                    runOnUiThread(() -> {
                        addressAdapter.clear();
                        addressAdapter.notifyDataSetChanged();
                    });
                    return;
                }

                addressRunnable = () -> fetchAddressSuggestions(query);
                addressHandler.postDelayed(addressRunnable, 350);
            }
        });
    }

    private void fetchAddressSuggestions(String query) {
        banApi.search(query, 6).enqueue(new Callback<BanResponse>() {
            @Override
            public void onResponse(Call<BanResponse> call, Response<BanResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                List<String> labels = new ArrayList<>();
                featureByLabel.clear();

                if (response.body().features != null) {
                    for (BanFeature f : response.body().features) {
                        if (f == null || f.properties == null) continue;
                        if (f.properties.score < 0.45) continue;

                        String label = f.properties.label;
                        if (label == null || label.isEmpty()) continue;

                        labels.add(label);
                        featureByLabel.put(label, f);
                    }
                }

                runOnUiThread(() -> {
                    addressAdapter.clear();
                    addressAdapter.addAll(labels);
                    addressAdapter.notifyDataSetChanged();

                    if (!labels.isEmpty() && etAdresse.hasFocus()) {
                        etAdresse.showDropDown();
                    }
                });
            }

            @Override
            public void onFailure(Call<BanResponse> call, Throwable t) {}
        });
    }

    private void doRegister() {
        String firstName = getTextSafe(etFirstName);
        String lastName = getTextSafe(etLastName);
        String email = getTextSafe(etEmail);
        String dateNaissance = getTextSafe(etDob);
        String adresseInput = getTextSafe(etAdresse);
        String password = getTextSafe(etPassword);
        String phone = getTextSafe(etPhone);

        if (firstName.isEmpty() || lastName.isEmpty()
                || email.isEmpty() || password.isEmpty()
                || phone.isEmpty() || adresseInput.isEmpty()) {
            Toast.makeText(this,
                    "Prénom, nom, email, téléphone, adresse et mot de passe sont obligatoires",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches("^0[1-9][0-9]{8}$")) {
            Toast.makeText(this,
                    "Numéro de téléphone invalide (ex : 0612345678)",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFeature == null || selectedFeature.properties == null) {
            if (tilAdresse != null) tilAdresse.setError("Choisis une adresse dans la liste");
            else Toast.makeText(this, "Choisis une adresse dans la liste", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔒 Mode strict (numéro + rue)
        if (!"housenumber".equals(selectedFeature.properties.type)) {
            if (tilAdresse != null) tilAdresse.setError("Adresse complète avec numéro obligatoire");
            else Toast.makeText(this, "Adresse complète avec numéro obligatoire", Toast.LENGTH_SHORT).show();
            return;
        }

        String adresseNormalisee = selectedFeature.properties.label;

        AuthApi api = ApiClient.INSTANCE.getAuthApi();
        RegisterRequest body = new RegisterRequest(
                firstName,
                lastName,
                email,
                password,
                dateNaissance,
                adresseNormalisee,
                phone
        );

        api.register(body).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this,
                            "Inscription réussie 🎉", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String msg = "Erreur inscription: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            JSONObject obj = new JSONObject(body);
                            if (obj.has("message")) msg = obj.getString("message");
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "Erreur réseau: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getTextSafe(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String getTextSafe(MaterialAutoCompleteTextView et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showDobPicker(TextInputEditText target) {
        final Calendar now = Calendar.getInstance();
        int y = now.get(Calendar.YEAR);
        int m = now.get(Calendar.MONTH);
        int d = now.get(Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog dlg = new android.app.DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dd = (dayOfMonth < 10 ? "0" : "") + dayOfMonth;
                    int mon = month + 1;
                    String mm = (mon < 10 ? "0" : "") + mon;
                    target.setText(dd + "/" + mm + "/" + year);
                }, y, m, d);

        dlg.getDatePicker().setMaxDate(now.getTimeInMillis());
        Calendar min = Calendar.getInstance();
        min.set(1900, Calendar.JANUARY, 1);
        dlg.getDatePicker().setMinDate(min.getTimeInMillis());

        dlg.show();
    }

    // ===== BAN API =====
    interface BanApi {
        @GET("search/")
        Call<BanResponse> search(@Query("q") String q, @Query("limit") int limit);
    }

    static class BanResponse {
        public List<BanFeature> features;
    }

    static class BanFeature {
        public BanProperties properties;
        public BanGeometry geometry;
    }

    static class BanProperties {
        public String label;
        public double score;
        public String city;
        public String postcode;
        public String type;
        public String id;
    }

    static class BanGeometry {
        public double[] coordinates;
    }
}
