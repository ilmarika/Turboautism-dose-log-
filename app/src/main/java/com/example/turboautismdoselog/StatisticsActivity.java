package com.example.turboautismdoselog;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private TextView statTotalEntries;
    private TextView statMostUsedDrug;
    private TextView statLastDose;
    private TextView statAvgPerDay;
    private View emptyState;
    private View statisticsContent;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        statTotalEntries = findViewById(R.id.statTotalEntries);
        statMostUsedDrug = findViewById(R.id.statMostUsedDrug);
        statLastDose = findViewById(R.id.statLastDose);
        statAvgPerDay = findViewById(R.id.statAvgPerDay);
        emptyState = findViewById(R.id.emptyState);
        statisticsContent = findViewById(R.id.statisticsContent);

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "drug_database"
        ).allowMainThreadQueries().build();

        loadStatistics();
    }

    private void loadStatistics() {

        List<DrugEntry> entries = db.drugDao().getAll();

        // Total entries
        statTotalEntries.setText(String.valueOf(entries.size()));

        if (entries.isEmpty()) {

            emptyState.setVisibility(View.VISIBLE);
            statisticsContent.setVisibility(View.GONE);
            return;

        } else {

            emptyState.setVisibility(View.GONE);
            statisticsContent.setVisibility(View.VISIBLE);
        }

        if (entries.isEmpty()) {
            statMostUsedDrug.setText("—");
            statLastDose.setText("—");
            return;
        }

        // Count drugs
        HashMap<String, Integer> counts = new HashMap<>();

        for (DrugEntry entry : entries) {

            String drug = entry.drug;

            if (!counts.containsKey(drug)) {
                counts.put(drug, 1);
            } else {
                counts.put(drug, counts.get(drug) + 1);
            }
        }

        // Find most used drug
        String mostUsed = null;
        int maxCount = 0;

        for (String drug : counts.keySet()) {

            int count = counts.get(drug);

            if (count > maxCount) {
                maxCount = count;
                mostUsed = drug;
            }
        }

        // Average doses per day

        if (entries.size() < 2) {

            statAvgPerDay.setText("—");

        } else {

            long minTimestamp = Long.MAX_VALUE;
            long maxTimestamp = Long.MIN_VALUE;

            for (DrugEntry entry : entries) {

                if (entry.timestamp < minTimestamp) {
                    minTimestamp = entry.timestamp;
                }

                if (entry.timestamp > maxTimestamp) {
                    maxTimestamp = entry.timestamp;
                }
            }

            long diffMillis = maxTimestamp - minTimestamp;

            double days = diffMillis / (1000.0 * 60 * 60 * 24);

            if (days < 1) {
                days = 1;
            }

            double avg = entries.size() / days;

            statAvgPerDay.setText(String.format(Locale.getDefault(), "%.2f", avg));
        }

        statMostUsedDrug.setText(mostUsed);

        // Last dose
        DrugEntry lastEntry = entries.get(entries.size() - 1);

        Date date = new Date(lastEntry.timestamp);

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        statLastDose.setText(sdf.format(date));
    }
}