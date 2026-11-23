package iut.dam.ecotri;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AvisAdapter extends RecyclerView.Adapter<AvisAdapter.VH> {

    public interface OnAvisActionListener {
        void onDelete(Avis avis);
    }

    private final List<Avis> data;
    private final Integer myUserId;
    private final OnAvisActionListener listener;

    public AvisAdapter(List<Avis> data, Integer myUserId, OnAvisActionListener listener) {
        this.data = data;
        this.myUserId = myUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avis, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Avis a = data.get(position);

        String auteur = (a.auteur != null && !a.auteur.isEmpty()) ? a.auteur : "Anonyme";
        h.tvHeader.setText(auteur + " • " + a.note + "/5");
        h.tvComment.setText(a.commentaire != null ? a.commentaire : "");
        h.tvDate.setText(a.createdAt != null ? a.createdAt : "");

        boolean isMine = myUserId != null && a.userId != null && a.userId.equals(myUserId);

        h.tvMine.setVisibility(isMine ? View.VISIBLE : View.GONE);
        h.btnDeleteMine.setVisibility(isMine ? View.VISIBLE : View.GONE);

        h.btnDeleteMine.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(a);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvHeader, tvComment, tvDate, tvMine;
        Button btnDeleteMine;

        VH(@NonNull View itemView) {
            super(itemView);
            tvMine = itemView.findViewById(R.id.tvMine);
            tvHeader = itemView.findViewById(R.id.tvHeader);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDeleteMine = itemView.findViewById(R.id.btnDeleteMine);
        }
    }
}
