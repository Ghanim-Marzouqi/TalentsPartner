package com.example.talentspartner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.talentspartner.models.Friendship;
import com.example.talentspartner.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartnerDetailsActivity extends AppCompatActivity {

    // Declare
    TextView tvName, tvEmail, tvPhone, tvGender, tvAge, tvTalents;
    ImageView ivAvatar;
    Button btnUpdate;
    ProgressBar progressBar;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @SuppressLint("UseCompatLoadingForColorStateLists")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_details);

        // show home button with title
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Partner Details");
        }

        // Initialize
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvGender = findViewById(R.id.tv_gender);
        tvAge = findViewById(R.id.tv_age);
        tvTalents = findViewById(R.id.tv_talents);
        ivAvatar = findViewById(R.id.iv_avatar);
        btnUpdate = findViewById(R.id.btn_update);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize Firebase Auth & Database
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get Firebase User
        FirebaseUser firebaseUser = auth.getCurrentUser();

        // Settings on startup
        if (firebaseUser == null) {
            btnUpdate.setVisibility(View.GONE);
        }

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        User person = (User) bundle.getSerializable("person");

        if (person != null) {
            tvName.setText(person.getName());
            tvEmail.setText(person.getEmail());
            tvPhone.setText(person.getPhone());
            tvGender.setText(person.getGender());
            tvAge.setText(String.valueOf(person.getAge()));
            tvTalents.setText(person.getTalents());
            if (!person.getImageUrl().isEmpty()) {
                Picasso.with(PartnerDetailsActivity.this)
                        .load(person.getImageUrl())
                        .resize(256, 256)
                        .centerCrop()
                        .placeholder(R.drawable.person_placeholder)
                        .into(ivAvatar);
            }

            // Check if your are already partners
            assert firebaseUser != null;
            List<Friendship> friendships = new ArrayList<>();
            db.collection("friendships")
                    .get()
                    .addOnCompleteListener(task -> {
                        progressBar.setIndeterminate(false);
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Friendship friendship = document.toObject(Friendship.class);
                                friendships.add(friendship);
                            }

                            Friendship friendship = findFriendshipFromList(firebaseUser.getUid(), person.getId(), friendships);

                            if (friendship != null) {
                                if (!friendship.isHasFriendship() && !friendship.isHasRequestFulfilled()) {
                                    btnUpdate.setVisibility(View.VISIBLE);
                                    btnUpdate.setText("PENDING REQUEST");
                                    btnUpdate.setBackgroundTintList(getResources().getColorStateList(R.color.button_grey));
                                    btnUpdate.setEnabled(false);
                                } else if (!friendship.isHasFriendship() && friendship.isHasRequestFulfilled()) {
                                    btnUpdate.setVisibility(View.VISIBLE);
                                    btnUpdate.setText("FRIENDSHIP REJECTED");
                                    btnUpdate.setBackgroundTintList(getResources().getColorStateList(R.color.button_red));
                                    btnUpdate.setEnabled(false);
                                } else {
                                    btnUpdate.setVisibility(View.GONE);
                                }
                            } else {
                                btnUpdate.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(PartnerDetailsActivity.this, "Cannot get friendship status", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            btnUpdate.setVisibility(View.GONE);
        }

        btnUpdate.setOnClickListener(view -> {
            List<Friendship> friendships = new ArrayList<>();

            assert firebaseUser != null;
            db.collection("friendships")
                    .whereEqualTo("userId", firebaseUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Friendship friendship = document.toObject(Friendship.class);
                                friendships.add(friendship);
                            }

                            if (friendships.size() < 1) {
                                // Create a hashmap to store friendship data
                                Map<String, Object> data = new HashMap<>();
                                data.put("userId", firebaseUser.getUid());
                                data.put("partnerId", person.getId());
                                data.put("hasFriendship", false);
                                data.put("hasRequestFulfilled", false);

                                db.collection("friendships").document().set(data).addOnCompleteListener(innerTask -> {
                                    if (innerTask.isSuccessful()) {
                                        Toast.makeText(PartnerDetailsActivity.this, "Your request has been sent successfully", Toast.LENGTH_SHORT).show();
                                        btnUpdate.setText("PENDING REQUEST");
                                        btnUpdate.setBackgroundTintList(getResources().getColorStateList(R.color.button_grey));
                                        btnUpdate.setEnabled(false);
                                    } else {
                                        Toast.makeText(PartnerDetailsActivity.this, "Cannot send a friendship request", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                boolean isFriendshipRequestExists = false;

                                for (Friendship friendship : friendships) {
                                    assert person != null;
                                    isFriendshipRequestExists = friendship.getPartnerId().equals(person.getId());
                                }

                                if (!isFriendshipRequestExists) {
                                    // Create a hashmap to store friendship data
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("userId", firebaseUser.getUid());
                                    data.put("partnerId", person.getId());
                                    data.put("hasFriendship", false);
                                    data.put("hasRequestFulfilled", false);

                                    db.collection("friendships").document().set(data).addOnCompleteListener(innerTask -> {
                                        if (innerTask.isSuccessful()) {
                                            Toast.makeText(PartnerDetailsActivity.this, "Your request has been sent successfully", Toast.LENGTH_SHORT).show();
                                            btnUpdate.setText("PENDING REQUEST");
                                            btnUpdate.setBackgroundTintList(getResources().getColorStateList(R.color.button_grey));
                                            btnUpdate.setEnabled(false);
                                        } else {
                                            Toast.makeText(PartnerDetailsActivity.this, "Cannot send a friendship request", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(PartnerDetailsActivity.this, "Friendship request already sent", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(PartnerDetailsActivity.this, "Cannot get user friendships", Toast.LENGTH_LONG).show();
                        }
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

    private Friendship findFriendshipFromList (String userId, String partnerId, List<Friendship> friendships) {
        for (Friendship friendship : friendships) {
            if ((friendship.getUserId().equals(userId) && friendship.getPartnerId().equals(partnerId)) || (friendship.getPartnerId().equals(userId) && friendship.getUserId().equals(partnerId))) {
                return friendship;
            }
        }
        return null;
    }
}