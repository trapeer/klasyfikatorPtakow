package com.example.klasyfikatorptakow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;

import android.content.SharedPreferences;

import android.os.Bundle;

import android.widget.Button;
import android.widget.TextView;



public class Photo_trapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_trap);

        final ViewPager viewPager = findViewById(R.id.viewPager);
        if (viewPager != null)
            viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
    }
}