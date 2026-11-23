package iut.dam.ecotri;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.VH> {

    public static class Item {
        public final int rank;
        public final String name;
        public final int points;
        public Item(int rank, String name, int points) {
            this.rank = rank;
            this.name = name;
            this.points = points;
        }
    }

    private final List<Item> items;

    public LeaderboardAdapter(List<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Item it = items.get(position);
        h.txtRank.setText("#" + it.rank);
        h.txtName.setText(it.name);
        h.txtPoints.setText(String.valueOf(it.points));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtRank, txtName, txtPoints;
        VH(@NonNull View itemView) {
            super(itemView);
            txtRank = itemView.findViewById(R.id.txtRank);
            txtName = itemView.findViewById(R.id.txtName);
            txtPoints = itemView.findViewById(R.id.txtPoints);
        }
    }
}
