package com.example.journaly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.journaly.databinding.ActivityMainBinding;
import com.example.journaly.login.LoginActivity;
import com.example.journaly.login.LoginManager;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!LoginManager.getInstance().isLoggedIn()){
            goToLogin();
        }

        binding.button.setOnClickListener(v -> {
            LoginManager.getInstance().logout();
            goToLogin();
        });

    }

    private void goToLogin(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        //Flags prevent user from returning to MainActivityView when pressing back button
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}