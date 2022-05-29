package com.example.talentspartner;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.talentspartner.models.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
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
    FirebaseStorage storage;
    User user;
    private List<String> genders = Arrays.asList("- select gender -", "Male", "Female");
    private Uri imageUri = null;
    private String resultUrl = "";

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

        // Initialize Firebase Authentication, Database & Storage
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();


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
                                    .resize(256, 256)
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

        // Activity result launcher
        ActivityResultLauncher<String> choosePhotoActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        ivProfileImage.setImageURI(imageUri);
                    }
                });

        // ImageView listener
        ivProfileImage.setOnClickListener(view -> {
            choosePhotoActivityResultLauncher.launch("image/*");
        });

        // Button listener
        btnUpdate.setOnClickListener(view -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(ProfileActivity.this, "User has not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null)
                updateUserWithImage();
            else
                updateUserWithoutImage();
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

    private void updateUserWithImage() {
        File imageFile = new File(String.valueOf(imageUri));
        Timestamp timestamp = new Timestamp(new Date());
        String imageName = timestamp.getNanoseconds() + imageFile.getName();
        StorageReference ref = storage.getReference().child("images/" + imageName);
        UploadTask uploadTask = ref.putFile(imageUri);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();

                FirebaseUser firebaseUser = auth.getCurrentUser();
                assert firebaseUser != null;
                User updatedUser = new User();
                updatedUser.setId(firebaseUser.getUid());
                updatedUser.setName(etName.getText().toString());
                updatedUser.setEmail(firebaseUser.getEmail());
                updatedUser.setPhone(etPhone.getText().toString());
                updatedUser.setAge(Integer.parseInt(etAge.getText().toString()));
                updatedUser.setTalents(etTalents.getText().toString());
                updatedUser.setImageUrl(downloadUri.toString());
                if (spGender.getSelectedItem().toString().equals("- select gender -")) {
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
                userRecord.put("imageUrl", updatedUser.getImageUrl());

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
            } else {
                Toast.makeText(ProfileActivity.this, "Cannot get image download url", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUserWithoutImage() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(ProfileActivity.this, "User has not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser firebaseUser = auth.getCurrentUser();
        User updatedUser = new User();
        updatedUser.setId(firebaseUser.getUid());
        updatedUser.setName(etName.getText().toString());
        updatedUser.setEmail(firebaseUser.getEmail());
        updatedUser.setPhone(etPhone.getText().toString());
        updatedUser.setAge(Integer.parseInt(etAge.getText().toString()));
        updatedUser.setTalents(etTalents.getText().toString());
        if (spGender.getSelectedItem().toString().equals("- select gender -")) {
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
    }
}