package com.suprabha.neuracareapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        EditText inputFullName = findViewById(R.id.inputFullName);
        EditText inputEmail = findViewById(R.id.inputEmail);
        EditText inputPhone = findViewById(R.id.inputPhone);
        EditText inputPassword = findViewById(R.id.inputPassword);
        EditText inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView loginLink = findViewById(R.id.loginLink);

        btnRegister.setOnClickListener(view -> {
            String name = inputFullName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();
            String password = inputPassword.getText().toString();
            String confirmPassword = inputConfirmPassword.getText().toString();

            // Validation
            if (name.isEmpty()) {
                inputFullName.setError("Full name is required");
                return;
            }

            if (email.isEmpty()) {
                inputEmail.setError("Email is required");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.setError("Enter a valid email");
                return;
            }

            if (phone.isEmpty()) {
                inputPhone.setError("Phone number is required");
                return;
            }

            if (phone.length() < 10) {
                inputPhone.setError("Enter a valid phone number");
                return;
            }

            if (password.isEmpty()) {
                inputPassword.setError("Password is required");
                return;
            }

            if (password.length() < 6) {
                inputPassword.setError("Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                inputConfirmPassword.setError("Passwords do not match");
                return;
            }

            // Check if email is already registered
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(checkTask -> {
                if (checkTask.isSuccessful()) {
                    boolean isRegistered = !checkTask.getResult().getSignInMethods().isEmpty();
                    if (isRegistered) {
                        Toast.makeText(this, "Email already registered. Please log in.", Toast.LENGTH_LONG).show();
                    } else {
                        // Proceed with registration
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            String uid = user.getUid();
                                            Log.d("Registration", "User UID: " + uid);

                                            UserModel userModel = new UserModel(name, email, phone);
                                            FirebaseDatabase.getInstance()
                                                    .getReference("Users")
                                                    .child(uid)
                                                    .setValue(userModel)
                                                    .addOnCompleteListener(dbTask -> {
                                                        if (dbTask.isSuccessful()) {
                                                            Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();

                                                            Intent intent = new Intent(this, DashboardActivity.class);
                                                            intent.putExtra("user_uid", uid);
                                                            intent.putExtra("user_name", name);
                                                            intent.putExtra("user_email", email);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(this, "User creation failed. Please try again.", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                            Toast.makeText(this, "Email already registered. Please log in.", Toast.LENGTH_LONG).show();
                                        } else {
                                            String errorMsg = (task.getException() != null)
                                                    ? task.getException().getMessage()
                                                    : "Unknown error";
                                            Toast.makeText(this, "Registration failed: " + errorMsg, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(this, "Failed to check email. Try again.", Toast.LENGTH_LONG).show();
                }
            });
        });

        loginLink.setOnClickListener(view -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}