package com.example.turboautismdoselog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DrugAdapter extends RecyclerView.Adapter<DrugAdapter.ViewHolder> {

    private List<DrugEntry> entries;
    private OnItemLongClickListener listener;

    // Interface for long press editing
    public interface OnItemLongClickListener {
        void onItemLongClick(DrugEntry entry);
    }

    public DrugAdapter(List<DrugEntry> entries, OnItemLongClickListener listener) {
        this.entries = entries;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drug_entry, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        DrugEntry entry = entries.get(position);

        holder.textDrug.setText(entry.drug);
        holder.textRoute.setText("Route: " + entry.route);
        holder.textDosage.setText("Dosage: " + entry.dosage);

        // Format timestamp
        Date date = new Date(entry.timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.textTimestamp.setText(sdf.format(date));

        // Long press → edit entry
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(entry);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textDrug;
        TextView textRoute;
        TextView textDosage;
        TextView textTimestamp;

        public ViewHolder(View itemView) {
            super(itemView);

            textDrug = itemView.findViewById(R.id.textDrug);
            textRoute = itemView.findViewById(R.id.textRoute);
            textDosage = itemView.findViewById(R.id.textDosage);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
        }
    }
}