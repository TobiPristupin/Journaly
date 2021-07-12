package com.example.journaly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.journaly.analysis_screen.AnalysisFragment;
import com.example.journaly.databinding.ActivityMainBinding;
import com.example.journaly.home_screen.HomeFragment;
import com.example.journaly.login.LoginActivity;
import com.example.journaly.login.LoginManager;
import com.example.journaly.more_screen.MoreFragment;
import com.example.journaly.settings_screen.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private HomeFragment homeFragment;
    private AnalysisFragment analysisFragment;
    private MoreFragment moreFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!LoginManager.getInstance().isLoggedIn()){
            goToLogin();
        }

        initViews();
    }

    private void initViews() {
        homeFragment = HomeFragment.newInstance();
        analysisFragment = AnalysisFragment.newInstance();
        moreFragment = MoreFragment.newInstance();
        settingsFragment = SettingsFragment.newInstance();

        initBottomNavigation();
    }

    private void initBottomNavigation() {
        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.getMenu().getItem(2).setEnabled(false);

        FragmentManager fm = getSupportFragmentManager();
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.menu_home:
                    fm.beginTransaction().replace(R.id.main_fragment_container_view, homeFragment).commit();
                    break;
                case R.id.menu_analysis:
                    fm.beginTransaction().replace(R.id.main_fragment_container_view, analysisFragment).commit();
                    break;
                case R.id.menu_more:
                    fm.beginTransaction().replace(R.id.main_fragment_container_view, moreFragment).commit();
                    break;
                case R.id.menu_settings:
                    fm.beginTransaction().replace(R.id.main_fragment_container_view, settingsFragment).commit();
                    break;
                default:
                    throw new RuntimeException("Unreachable");

            }

            return true;
        });

        binding.bottomNavigationView.setSelectedItemId(R.id.menu_home);
    }

    private void goToLogin(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        //Flags prevent user from returning to MainActivityView when pressing back button
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}