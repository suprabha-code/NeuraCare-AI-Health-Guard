package com.suprabha.neuracareapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EmergencyContactActivity extends AppCompatActivity {

    private EditText contactNameInput, contactPhoneInput;
    private Button addContactButton;
    private ListView contactListView;

    private ArrayList<String> contactList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        contactNameInput = findViewById(R.id.contactNameInput);
        contactPhoneInput = findViewById(R.id.contactPhoneInput);
        addContactButton = findViewById(R.id.addContactButton);
        contactListView = findViewById(R.id.contactListView);

        contactList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        contactListView.setAdapter(adapter);

        addContactButton.setOnClickListener(v -> {
            String name = contactNameInput.getText().toString().trim();
            String phone = contactPhoneInput.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter both name and phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            contactList.add(name + " - " + phone);
            adapter.notifyDataSetChanged();

            contactNameInput.setText("");
            contactPhoneInput.setText("");
        });
    }
}