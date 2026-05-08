package com.suprabha.neuracareapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        // ✅ Always set layout first
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "MainActivity loaded!", Toast.LENGTH_SHORT).show();

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // ✅ If user is already logged in, go to dashboard
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        // ✅ Setup buttons
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        }

        if (registerButton != null) {
            registerButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(intent);
            });
        }
    }
}