package iut.dam.ecotri;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuizCorrectionAdapter extends RecyclerView.Adapter<QuizCorrectionAdapter.VH> {

    public static class Item {
        public int number;
        public boolean isCorrect;

        public Item(int number, boolean isCorrect) {
            this.number = number;
            this.isCorrect = isCorrect;
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    private final List<Item> items;
    private final OnItemClickListener listener;

    public QuizCorrectionAdapter(List<Item> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_correction_box, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Item it = items.get(position);
        h.txt.setText(String.valueOf(it.number));
        h.txt.setBackgroundColor(it.isCorrect ? Color.parseColor("#2E7D32") : Color.parseColor("#D32F2F"));
        h.txt.setTextColor(Color.WHITE);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txt;
        VH(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.txtBox);
        }
    }
}
