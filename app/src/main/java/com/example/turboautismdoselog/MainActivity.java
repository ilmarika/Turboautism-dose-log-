package com.example.turboautismdoselog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editDrug, editRoute, editDosage;
    Button buttonSave;
    RecyclerView recyclerView;
    DrugAdapter adapter;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "drug_database"
        ).allowMainThreadQueries().build();

        editDrug = findViewById(R.id.editDrug);
        editRoute = findViewById(R.id.editRoute);
        editDosage = findViewById(R.id.editDosage);
        buttonSave = findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(v -> {

            String drug = editDrug.getText().toString();
            String route = editRoute.getText().toString();
            String dosage = editDosage.getText().toString();
            long timestamp = System.currentTimeMillis();

            DrugEntry entry = new DrugEntry();
            entry.drug = drug;
            entry.route = route;
            entry.dosage = dosage;
            entry.timestamp = timestamp;

            db.drugDao().insert(entry);
            List<DrugEntry> updatedEntries = db.drugDao().getAll();
            adapter = new DrugAdapter(updatedEntries);
            recyclerView.setAdapter(adapter);

            Toast.makeText(this,
                    "Saved to database",
                    Toast.LENGTH_LONG).show();

            editDrug.setText("");
            editRoute.setText("");
            editDosage.setText("");
        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<DrugEntry> entries = db.drugDao().getAll();
        adapter = new DrugAdapter(entries);
        recyclerView.setAdapter(adapter);
    }
}