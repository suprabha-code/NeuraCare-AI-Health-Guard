package com.suprabha.neuracareapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditHistoryActivity extends AppCompatActivity {

    private EditText editHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_history); // Make sure this layout exists

        editHistory = findViewById(R.id.editHistory);
        Button btnSave = findViewById(R.id.btnSaveHistory);

        btnSave.setOnClickListener(v -> {
            String history = editHistory.getText().toString().trim();
            Toast.makeText(this, "Saved: " + history, Toast.LENGTH_SHORT).show();
        });
    }
}