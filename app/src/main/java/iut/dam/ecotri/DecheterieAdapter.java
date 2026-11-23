package iut.dam.ecotri;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DecheterieAdapter extends RecyclerView.Adapter<DecheterieAdapter.VH> {

    private final Context context;
    private final List<Decheterie> data = new ArrayList<>();

    public DecheterieAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Decheterie> newData) {
        data.clear();
        if (newData != null) data.addAll(newData);
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

        h.tvNom.setText(d.nom != null ? d.nom : "");
        h.tvAdresse.setText("Adresse : " + (d.adresse != null ? d.adresse : ""));
        h.tvHoraires.setText("Horaires : " + (d.horaires != null ? d.horaires : ""));
        h.tvAcces.setText("Accès : " + (d.acces != null ? d.acces: ""));

        h.btnItineraire.setOnClickListener(v -> {
            String uri = "google.navigation:q=" + d.lat + "," + d.lng;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            context.startActivity(intent);
        });
        h.btnRdv.setOnClickListener(v -> {
            String url = "https://www.ecorec-online.fr/926113/Portail/#/ecoportail/rdv";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        });


        h.btnAvis.setOnClickListener(v -> {
            Intent intent = new Intent(context, AvisActivity.class);
            intent.putExtra("DECHE_ID", String.valueOf(d.id)); // ✅ ID NUMÉRIQUE
            intent.putExtra("DECHE_NOM", d.nom);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNom, tvAdresse, tvHoraires, tvAcces;
        Button btnItineraire, btnAvis, btnRdv;

        VH(@NonNull View itemView) {
            super(itemView);
            tvNom = itemView.findViewById(R.id.tvNom);
            tvAdresse = itemView.findViewById(R.id.tvAdresse);
            tvHoraires = itemView.findViewById(R.id.tvHoraires);
            tvAcces = itemView.findViewById(R.id.tvAcces);
            btnItineraire = itemView.findViewById(R.id.btnItineraire);
            btnAvis = itemView.findViewById(R.id.btnAvis);
            btnRdv = itemView.findViewById(R.id.btnRdv);

        }
    }
}
