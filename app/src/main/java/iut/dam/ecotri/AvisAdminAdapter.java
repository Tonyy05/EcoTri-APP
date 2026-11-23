package iut.dam.ecotri;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AvisAdminAdapter extends RecyclerView.Adapter<AvisAdminAdapter.VH> {

    public interface OnDeleteListener {
        void onDelete(Avis avis);
    }

    private final List<Avis> data = new ArrayList<>();
    private final OnDeleteListener deleteListener;

    public AvisAdminAdapter(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setData(List<Avis> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avis_admin, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Avis a = data.get(position);

        String auteur = (a.auteur != null) ? a.auteur : "";
        String commentaire = (a.commentaire != null) ? a.commentaire : "";

        h.tvAuteur.setText(auteur);
        h.tvNote.setText("Note : " + a.note);
        h.tvCommentaire.setText(commentaire);

        h.btnSupprimer.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Supprimer l'avis")
                    .setMessage("Tu es sûr de vouloir supprimer cet avis ?")
                    .setNegativeButton("Annuler", null)
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        if (deleteListener != null) deleteListener.onDelete(a);
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAuteur, tvNote, tvCommentaire;
        Button btnSupprimer;

        VH(@NonNull View itemView) {
            super(itemView);
            tvAuteur = itemView.findViewById(R.id.tvAuteur);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvCommentaire = itemView.findViewById(R.id.tvCommentaire);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimer);
        }
    }
}
