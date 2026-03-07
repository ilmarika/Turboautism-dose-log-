package com.example.turboautismdoselog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DrugAdapter adapter;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar export menu
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.action_export) {
                exportDatabaseToCSV();
                return true;
            }

            return false;
        });

        // Floating action button
        FloatingActionButton fab = findViewById(R.id.fabAddEntry);
        fab.setOnClickListener(v -> openAddEntrySheet());

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Database
        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "drug_database"
        ).allowMainThreadQueries().build();

        refreshList();
        setupSwipeDelete();
    }

    // Refresh RecyclerView
    private void refreshList() {

        List<DrugEntry> entries = db.drugDao().getAll();

        adapter = new DrugAdapter(entries, entry -> openEditEntrySheet(entry));
        recyclerView.setAdapter(adapter);
    }

    // Swipe to delete
    private void setupSwipeDelete() {

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
                        refreshList();

                        Snackbar.make(recyclerView, "Entry deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {

                                    db.drugDao().insert(deletedEntry);
                                    refreshList();

                                })
                                .show();
                    }
                };

        new ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView);
    }

    // Add new entry bottom sheet
    private void openAddEntrySheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_add_entry, null);
        dialog.setContentView(view);

        AutoCompleteTextView drug = view.findViewById(R.id.editDrugSheet);
        EditText route = view.findViewById(R.id.editRouteSheet);
        EditText dosage = view.findViewById(R.id.editDosageSheet);
        Button save = view.findViewById(R.id.buttonSaveSheet);

        setupDrugAutocomplete(drug);

        save.setOnClickListener(v -> {

            String drugText = drug.getText().toString();
            String routeText = route.getText().toString();
            String dosageText = dosage.getText().toString();

            if (drugText.isEmpty()) {
                Toast.makeText(this, "Drug name required", Toast.LENGTH_SHORT).show();
                return;
            }

            DrugEntry entry = new DrugEntry();
            entry.drug = drugText;
            entry.route = routeText;
            entry.dosage = dosageText;
            entry.timestamp = System.currentTimeMillis();

            db.drugDao().insert(entry);

            refreshList();
            dialog.dismiss();
        });

        dialog.show();
    }

    // Edit entry bottom sheet
    private void openEditEntrySheet(DrugEntry entry) {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_add_entry, null);
        dialog.setContentView(view);

        AutoCompleteTextView drug = view.findViewById(R.id.editDrugSheet);
        EditText route = view.findViewById(R.id.editRouteSheet);
        EditText dosage = view.findViewById(R.id.editDosageSheet);
        Button save = view.findViewById(R.id.buttonSaveSheet);

        setupDrugAutocomplete(drug);

        drug.setText(entry.drug);
        route.setText(entry.route);
        dosage.setText(entry.dosage);

        save.setOnClickListener(v -> {

            entry.drug = drug.getText().toString();
            entry.route = route.getText().toString();
            entry.dosage = dosage.getText().toString();

            db.drugDao().update(entry);

            refreshList();
            dialog.dismiss();
        });

        dialog.show();
    }

    // Drug autocomplete
    private void setupDrugAutocomplete(AutoCompleteTextView field) {

        List<DrugEntry> entries = db.drugDao().getAll();
        List<String> drugNames = new ArrayList<>();

        for (DrugEntry entry : entries) {

            if (entry.drug != null && !drugNames.contains(entry.drug)) {
                drugNames.add(entry.drug);
            }
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        drugNames
                );

        field.setAdapter(adapter);
        field.setThreshold(1);
        field.setOnClickListener(v -> field.showDropDown());
    }

    // CSV export
    private void exportDatabaseToCSV() {

        List<DrugEntry> entries = db.drugDao().getAll();

        SimpleDateFormat fileDateFormat =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());

        String timestamp = fileDateFormat.format(new Date());

        File downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File file = new File(downloadsDir, "drug_log_" + timestamp + ".csv");

        try {

            FileWriter writer = new FileWriter(file);

            writer.append("Drug,Route,Dosage,Timestamp\n");

            for (DrugEntry entry : entries) {

                Date date = new Date(entry.timestamp);

                SimpleDateFormat sdf =
                        new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

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
}