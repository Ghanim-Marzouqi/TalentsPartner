package com.example.talentspartner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;

import com.example.talentspartner.models.Conversation;
import com.example.talentspartner.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    // Declaration
    Toolbar toolbar;
    ImageView ivAvatar;
    TextView tvName;
    ListView lvConversations;
    List<Conversation> conversations = new ArrayList<>();
    MyAdapter adapter;
    EditText etMessage;
    ImageView ivSend;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize
        toolbar = findViewById(R.id.toolbar);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvName = findViewById(R.id.tv_name);
        lvConversations = findViewById(R.id.lv_conversations);
        etMessage = findViewById(R.id.et_message);
        ivSend = findViewById(R.id.iv_send);

        // Initialize Firebase Authentication & Database
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get partner data
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        User person = (User) bundle.getSerializable("person");

        if (person != null) {
            tvName.setText(person.getName());
            if (person.getImageUrl() != null && !person.getImageUrl().isEmpty()) {
                Picasso.with(ChatActivity.this)
                        .load(person.getImageUrl())
                        .resize(256, 256)
                        .centerCrop()
                        .placeholder(R.drawable.person_placeholder)
                        .into(ivAvatar);
            }
        }

        // Set custom toolbar
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Load conversations
        loadConversations(person);

        // Send message listener
        ivSend.setOnClickListener(view -> {
            FirebaseUser firebaseUser = auth.getCurrentUser();
            String message = etMessage.getText().toString();

            if (message.isEmpty() || firebaseUser == null || person == null)
                return;

            long tsLong = System.currentTimeMillis() / 1000;
            String timestamp = Long.toString(tsLong);

            Map<String, Object> conversation = new HashMap<>();
            conversation.put("messageFrom", firebaseUser.getUid());
            conversation.put("messageTo", person.getId());
            conversation.put("message", message);
            conversation.put("timestamp", timestamp);

            db.collection("conversations").document().set(conversation).addOnCompleteListener(task -> {
                if (!task.isSuccessful())
                    Toast.makeText(ChatActivity.this, "Cannot send message", Toast.LENGTH_SHORT).show();
                else {
                    loadConversations(person);
                    etMessage.setText("");
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

    private void loadConversations(User person) {
        conversations.clear();
        db.collection("conversations").orderBy("timestamp").addSnapshotListener((value, error) -> {
            FirebaseUser firebaseUser = auth.getCurrentUser();
            assert value != null;
            for (QueryDocumentSnapshot document : value) {
                Conversation conversation = document.toObject(Conversation.class);
                assert firebaseUser != null;
                if ((firebaseUser.getUid().equals(conversation.getMessageFrom()) || firebaseUser.getUid().equals(conversation.getMessageTo()))) {
                    assert person != null;
                    if (person.getId().equals(conversation.getMessageFrom()) || person.getId().equals(conversation.getMessageTo())) {
                        conversations.add(conversation);
                    }
                }
            }

            // View list of people
            if (conversations.size() > 0) {
                adapter = new MyAdapter(ChatActivity.this, conversations);
                lvConversations.setAdapter(adapter);
            }
        });
    }

    private class MyAdapter extends BaseAdapter {

        List<Conversation> conversations;
        LayoutInflater inflater;
        Conversation conversation = new Conversation();

        public MyAdapter(Activity activity, List<Conversation> conversations) {
            this.conversations = conversations;
            inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if(conversations.size() <= 0){
                return 1;
            }
            return conversations.size();
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
            public LinearLayoutCompat llConversationCard;
            public TextView tvMessage;
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            @SuppressLint({"ViewHolder", "InflateParams"}) View v = inflater.inflate(R.layout.custom_coversation_card, null);
            MyAdapter.ViewHolder holder = new MyAdapter.ViewHolder();
            FirebaseUser firebaseUser = auth.getCurrentUser();

            if(v != null){
                holder.llConversationCard = v.findViewById(R.id.ll_conversation_card);
                holder.tvMessage = v.findViewById(R.id.tv_message);
            }

            if(conversations.size() > 0 && firebaseUser != null) {
                conversation = conversations.get(i);
                holder.tvMessage.setText(conversation.getMessage());
                if (firebaseUser.getUid().equals(conversation.getMessageFrom())) {
                    holder.tvMessage.setBackground(getResources().getDrawable(R.drawable.incoming_bubble));
                    holder.llConversationCard.setGravity(Gravity.CENTER | Gravity.START);
                } else {
                    holder.tvMessage.setBackground(getResources().getDrawable(R.drawable.outgoing_bubble));
                    holder.llConversationCard.setGravity(Gravity.CENTER | Gravity.END);
                }
            }

            return v;
        }
    }
}