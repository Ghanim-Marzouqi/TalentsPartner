package com.example.talentspartner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // go to intro slider page
        new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, IntroSliderActivity.class)), 3000);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }
}