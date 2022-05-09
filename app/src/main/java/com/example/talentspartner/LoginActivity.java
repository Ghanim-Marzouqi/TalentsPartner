package com.example.talentspartner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // Declaration
    private EditText etEmail, etPassword;
    private Checkable cbRememberMe;
    private TextView tvResetPassword;
    private Button btnSignIn, btnSignUp;
    private LinearLayoutCompat llSkip;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialization
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        tvResetPassword = findViewById(R.id.tv_reset_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        btnSignUp = findViewById(R.id.btn_sign_up);
        llSkip = findViewById(R.id.ll_skip);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Click listeners
        btnSignIn.setOnClickListener(view -> {
            // Read user input
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            // Validate user input
            if (!isValid(email, password)) {
                return;
            }

            // TODO: Check if user registered

            // Sign in user
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = auth.getCurrentUser();
                    Log.i("User Id", user.getUid());
                    goToHomePage();
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        btnSignUp.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });

        tvResetPassword.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        llSkip.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = auth.getCurrentUser();

        if(currentUser != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
    }

    private boolean isValid(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Entered email address is invalid", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void goToHomePage() {
        startActivity(new Intent(this, MainActivity.class));
    }
}