package com.example.turboautismdoselog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText editDrug, editRoute, editDosage;
    Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editDrug = findViewById(R.id.editDrug);
        editRoute = findViewById(R.id.editRoute);
        editDosage = findViewById(R.id.editDosage);
        buttonSave = findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(v -> {

            String drug = editDrug.getText().toString();
            String route = editRoute.getText().toString();
            String dosage = editDosage.getText().toString();
            long timestamp = System.currentTimeMillis();

            Toast.makeText(this,
                    "Saved:\n" +
                            "Drug: " + drug + "\n" +
                            "Route: " + route + "\n" +
                            "Dosage: " + dosage + "\n" +
                            "Timestamp: " + timestamp,
                    Toast.LENGTH_LONG).show();
        });
    }
}