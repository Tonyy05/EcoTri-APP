package iut.dam.ecotri;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DecheterieSelectAdapter extends RecyclerView.Adapter<DecheterieSelectAdapter.VH> {

    public interface OnSelectListener {
        void onSelect(Decheterie decheterie);
    }

    private final List<Decheterie> data = new ArrayList<>();
    private final OnSelectListener listener;

    public DecheterieSelectAdapter(OnSelectListener listener) {
        this.listener = listener;
    }

    public void setData(List<Decheterie> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_decheterie, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Decheterie d = data.get(position);

        String nom = d.nom != null ? d.nom : "";
        String adresse = d.adresse != null ? d.adresse : "";
        String horaires = d.horaires != null ? d.horaires : "";
        String acces = d.acces != null ? d.acces : "";

        h.tvNom.setText(nom);
        h.tvAdresse.setText("Adresse : " + adresse);
        h.tvHoraires.setText("Horaires : " + horaires);
        h.tvAcces.setText("Accès : " + acces);

        // Itinéraire (garde l’existant)
        h.btnItineraire.setOnClickListener(v -> {
            String uri = "google.navigation:q=" + d.lat + "," + d.lng;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            v.getContext().startActivity(intent);
        });

        // En mairie, on n'utilise pas "Avis" → on peut le cacher
        if (h.btnAvis != null) {
            h.btnAvis.setVisibility(View.GONE);
        }

        // Clic sur toute la carte => sélection
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSelect(d);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNom, tvAdresse, tvHoraires, tvAcces;
        Button btnItineraire, btnAvis;

        VH(@NonNull View itemView) {
            super(itemView);
            tvNom = itemView.findViewById(R.id.tvNom);
            tvAdresse = itemView.findViewById(R.id.tvAdresse);
            tvHoraires = itemView.findViewById(R.id.tvHoraires);
            tvAcces = itemView.findViewById(R.id.tvAcces);
            btnItineraire = itemView.findViewById(R.id.btnItineraire);
            btnAvis = itemView.findViewById(R.id.btnAvis);
        }
    }
}

