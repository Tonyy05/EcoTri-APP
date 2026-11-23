package iut.dam.ecotri;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import iut.dam.ecotri.data.ApiClient;
import iut.dam.ecotri.data.DemandeBacApi;
import iut.dam.ecotri.data.DemandeBacResponse;
import iut.dam.ecotri.data.UpdateStatutRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;
import com.google.android.material.button.MaterialButton;

public class GestionDemandesBacActivity extends AppCompatActivity {

    private RecyclerView recyclerDemandes;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private SessionManager session;
    private DemandeBacApi demandeBacApi;
    private DemandesAdapter adapter;
    private final List<DemandeBacResponse> demandes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_demandes_bac);

        session = new SessionManager(this);
        demandeBacApi = ApiClient.INSTANCE.getDemandeBacApi();

        MaterialToolbar toolbar = findViewById(R.id.toolbarGestionDemandes);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerDemandes = findViewById(R.id.recyclerDemandes);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        MaterialButton btnHistorique = findViewById(R.id.btnHistoriqueDemandes);
        btnHistorique.setOnClickListener(v -> {
            Intent i = new Intent(GestionDemandesBacActivity.this, HistoriqueDemandesBacActivity.class);
            startActivity(i);
        });

        recyclerDemandes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DemandesAdapter(demandes, new DemandesAdapter.OnActionListener() {
            @Override
            public void onAccepter(DemandeBacResponse demande) {
                updateStatut(demande, "ACCEPTEE");
            }

            @Override
            public void onRefuser(DemandeBacResponse demande) {
                updateStatut(demande, "REFUSEE");
            }
        });
        recyclerDemandes.setAdapter(adapter);

        loadDemandes();
    }

    private void loadDemandes() {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token manquant (non connecté)", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerDemandes.setVisibility(View.GONE);

        Call<List<DemandeBacResponse>> call = demandeBacApi.getDemandes("Bearer " + token);
        call.enqueue(new Callback<List<DemandeBacResponse>>() {
            @Override
            public void onResponse(Call<List<DemandeBacResponse>> call, Response<List<DemandeBacResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    demandes.clear();

                    // On ne garde que les demandes EN_ATTENTE
                    for (DemandeBacResponse d : response.body()) {
                        if ("EN ATTENTE".equals(d.getStatut())) {
                            demandes.add(d);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (demandes.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Aucune demande en attente");
                        recyclerDemandes.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerDemandes.setVisibility(View.VISIBLE);
                    }
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

    private void updateStatut(DemandeBacResponse demande, String nouveauStatut) {
        String token = session.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token manquant (non connecté)", Toast.LENGTH_LONG).show();
            return;
        }

        UpdateStatutRequest body = new UpdateStatutRequest(nouveauStatut);
        Call<DemandeBacResponse> call = demandeBacApi.updateDemandeStatut("Bearer " + token, demande.getId(), body);
        call.enqueue(new Callback<DemandeBacResponse>() {
            @Override
            public void onResponse(Call<DemandeBacResponse> call, Response<DemandeBacResponse> response) {
                if (response.isSuccessful()) {
                    // On enlève la demande de la liste locale
                    int index = demandes.indexOf(demande);
                    if (index != -1) {
                        demandes.remove(index);
                        adapter.notifyItemRemoved(index);
                    } else {
                        // fallback si l'index n'est pas trouvé
                        loadDemandes();
                    }

                    Toast.makeText(GestionDemandesBacActivity.this,
                            "Statut mis à jour : " + nouveauStatut,
                            Toast.LENGTH_SHORT).show();

                    if (demandes.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Aucune demande en attente");
                        recyclerDemandes.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(GestionDemandesBacActivity.this,
                            "Erreur " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DemandeBacResponse> call, Throwable t) {
                Toast.makeText(GestionDemandesBacActivity.this,
                        "Erreur réseau : " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ========= Adapter interne =========

    public static class DemandesAdapter extends RecyclerView.Adapter<DemandesAdapter.DemandeViewHolder> {

        public interface OnActionListener {
            void onAccepter(DemandeBacResponse demande);
            void onRefuser(DemandeBacResponse demande);
        }

        private final List<DemandeBacResponse> items;
        private final OnActionListener listener;

        public DemandesAdapter(List<DemandeBacResponse> items, OnActionListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public DemandeViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_demande_bac_mairie, parent, false);
            return new DemandeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DemandeViewHolder holder, int position) {
            DemandeBacResponse d = items.get(position);
            holder.bind(d, listener);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class DemandeViewHolder extends RecyclerView.ViewHolder {

            TextView tvTypeBac, tvRaison, tvAdresse, tvCommentaire, tvStatut;
            View btnAccepter, btnRefuser, layoutButtons;

            public DemandeViewHolder(android.view.View itemView) {
                super(itemView);
                tvTypeBac = itemView.findViewById(R.id.tvTypeBac);
                tvRaison = itemView.findViewById(R.id.tvRaison);
                tvAdresse = itemView.findViewById(R.id.tvAdresse);
                tvCommentaire = itemView.findViewById(R.id.tvCommentaire);
                tvStatut = itemView.findViewById(R.id.tvStatut);
                btnAccepter = itemView.findViewById(R.id.btnAccepter);
                btnRefuser = itemView.findViewById(R.id.btnRefuser);
                layoutButtons = itemView.findViewById(R.id.layoutButtons);
            }

            public void bind(DemandeBacResponse d, OnActionListener listener) {
                tvTypeBac.setText("Type : " + d.getTypeBac());
                tvRaison.setText("Raison : " + d.getRaison());
                tvAdresse.setText("Adresse : " + d.getAdresse());
                tvCommentaire.setText("Commentaire : " + (d.getCommentaire() != null ? d.getCommentaire() : "Aucun"));
                tvStatut.setText("Statut : " + d.getStatut());

                // Normalement on ne reçoit que des EN_ATTENTE, mais au cas où :
                if ("EN ATTENTE".equals(d.getStatut())) {
                    layoutButtons.setVisibility(View.VISIBLE);
                    btnAccepter.setOnClickListener(v -> listener.onAccepter(d));
                    btnRefuser.setOnClickListener(v -> listener.onRefuser(d));
                } else {
                    layoutButtons.setVisibility(View.GONE);
                }
            }
        }
    }
}
