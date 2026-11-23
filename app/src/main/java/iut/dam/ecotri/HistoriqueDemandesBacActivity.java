package iut.dam.ecotri;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import iut.dam.ecotri.data.ApiClient;
import iut.dam.ecotri.data.DemandeBacApi;
import iut.dam.ecotri.data.DemandeBacResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoriqueDemandesBacActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SearchView searchView;

    private SessionManager session;
    private DemandeBacApi demandeBacApi;

    private HistoriqueDemandesAdapter adapter;

    private final List<DemandeBacResponse> allDemandes = new ArrayList<>();
    private final List<DemandeBacResponse> filteredDemandes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique_demandes_bac);

        session = new SessionManager(this);
        demandeBacApi = ApiClient.INSTANCE.getDemandeBacApi();

        MaterialToolbar toolbar = findViewById(R.id.toolbarHistorique);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        searchView = findViewById(R.id.searchView);
        recycler = findViewById(R.id.recyclerHistorique);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoriqueDemandesAdapter(filteredDemandes);
        recycler.setAdapter(adapter);

        setupSearch();

        loadAllDemandes();
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                applyFilter(query);
                return true;
            }

            @Override public boolean onQueryTextChange(String newText) {
                applyFilter(newText);
                return true;
            }
        });
    }

    private void applyFilter(String query) {
        String q = (query == null) ? "" : query.trim().toLowerCase(Locale.ROOT);

        filteredDemandes.clear();

        if (q.isEmpty()) {
            filteredDemandes.addAll(allDemandes);
        } else {
            for (DemandeBacResponse d : allDemandes) {
                // ✅ Recherche multi-champs (fonctionne même sans "nom")
                String haystack =
                        (safe(d.getTypeBac()) + " " +
                                safe(d.getRaison()) + " " +
                                safe(d.getAdresse()) + " " +
                                safe(d.getCommentaire()) + " " +
                                safe(d.getStatut()))
                                .toLowerCase(Locale.ROOT);

                if (haystack.contains(q)) {
                    filteredDemandes.add(d);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void updateEmptyState() {
        if (filteredDemandes.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Aucun résultat");
            recycler.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recycler.setVisibility(View.VISIBLE);
        }
    }

    private void loadAllDemandes() {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token manquant (non connecté)", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recycler.setVisibility(View.GONE);

        Call<List<DemandeBacResponse>> call = demandeBacApi.getDemandes("Bearer " + token);
        call.enqueue(new Callback<List<DemandeBacResponse>>() {
            @Override
            public void onResponse(Call<List<DemandeBacResponse>> call, Response<List<DemandeBacResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allDemandes.clear();
                    allDemandes.addAll(response.body());

                    filteredDemandes.clear();
                    filteredDemandes.addAll(allDemandes);

                    adapter.notifyDataSetChanged();
                    updateEmptyState();

                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Erreur " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<DemandeBacResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Erreur réseau : " + t.getMessage());
            }
        });
    }
}
