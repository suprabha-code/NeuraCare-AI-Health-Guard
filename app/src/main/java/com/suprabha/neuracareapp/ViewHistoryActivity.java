package com.suprabha.neuracareapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ViewHistoryActivity extends AppCompatActivity {

    private TextView textHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history); // Make sure this layout exists

        textHistory = findViewById(R.id.textHistory); // Ensure this ID exists in view_history_page.xml
        textHistory.setText(getString(R.string.view_history_placeholder));
    }
}