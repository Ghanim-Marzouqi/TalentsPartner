package com.example.talentspartner.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.talentspartner.R;
import com.example.talentspartner.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    List<User> talentedPeople = new ArrayList<>();
    ListView lvTalentedPeople;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize
        lvTalentedPeople = fragment.findViewById(R.id.lv_talented_people);

        // Initialize Firebase Auth & Database
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser firebaseUser = auth.getCurrentUser();

        // Load all available people except the current user
        db.collection("users").addSnapshotListener((value, error) -> {
            assert value != null;
            for (QueryDocumentSnapshot document : value) {
                User person = document.toObject(User.class);
                if (firebaseUser == null || !person.getId().equals(firebaseUser.getUid()))
                    talentedPeople.add(person);
            }

            // View list of people
            if (talentedPeople.size() > 0) {
                MyAdapter adapter = new MyAdapter(getActivity(), talentedPeople);
                lvTalentedPeople.setAdapter(adapter);
            }
        });

        return fragment;
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
            public ImageView ivViewDetails;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            @SuppressLint({"ViewHolder", "InflateParams"}) View v = inflater.inflate(R.layout.custom_person_card, null);
            MyAdapter.ViewHolder holder = new MyAdapter.ViewHolder();

            if(v != null){
                holder.ivAvatar = v.findViewById(R.id.iv_avatar);
                holder.tvPersonName = v.findViewById(R.id.tv_person_name);
                holder.tvPersonTalents = v.findViewById(R.id.tv_person_talents);
                holder.ivViewDetails = v.findViewById(R.id.iv_view_details);
            }

            if(people.size() <= 0){
                holder.tvPersonName.setText("Unknown");
            } else {
                person = people.get(i);
                holder.tvPersonName.setText(person.getName());
                holder.tvPersonTalents.setText(person.getTalents());
                if (!person.getImageUrl().isEmpty()) {
                    Picasso.with(getActivity())
                            .load(person.getImageUrl())
                            .resize(96, 96)
                            .centerCrop()
                            .placeholder(R.drawable.male_placeholder)
                            .into(holder.ivAvatar);
                }
            }

            return v;
        }
    }
}