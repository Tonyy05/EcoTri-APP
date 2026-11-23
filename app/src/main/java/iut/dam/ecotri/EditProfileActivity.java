package iut.dam.ecotri;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iut.dam.ecotri.data.ApiClient;
import iut.dam.ecotri.data.AuthApi;
import iut.dam.ecotri.data.UpdateProfileRequest;
import iut.dam.ecotri.data.UserDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etEmail, etPhone, etDob;
    private MaterialAutoCompleteTextView etAdresse;
    private TextInputLayout tilAdresseEdit;

    private MaterialButton btnSave;
    private SessionManager session;

    // Autocomplete BAN
    private ArrayAdapter<String> addressAdapter;
    private final Handler addressHandler = new Handler();
    private Runnable addressRunnable;

    private final Map<String, BanFeature> featureByLabel = new HashMap<>();
    private BanFeature selectedFeature = null;

    // Fix bug sélection
    private boolean isSelectingAddress = false;

    private BanApi banApi;

    // Pour savoir si l'utilisateur a réellement modifié l'adresse
    private String initialAdresse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        session = new SessionManager(this);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tb.setNavigationOnClickListener(v -> finish());

        etFirstName = findViewById(R.id.etFirstNameEdit);
        etLastName  = findViewById(R.id.etLastNameEdit);
        etEmail     = findViewById(R.id.etEmailEdit);
        etPhone     = findViewById(R.id.etPhoneEdit);
        etAdresse   = findViewById(R.id.etAdresseEdit);      // ✅ AutoComplete
        tilAdresseEdit = findViewById(R.id.tilAdresseEdit);  // ✅ pour erreurs
        etDob       = findViewById(R.id.etDobEdit);

        btnSave     = findViewById(R.id.btnSaveProfile);

        MaterialButton btnLogoutEdit = findViewById(R.id.btnLogoutEdit);
        btnLogoutEdit.setOnClickListener(v -> {
            session.clear();
            Toast.makeText(this, "Déconnexion", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Pré-remplissage avec la session
        etFirstName.setText(session.getFirstName());
        etLastName.setText(session.getLastName());
        etEmail.setText(session.getEmail());
        etPhone.setText(session.getPhone());
        etAdresse.setText(session.getAdresse());
        etDob.setText(session.getDob());

        initialAdresse = session.getAdresse() != null ? session.getAdresse().trim() : "";

        // Date picker
        etDob.setOnClickListener(v -> showDatePicker());

        // ✅ Init BAN + autocomplete
        initBanApi();
        initAdresseAutocomplete();

        btnSave.setOnClickListener(v -> saveProfile());
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

            if (tilAdresseEdit != null) tilAdresseEdit.setError(null);
        });

        etAdresse.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isSelectingAddress) return;

                // si l'utilisateur retape, il n'a plus une adresse "sélectionnée"
                selectedFeature = null;
                if (tilAdresseEdit != null) tilAdresseEdit.setError(null);

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
            public void onFailure(Call<BanResponse> call, Throwable t) { }
        });
    }

    private void showDatePicker() {
        final Calendar now = Calendar.getInstance();
        int y = now.get(Calendar.YEAR);
        int m = now.get(Calendar.MONTH);
        int d = now.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dlg = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dd = (dayOfMonth < 10 ? "0" : "") + dayOfMonth;
                    int mon = month + 1;
                    String mm = (mon < 10 ? "0" : "") + mon;
                    etDob.setText(dd + "/" + mm + "/" + year);
                }, y, m, d);
        dlg.show();
    }

    private void saveProfile() {
        String oldEmail = session.getEmail();

        String firstName = textOf(etFirstName);
        String lastName  = textOf(etLastName);
        String newEmail  = textOf(etEmail);
        String phone     = textOf(etPhone);
        String adresse   = textOf(etAdresse);
        String dob       = textOf(etDob);

        // ✅ Si l'utilisateur a modifié l'adresse, il doit sélectionner dans la liste
        boolean adresseChanged = !adresse.trim().equals(initialAdresse);

        if (adresseChanged) {
            if (selectedFeature == null || selectedFeature.properties == null) {
                if (tilAdresseEdit != null) tilAdresseEdit.setError("Choisis une adresse dans la liste");
                else Toast.makeText(this, "Choisis une adresse dans la liste", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔒 Mode strict : adresse complète avec numéro
            if (!"housenumber".equals(selectedFeature.properties.type)) {
                if (tilAdresseEdit != null) tilAdresseEdit.setError("Adresse complète avec numéro obligatoire");
                else Toast.makeText(this, "Adresse complète avec numéro obligatoire", Toast.LENGTH_SHORT).show();
                return;
            }

            // adresse normalisée si modifiée
            adresse = selectedFeature.properties.label;
        } else {
            // il n'a pas modifié l'adresse, on accepte celle de la session
            if (tilAdresseEdit != null) tilAdresseEdit.setError(null);
        }

        AuthApi api = ApiClient.INSTANCE.getAuthApi();

        UpdateProfileRequest body = new UpdateProfileRequest(
                oldEmail,
                firstName,
                lastName,
                newEmail,
                adresse,
                phone,
                dob
        );

        api.updateProfile(body).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDto u = response.body();
                    session.updateProfile(u);

                    Toast.makeText(EditProfileActivity.this,
                            "Profil mis à jour",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this,
                            "Erreur update: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this,
                        "Erreur réseau: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String textOf(MaterialAutoCompleteTextView et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
        public String type; // housenumber, street...
        public String id;
    }

    static class BanGeometry {
        public double[] coordinates; // [lon, lat]
    }
}
