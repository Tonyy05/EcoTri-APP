package iut.dam.ecotri;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScoreHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SCORE = 1;

    public interface Row {}

    public static class HeaderRow implements Row {
        public final String title;
        public HeaderRow(String title) { this.title = title; }
    }

    public static class ScoreRow implements Row {
        public final String quizTitle;
        public final int points;
        public final String date;
        public ScoreRow(String quizTitle, int points, String date) {
            this.quizTitle = quizTitle;
            this.points = points;
            this.date = date;
        }
    }

    private final List<Row> rows;

    public ScoreHistoryAdapter(List<Row> rows) {
        this.rows = rows;
    }

    @Override
    public int getItemViewType(int position) {
        return (rows.get(position) instanceof HeaderRow) ? TYPE_HEADER : TYPE_SCORE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inf.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new HeaderVH(v);
        } else {
            View v = inf.inflate(R.layout.item_score, parent, false);
            return new ScoreVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row r = rows.get(position);
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).title.setText(((HeaderRow) r).title);
        } else {
            ScoreRow it = (ScoreRow) r;
            ScoreVH h = (ScoreVH) holder;
            h.txtQuizTitle.setText(it.quizTitle);
            h.txtScoreLine.setText("Score : " + it.points);
            h.txtDate.setText(it.date);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView title;
        HeaderVH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            title.setTextSize(18);
            title.setPadding(16, 24, 16, 12);
        }
    }

    static class ScoreVH extends RecyclerView.ViewHolder {
        TextView txtQuizTitle, txtScoreLine, txtDate;
        ScoreVH(@NonNull View itemView) {
            super(itemView);
            txtQuizTitle = itemView.findViewById(R.id.txtQuizTitle);
            txtScoreLine = itemView.findViewById(R.id.txtScoreLine);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }
}
