package com.example.talentspartner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.talentspartner.models.User;
import com.squareup.picasso.Picasso;

public class PartnerDetailsActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvPhone, tvGender, tvAge, tvTalents;
    ImageView ivAvatar;
    Button btnUpdate;

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
        } else {
            btnUpdate.setVisibility(View.GONE);
        }

        btnUpdate.setOnClickListener(view -> {
            // TODO: Implement friendship mechanism
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