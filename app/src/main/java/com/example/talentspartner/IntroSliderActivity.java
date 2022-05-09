package com.example.talentspartner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IntroSliderActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private IntroManager introManager;
    private ViewPagerAdapter viewPagerAdapter;
    private TextView[] dots;
    private TextView next, skip;
    private LinearLayout dotsLayout;
    private int[] layouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_TalentsPartner_NoActionBar);
        setContentView(R.layout.activity_intro_slider);

        introManager = new IntroManager(this);

        if(!introManager.Check())
        {
            introManager.setFirst(false);
            Intent i = new Intent(IntroSliderActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_intro_slider);

        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        skip= findViewById(R.id.tv_skip);
        next = findViewById(R.id.tv_next);
        layouts = new int[]{R.layout.slider_1, R.layout.slider_2, R.layout.slider_3};
        addBottomDots(0);
        changeStatusBarColor();
        viewPagerAdapter = new ViewPagerAdapter();
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(viewListener);

        skip.setOnClickListener(view -> {
            Intent i = new Intent(IntroSliderActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        });

        next.setOnClickListener(view -> {
            int current = getItem(+1);
            if(current<layouts.length)
            {
                viewPager.setCurrentItem(current);
            }
            else
            {
                Intent i = new Intent(IntroSliderActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void addBottomDots(int position)
    {
        dots = new TextView[layouts.length];
        int colorActive = getResources().getColor(R.color.primary);
        int colorInactive = getResources().getColor(R.color.secondary);
        dotsLayout.removeAllViews();
        for(int i = 0; i<dots.length; i++)
        {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorInactive);
            dotsLayout.addView(dots[i]);
        }
        if(dots.length > 0)
            dots[position].setTextColor(colorActive);
    }

    private int getItem(int i)
    {
        return viewPager.getCurrentItem() + i;
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener()
    {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            addBottomDots(position);

            if(position==layouts.length-1)
            {
                next.setText("PROCEED");
                skip.setVisibility(View.GONE);
            }
            else
            {
                next.setText("NEXT");
                skip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void changeStatusBarColor()
    {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        ((Window) window).setStatusBarColor(Color.TRANSPARENT);
    }

    class ViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = layoutInflater.inflate(layouts[position], container,false);
            container.addView(v);
            return v;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int mPosition, @NonNull Object object) {
            View v = (View) object;
            container.removeView(v);
        }
    }
}