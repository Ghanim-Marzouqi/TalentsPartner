package com.example.talentspartner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.talentspartner.models.Friendship;
import com.example.talentspartner.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendshipRequestsActivity extends AppCompatActivity {

    // Declaration
    SwipeRefreshLayout swipeRefreshLayout;
    ListView lvFriendshipRequests;
    List<Friendship> friendshipRequests = new ArrayList<>();
    MyAdapter adapter;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendship_requests);

        // show home button with title
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Friendship Requests");
        }

        // Initialize
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        lvFriendshipRequests = findViewById(R.id.lv_friendship_requests);

        // Initialize Firebase Auth & Database
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load all available people except the current user
        loadFriendshipRequests();

        // Pull to refresh list
        swipeRefreshLayout.setOnRefreshListener(this::loadFriendshipRequests);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadFriendshipRequests() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        List<User> talentedPeople = new ArrayList<>();
        friendshipRequests = new ArrayList<>();
        db.collection("friendships").addSnapshotListener((value, error) -> {
            assert value != null;
            for (QueryDocumentSnapshot document : value) {
                Friendship friendship = document.toObject(Friendship.class);
                assert firebaseUser != null;
                if (!friendship.getUserId().equals(firebaseUser.getUid()) && !friendship.isHasRequestFulfilled() && friendship.getPartnerId().equals(firebaseUser.getUid()))
                    friendshipRequests.add(friendship);
            }

            // View list of people
            if (friendshipRequests.size() > 0) {
                db.collection("users").addSnapshotListener((value1, error1) -> {
                    assert value1 != null;
                    for (QueryDocumentSnapshot document : value1) {
                        User person = document.toObject(User.class);
                        friendshipRequests.forEach(friendship -> {
                            if (person.getId().equals(friendship.getUserId())) {
                                talentedPeople.add(person);
                            }
                        });
                    }

                    if (talentedPeople.size() > 0) {
                        adapter = new MyAdapter(FriendshipRequestsActivity.this, talentedPeople);
                        lvFriendshipRequests.setAdapter(adapter);
                    }
                });
            }

            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private class MyAdapter extends BaseAdapter {

        List<User> people;
        LayoutInflater inflater;
        User person = new User();

        public MyAdapter(Activity activity, List<User> people) {
            this.people = people;
            inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if(people.size() <= 0){
                return 1;
            }
            return people.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public class ViewHolder{
            public ImageView ivAvatar;
            public TextView tvPersonName;
            public TextView tvPersonTalents;
            public ImageView ivCheck;
            public ImageView ivCancel;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            @SuppressLint({"ViewHolder", "InflateParams"}) View v = inflater.inflate(R.layout.custom_friendship_request_card, null);
            ViewHolder holder = new ViewHolder();

            if(v != null){
                holder.ivAvatar = v.findViewById(R.id.iv_avatar);
                holder.tvPersonName = v.findViewById(R.id.tv_person_name);
                holder.tvPersonTalents = v.findViewById(R.id.tv_person_talents);
                holder.ivCheck = v.findViewById(R.id.iv_check);
                holder.ivCancel = v.findViewById(R.id.iv_cancel);
            }

            if(people.size() <= 0){
                holder.tvPersonName.setText("Unknown");
            } else {
                person = people.get(i);
                holder.tvPersonName.setText(person.getName());
                holder.tvPersonTalents.setText(person.getTalents());
                if (person.getImageUrl() != null && !person.getImageUrl().isEmpty()) {
                    Picasso.with(FriendshipRequestsActivity.this)
                            .load(person.getImageUrl())
                            .resize(96, 96)
                            .centerCrop()
                            .placeholder(R.drawable.person_placeholder)
                            .into(holder.ivAvatar);
                }

                holder.ivCheck.setOnClickListener(view1 -> {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    db.collection("friendships").whereEqualTo("userId", person.getId()).whereEqualTo("partnerId", firebaseUser.getUid()).whereEqualTo("hasFriendship", false).whereEqualTo("hasRequestFulfilled", false).get().addOnCompleteListener(task -> {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        if (document != null && document.exists()) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("userId", document.getString("userId"));
                            data.put("partnerId", document.getString("partnerId"));
                            data.put("hasFriendship", true);
                            data.put("hasRequestFulfilled", true);

                            db.collection("friendships").document(document.getId()).update(data).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(FriendshipRequestsActivity.this, "Request Accepted", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }
                    });
                });

                holder.ivCancel.setOnClickListener(view12 -> {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    db.collection("friendships").whereEqualTo("userId", person.getId()).whereEqualTo("partnerId", firebaseUser.getUid()).whereEqualTo("hasFriendship", false).whereEqualTo("hasRequestFulfilled", false).get().addOnCompleteListener(task -> {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        if (document != null && document.exists()) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("userId", document.getString("userId"));
                            data.put("partnerId", document.getString("partnerId"));
                            data.put("hasFriendship", false);
                            data.put("hasRequestFulfilled", true);

                            db.collection("friendships").document(document.getId()).update(data).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(FriendshipRequestsActivity.this, "Request Rejected", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }
                    });
                });
            }

            return v;
        }
    }
}