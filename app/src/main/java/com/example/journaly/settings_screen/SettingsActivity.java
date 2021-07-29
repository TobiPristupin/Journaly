package com.example.journaly.settings_screen;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.journaly.databinding.ActivitySettingsBinding;

/*
This class is nothing more than a wrapper for SettingsFragment. Sometimes it is nice to have an
activity instead of a fragment since an activity can handle back navigation and does not require
a fragment container nor a complicated fragment transaction, only an intent. Fragment is inflated in xml
layout.
*/
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
    }

    private void initToolbar() {
        setSupportActionBar(binding.settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}