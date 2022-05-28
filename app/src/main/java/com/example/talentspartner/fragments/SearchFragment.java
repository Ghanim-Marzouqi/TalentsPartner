package com.example.talentspartner.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.talentspartner.PartnerDetailsActivity;
import com.example.talentspartner.R;
import com.example.talentspartner.RegistrationActivity;
import com.example.talentspartner.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SearchFragment extends Fragment {

    // Declaration
    SwipeRefreshLayout swipeRefreshLayout;
    ListView lvTalentedPeople;
    List<User> talentedPeople = new ArrayList<>();
    MyAdapter adapter;
    SearchDialog searchDialog;
    EditText etFilterByTalent;
    ImageView ivSearch;
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
        ivSearch = fragment.findViewById(R.id.iv_search);
        searchDialog = new SearchDialog(getActivity());

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
                    List<User> filteredList = talentedPeople.stream().filter(p -> p.getTalents().toLowerCase(Locale.ROOT).contains(charSequence.toString().toLowerCase(Locale.ROOT))).collect(Collectors.toList());
                    adapter = new MyAdapter(getActivity(), filteredList);
                } else {
                    adapter = new MyAdapter(getActivity(), talentedPeople);
                }
                lvTalentedPeople.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Search list
        ivSearch.setOnClickListener(view -> {
            searchDialog.show();
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

    private class SearchDialog extends Dialog {

        Context context;
        List<String> genders = Arrays.asList("- select gender -", "Male", "Female");

        public SearchDialog(@NonNull Context context) {
            super(context);
            this.context = context;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_search_dialog);

            EditText etTalentKeywords = findViewById(R.id.et_talent_keywords);
            EditText etMinAge = findViewById(R.id.et_min_age);
            EditText etMaxAge = findViewById(R.id.et_max_age);
            Spinner spGender = findViewById(R.id.sp_gender);
            Button btnSearch = findViewById(R.id.btn_search);
            Button btnCancel = findViewById(R.id.btn_cancel);

            // Feed spinner with data
            ArrayAdapter spinnerAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, genders);
            spGender.setAdapter(spinnerAdapter);

            btnSearch.setOnClickListener(view -> {
                String talentKeywords = etTalentKeywords.getText().toString();
                int minAge = !etMinAge.getText().toString().isEmpty() ? Integer.parseInt(etMinAge.getText().toString().trim()) : 0;
                int maxAge = !etMaxAge.getText().toString().isEmpty() ? Integer.parseInt(etMaxAge.getText().toString().trim()) : 0;
                String gender = spGender.getSelectedItem().toString();

                db.collection("users").addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(context, "Error while fetching data", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }

                    talentedPeople.clear();
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    for (DocumentSnapshot document : value) {
                        User person = document.toObject(User.class);
                        assert firebaseUser != null;
                        if (!person.getId().equals(firebaseUser.getUid()))
                            talentedPeople.add(person);
                    }

                    if (talentedPeople.size() > 0) {

                        if (!talentKeywords.isEmpty())
                            talentedPeople = talentedPeople.stream().filter(p -> p.getTalents().toLowerCase(Locale.ROOT).contains(talentKeywords.toLowerCase(Locale.ROOT))).collect(Collectors.toList());

                        if (minAge != 0)
                            talentedPeople = talentedPeople.stream().filter(p -> p.getAge() >= minAge).collect(Collectors.toList());

                        if (maxAge != 0)
                            talentedPeople = talentedPeople.stream().filter(p -> p.getAge() <= maxAge).collect(Collectors.toList());

                        if (!gender.isEmpty() && !gender.equals("- select gender -"))
                            talentedPeople = talentedPeople.stream().filter(p -> p.getGender().equals(gender)).collect(Collectors.toList());

                        adapter = new MyAdapter(getActivity(), talentedPeople);
                        lvTalentedPeople.setAdapter(adapter);
                    }

                    dismiss();
                });
            });

            btnCancel.setOnClickListener(view -> {
                loadTalentedPeople();
                dismiss();
            });
        }
    }
}