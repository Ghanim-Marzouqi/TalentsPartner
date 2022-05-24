package com.example.talentspartner.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.talentspartner.PartnerDetailsActivity;
import com.example.talentspartner.R;
import com.example.talentspartner.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchFragment extends Fragment {

    // Declaration
    SwipeRefreshLayout swipeRefreshLayout;
    ListView lvTalentedPeople;
    List<User> talentedPeople = new ArrayList<>();
    MyAdapter adapter;
    EditText etFilterByTalent;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize
        swipeRefreshLayout = fragment.findViewById(R.id.swipe_refresh_layout);
        lvTalentedPeople = fragment.findViewById(R.id.lv_talented_people);
        etFilterByTalent = fragment.findViewById(R.id.et_filter_by_talent);

        // Initialize Firebase Auth & Database
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load all available people except the current user
        loadTalentedPeople();

        // Pull to refresh list
        swipeRefreshLayout.setOnRefreshListener(this::loadTalentedPeople);

        // Select item from list
        lvTalentedPeople.setOnItemClickListener((parent, view, position, id) -> {
            // Detect any changes
            adapter.notifyDataSetChanged();

            // Get selected person
            User person = talentedPeople.get(position);

            // send person data to details activity
            Bundle bundle = new Bundle();
            bundle.putSerializable("person", person);

            Intent intent = new Intent(getActivity(), PartnerDetailsActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        // Filter list by talent
        etFilterByTalent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    List<User> filteredList = talentedPeople.stream().filter(p -> p.getTalents().contains(charSequence)).collect(Collectors.toList());
                    adapter = new MyAdapter(getActivity(), filteredList);
                    lvTalentedPeople.setAdapter(adapter);
                } else {
                    adapter = new MyAdapter(getActivity(), talentedPeople);
                    lvTalentedPeople.setAdapter(adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return fragment;
    }

    private void loadTalentedPeople() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        talentedPeople = new ArrayList<>();
        db.collection("users").addSnapshotListener((value, error) -> {
            assert value != null;
            for (QueryDocumentSnapshot document : value) {
                User person = document.toObject(User.class);
                if (firebaseUser == null || !person.getId().equals(firebaseUser.getUid()))
                    talentedPeople.add(person);
            }

            // View list of people
            if (talentedPeople.size() > 0 && getActivity() != null) {
                adapter = new MyAdapter(getActivity(), talentedPeople);
                lvTalentedPeople.setAdapter(adapter);
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
                if (!person.getImageUrl().isEmpty()) {
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