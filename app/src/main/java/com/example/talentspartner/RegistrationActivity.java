package com.example.talentspartner;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    // Declaration
    private EditText etName, etEmail, etPhone, etAge, etPassword, etConfirmPassword;
    private Spinner spGender;
    private ImageView ivBack;
    private Button btnSignUp;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private List<String> genders = Arrays.asList("- select gender -", "Male", "Female");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialization
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAge = findViewById(R.id.et_age);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        spGender = findViewById(R.id.sp_gender);
        ivBack = findViewById(R.id.iv_back);
        btnSignUp = findViewById(R.id.btn_sign_up);

        // Initialize Firebase Auth & Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Feed spinner with data
        ArrayAdapter spinnerAdapter = new ArrayAdapter(RegistrationActivity.this, android.R.layout.simple_spinner_dropdown_item, genders);
        spGender.setAdapter(spinnerAdapter);

        // Click listeners
        btnSignUp.setOnClickListener(view -> {
            // Read user input
            String name = etName.getText().toString();
            String email = etEmail.getText().toString();
            String phone = etPhone.getText().toString();
            String age = etAge.getText().toString();
            String gender = spGender.getSelectedItem().toString();
            String password = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            // Validate user input
            if (!isValidInput(name, email, phone, gender, age, password, confirmPassword)) {
                return;
            }

            // Register user in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = auth.getCurrentUser();

                    // Add user to Firebase Cloud Firestore
                    Map<String, Object> userRecord = new HashMap<>();
                    userRecord.put("id", user.getUid());
                    userRecord.put("name", name);
                    userRecord.put("email", email);
                    userRecord.put("phone", phone);
                    userRecord.put("age", Integer.parseInt(age));
                    userRecord.put("gender", gender);
                    userRecord.put("imageUrl", "");
                    userRecord.put("talents", "");

                    db.collection("users")
                            .document(user.getUid())
                            .set(userRecord)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(RegistrationActivity.this, "User registration successful", Toast.LENGTH_SHORT).show();

                                // Send email verification
                                if (auth.getCurrentUser() != null)
                                    auth.getCurrentUser().sendEmailVerification();

                                // Go to login page
                                goToLoginPage();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RegistrationActivity.this, "User registration failed", Toast.LENGTH_SHORT).show();

                                // Delete user from Firebase Authentication
                                user.delete();
                            });
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(RegistrationActivity.this, "User registration failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        ivBack.setOnClickListener(view -> {
            finish();
        });
    }

    private boolean isValidInput(String name, String email, String phone, String gender, String age, String password, String confirmPassword) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (gender.isEmpty() || gender.equals("- select gender -")) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (age.isEmpty()) {
            Toast.makeText(this, "Please enter your age", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please confirm password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Entered email address is invalid", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidPhone(phone)) {
            return false;
        }

        if (!password.matches(confirmPassword)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private boolean isValidPhone(String target) {
        if (target.length() != 8) {
            Toast.makeText(this, "Phone number must be 8 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!target.startsWith("7") && !target.startsWith("9")) {
            Toast.makeText(this, "Phone number must start with 7 or 9", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void goToLoginPage(){
        new Handler().postDelayed(() -> {
            // Sign out user
            auth.signOut();
            finish();
        }, 2000);
    }
}