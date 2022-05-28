package com.example.talentspartner;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.talentspartner.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    // Declare
    EditText etName, etEmail, etPhone, etAge, etTalents;
    Spinner spGender;
    ImageView ivProfileImage;
    Button btnUpdate;
    FirebaseAuth auth;
    FirebaseFirestore db;
    User user;
    private List<String> genders = Arrays.asList("- select gender -", "Male", "Female");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAge = findViewById(R.id.et_age);
        etTalents = findViewById(R.id.et_talents);
        spGender = findViewById(R.id.sp_gender);
        ivProfileImage = findViewById(R.id.iv_profile);
        btnUpdate = findViewById(R.id.btn_update);

        // Initialize Firebase Authentication & Database
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // show home button with title
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        // Feed spinner with data
        ArrayAdapter spinnerAdapter = new ArrayAdapter(ProfileActivity.this, android.R.layout.simple_spinner_dropdown_item, genders);
        spGender.setAdapter(spinnerAdapter);

        // Get user data
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null && !firebaseUser.getUid().isEmpty()) {
            DocumentReference docRef = db.collection("users").document(firebaseUser.getUid());
            docRef.get().addOnCompleteListener(dbTask -> {
                if (dbTask.isSuccessful()) {
                    DocumentSnapshot document = dbTask.getResult();
                    assert document != null;
                    if (document.exists()) {
                        // Instantiate logged in user
                        user = document.toObject(User.class);

                        String name = user.getName();
                        String email = user.getEmail();
                        String phone = user.getPhone();
                        String gender = user.getGender();
                        String age = String.valueOf(user.getAge());
                        String imageUrl = user.getImageUrl();
                        String talents = user.getTalents();

                        // set Drawer views
                        etName.setText(name);
                        etEmail.setText(email);
                        etPhone.setText(phone);
                        etAge.setText(age);
                        etTalents.setText(talents);

                        if (!gender.isEmpty()) {
                            if (gender.equals("Male"))
                                spGender.setSelection(1);
                            else
                                spGender.setSelection(2);
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.with(ProfileActivity.this)
                                    .load(imageUrl)
                                    .resize(96, 96)
                                    .centerCrop()
                                    .placeholder(R.drawable.person_placeholder)
                                    .into(ivProfileImage);
                        }
                    } else {
                        Toast.makeText(this, "User doesn't exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Cannot get user data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Button listener
        btnUpdate.setOnClickListener(view -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(ProfileActivity.this, "User has not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            User updatedUser = new User();
            updatedUser.setId(firebaseUser.getUid());
            updatedUser.setName(etName.getText().toString());
            updatedUser.setEmail(firebaseUser.getEmail());
            updatedUser.setPhone(etPhone.getText().toString());
            updatedUser.setAge(Integer.parseInt(etAge.getText().toString()));
            updatedUser.setTalents(etTalents.getText().toString());
            if (spGender.getSelectedItem().toString().equals("- select gender -"))
            {
                Toast.makeText(ProfileActivity.this, "Please select gender", Toast.LENGTH_SHORT).show();
                return;
            }
            updatedUser.setGender(spGender.getSelectedItem().toString());

            // Add user to Firebase Cloud Firestore
            Map<String, Object> userRecord = new HashMap<>();
            userRecord.put("id", updatedUser.getId());
            userRecord.put("name", updatedUser.getName());
            userRecord.put("email", updatedUser.getEmail());
            userRecord.put("phone", updatedUser.getPhone());
            userRecord.put("gender", updatedUser.getGender());
            userRecord.put("age", updatedUser.getAge());
            userRecord.put("talents", updatedUser.getTalents());

            db.collection("users")
                    .document(auth.getCurrentUser().getUid())
                    .update(userRecord)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "User profile has been updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "User profile update failed", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}