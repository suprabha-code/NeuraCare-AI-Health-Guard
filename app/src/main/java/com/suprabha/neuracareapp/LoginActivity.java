package com.suprabha.neuracareapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnLogin, btnTogglePassword;
    private TextView forgotPassword, registerPrompt;
    private FirebaseAuth auth;
    private boolean isPasswordVisible = false;
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        forgotPassword = findViewById(R.id.forgotPassword);
        registerPrompt = findViewById(R.id.registerPrompt);
        auth = FirebaseAuth.getInstance();

        // Toggle password visibility
        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setText(getString(R.string.toggle_password_show));
            } else {
                editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setText(getString(R.string.toggle_password_hide));
            }
            isPasswordVisible = !isPasswordVisible;
            editPassword.setSelection(editPassword.getText().length());
        });

        // Forgot Password
        forgotPassword.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_LONG).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show();
                return;
            }

            showLoadingDialog();

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        hideLoadingDialog();
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset email sent. Please check your inbox or spam folder.", Toast.LENGTH_LONG).show();
                        } else {
                            Exception e = task.getException();
                            Log.e("PasswordReset", "Error: " + (e != null ? e.getMessage() : "Unknown"));
                            Toast.makeText(this, getString(R.string.password_reset_failed), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Register navigation
        registerPrompt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });

        // Login logic
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            showLoadingDialog();

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        hideLoadingDialog();
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();

                                // 🔹 Fetch user profile from Firebase
                                FirebaseDatabase.getInstance().getReference("Users").child(uid)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                UserModel userModel = snapshot.getValue(UserModel.class);
                                                if (userModel != null) {
                                                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                                    intent.putExtra("user_uid", uid);
                                                    intent.putExtra("user_name", userModel.name);
                                                    intent.putExtra("user_email", userModel.email);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "User profile not found", Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(LoginActivity.this, "Failed to load profile", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        } else {
                            handleLoginError(task.getException());
                        }
                    });
        });
    }

    private void showLoadingDialog() {
        loadingDialog = new AlertDialog.Builder(this)
                .setView(R.layout.dialog_loading)
                .setCancelable(false)
                .create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void handleLoginError(Exception exception) {
        if (exception != null) {
            Log.e("FirebaseLogin", "Login failed: " + exception.getMessage());

            if (exception instanceof FirebaseAuthInvalidUserException) {
                Toast.makeText(this, getString(R.string.error_user_not_found), Toast.LENGTH_LONG).show();
            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(this, getString(R.string.error_wrong_password), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.error_unknown), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.error_unknown), Toast.LENGTH_LONG).show();
        }
    }
}