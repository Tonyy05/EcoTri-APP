package iut.dam.ecotri;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import iut.dam.ecotri.data.DemandeBacResponse;

public class HistoriqueDemandesAdapter extends RecyclerView.Adapter<HistoriqueDemandesAdapter.VH> {

    private final List<DemandeBacResponse> items;

    public HistoriqueDemandesAdapter(List<DemandeBacResponse> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_demande_bac_historique, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DemandeBacResponse d = items.get(position);

        h.tvTypeBac.setText("Type : " + d.getTypeBac());
        h.tvRaison.setText("Raison : " + d.getRaison());
        h.tvAdresse.setText("Adresse : " + d.getAdresse());
        h.tvCommentaire.setText("Commentaire : " + (d.getCommentaire() != null ? d.getCommentaire() : "Aucun"));
        h.tvStatut.setText("Statut : " + d.getStatut());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTypeBac, tvRaison, tvAdresse, tvCommentaire, tvStatut;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTypeBac = itemView.findViewById(R.id.tvTypeBac);
            tvRaison = itemView.findViewById(R.id.tvRaison);
            tvAdresse = itemView.findViewById(R.id.tvAdresse);
            tvCommentaire = itemView.findViewById(R.id.tvCommentaire);
            tvStatut = itemView.findViewById(R.id.tvStatut);
        }
    }
}
