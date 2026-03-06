package com.example.turboautismdoselog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.room.Room;
import com.google.android.material.snackbar.Snackbar;
import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editRoute, editDosage;

    AutoCompleteTextView editDrug;
    Button buttonSave;

    RecyclerView recyclerView;
    DrugAdapter adapter;

    AppDatabase db;
    Button buttonExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editDrug = findViewById(R.id.editDrug);
        editRoute = findViewById(R.id.editRoute);
        editDosage = findViewById(R.id.editDosage);
        buttonSave = findViewById(R.id.buttonSave);
        buttonExport = findViewById(R.id.buttonExport);

        buttonExport.setOnClickListener(v -> exportDatabaseToCSV());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "drug_database"
        ).allowMainThreadQueries().build();

        setupDrugAutocomplete();

        List<DrugEntry> entries = db.drugDao().getAll();
        adapter = new DrugAdapter(entries);
        recyclerView.setAdapter(adapter);

        buttonSave.setOnClickListener(v -> {

            String drug = editDrug.getText().toString();
            String route = editRoute.getText().toString();
            String dosage = editDosage.getText().toString();
            long timestamp = System.currentTimeMillis();

            if (drug.isEmpty()) {
                Toast.makeText(this, "Drug name required", Toast.LENGTH_SHORT).show();
                return;
            }

            DrugEntry entry = new DrugEntry();
            entry.drug = drug;
            entry.route = route;
            entry.dosage = dosage;
            entry.timestamp = timestamp;

            db.drugDao().insert(entry);

            List<DrugEntry> updatedEntries = db.drugDao().getAll();
            adapter = new DrugAdapter(updatedEntries);
            recyclerView.setAdapter(adapter);

            editDrug.setText("");
            editRoute.setText("");
            editDosage.setText("");
            setupDrugAutocomplete();
        });

        ItemTouchHelper.SimpleCallback swipeHandler =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                        int position = viewHolder.getAdapterPosition();

                        List<DrugEntry> entries = db.drugDao().getAll();
                        DrugEntry deletedEntry = entries.get(position);

                        db.drugDao().delete(deletedEntry);

                        List<DrugEntry> updatedEntries = db.drugDao().getAll();
                        adapter = new DrugAdapter(updatedEntries);
                        recyclerView.setAdapter(adapter);

                        Snackbar.make(recyclerView, "Entry deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {

                                    db.drugDao().insert(deletedEntry);

                                    List<DrugEntry> restoredEntries = db.drugDao().getAll();
                                    adapter = new DrugAdapter(restoredEntries);
                                    recyclerView.setAdapter(adapter);

                                })
                                .show();
                    }
                };

        new ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView);
    }
    private void exportDatabaseToCSV() {

        List<DrugEntry> entries = db.drugDao().getAll();

        SimpleDateFormat fileDateFormat =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());

        String timestamp = fileDateFormat.format(new Date());

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, "drug_log" + timestamp + ".csv");

        try {
            FileWriter writer = new FileWriter(file);

            writer.append("Drug,Route,Dosage,Timestamp\n");

            for (DrugEntry entry : entries) {
                Date date = new Date(entry.timestamp);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String formattedTime = sdf.format(date);

                writer.append(entry.drug).append(",");
                writer.append(entry.route).append(",");
                writer.append(entry.dosage).append(",");
                writer.append(formattedTime).append("\n");
            }

            writer.flush();
            writer.close();

            Toast.makeText(this, "CSV exported to Downloads", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed", Toast.LENGTH_LONG).show();
        }
    }
    private void setupDrugAutocomplete() {

        List<DrugEntry> entries = db.drugDao().getAll();
        List<String> drugNames = new java.util.ArrayList<>();

        for (DrugEntry entry : entries) {
            if (!drugNames.contains(entry.drug)) {
                drugNames.add(entry.drug);
            }
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        drugNames);

        editDrug.setAdapter(adapter);
    }
}