package com.example.talentspartner.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.talentspartner.ChatActivity;
import com.example.talentspartner.PartnerDetailsActivity;
import com.example.talentspartner.R;
import com.example.talentspartner.models.Friendship;
import com.example.talentspartner.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    // Declaration
    SwipeRefreshLayout swipeRefreshLayout;
    ListView lvTalentedPeople;
    List<User> talentedPeople = new ArrayList<>();
    MyAdapter adapter;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_friends, container, false);

        // Initialize
        swipeRefreshLayout = fragment.findViewById(R.id.swipe_refresh_layout);
        lvTalentedPeople = fragment.findViewById(R.id.lv_talented_people);

        // Initialize Firebase Auth & Database
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load all available people except the current user
        loadTalentedPeople();

        // Select item from list
        lvTalentedPeople.setOnItemClickListener((parent, view, position, id) -> {
            // Detect any changes
            adapter.notifyDataSetChanged();

            // Get selected person
            User person = talentedPeople.get(position);

            // send person data to details activity
            Bundle bundle = new Bundle();
            bundle.putSerializable("person", person);

            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        // Pull to refresh list
        swipeRefreshLayout.setOnRefreshListener(this::loadTalentedPeople);

        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadTalentedPeople() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        List<Friendship> friendships = new ArrayList<>();
        talentedPeople = new ArrayList<>();

        db.collection("friendships").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Friendship friendship = document.toObject(Friendship.class);
                    assert firebaseUser != null;
                    if ((friendship.getUserId().equals(firebaseUser.getUid()) || friendship.getPartnerId().equals(firebaseUser.getUid())) && friendship.isHasFriendship() && friendship.isHasRequestFulfilled())
                        friendships.add(friendship);
                }

                friendships.forEach(friendship -> {
                    db.collection("users").addSnapshotListener((value, error) -> {
                        assert value != null;
                        for (QueryDocumentSnapshot document : value) {
                            User person = document.toObject(User.class);
                            assert firebaseUser != null;
                            if (!person.getId().equals(firebaseUser.getUid()) && (friendship.getUserId().equals(person.getId()) || friendship.getPartnerId().equals(person.getId()) && friendship.isHasFriendship() && friendship.isHasRequestFulfilled())) {
                                talentedPeople.add(person);
                            }
                        }

                        // View list of people
                        if (talentedPeople.size() > 0 && getActivity() != null) {
                            adapter = new MyAdapter(getActivity(), talentedPeople);
                            lvTalentedPeople.setAdapter(adapter);
                        }

                        swipeRefreshLayout.setRefreshing(false);
                    });
                });
            } else {
                Toast.makeText(getActivity(), "No friends available yet!", Toast.LENGTH_LONG).show();
            }
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
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            @SuppressLint({"ViewHolder", "InflateParams"}) View v = inflater.inflate(R.layout.custom_person_card, null);
            MyAdapter.ViewHolder holder = new MyAdapter.ViewHolder();

            if(v != null){
                holder.ivAvatar = v.findViewById(R.id.iv_avatar);
                holder.tvPersonName = v.findViewById(R.id.tv_person_name);
                holder.tvPersonTalents = v.findViewById(R.id.tv_person_talents);
            }

            if(people.size() <= 0){
                holder.tvPersonName.setText("Unknown");
            } else {
                person = people.get(i);
                holder.tvPersonName.setText(person.getName());
                holder.tvPersonTalents.setText(person.getTalents());
                if (person.getImageUrl() != null && !person.getImageUrl().isEmpty()) {
                    Picasso.with(getActivity())
                            .load(person.getImageUrl())
                            .resize(96, 96)
                            .centerCrop()
                            .placeholder(R.drawable.person_placeholder)
                            .into(holder.ivAvatar);
                }
            }

            return v;
        }
    }
}