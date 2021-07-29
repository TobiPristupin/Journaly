package com.example.journaly.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.journaly.MainActivity;
import com.example.journaly.databinding.ActivityLoginBinding;
import com.example.journaly.model.users.FirebaseUsersRepository;
import com.example.journaly.model.users.UsersRepository;
import com.google.firebase.auth.FirebaseUser;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
    }

    private void initViews() {
        initLogin();
        initSignup();
    }

    private void initSignup() {
        binding.signupButton.setOnClickListener(v -> {
            String username = binding.loginUsernameEdittext.getText().toString();
            String password = binding.loginPasswordEdittext.getText().toString();

            if (!AuthManager.fieldsAreValid(username, password)) {
                Toasty.error(this, "Invalid credentials", Toast.LENGTH_SHORT, true).show();
                return;
            }

            AuthManager.getInstance().signup(username, password, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Successfully signed up");
                    Toasty.success(this, "Successfully signed up!", Toast.LENGTH_SHORT, true).show();
                    createUserInDatabase(username, task);
                } else {
                    Toasty.error(this, "Could not sign up. Please try again", Toast.LENGTH_SHORT, true).show();
                    Log.w(TAG, task.getException());
                }
            }, this);
        });
    }

    private void createUserInDatabase(String username, com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> task) {
        UsersRepository usersRepository = FirebaseUsersRepository.getInstance();
        FirebaseUser user = task.getResult().getUser();
        usersRepository.createNewUser(user.getUid(), user.getEmail(), null);
    }

    private void initLogin() {
        binding.loginButton.setOnClickListener(v -> {
            String username = binding.loginUsernameEdittext.getText().toString();
            String pwdGuess = binding.loginPasswordEdittext.getText().toString();

            if (!AuthManager.fieldsAreValid(username, pwdGuess)) {
                Toasty.error(this, "Invalid credentials", Toast.LENGTH_SHORT, true).show();
                return;
            }

            AuthManager.getInstance().login(username, pwdGuess, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Successfully logged in");
                    goToMainActivity();
                } else {
                    Toasty.error(this, "Could not login. Please try again", Toast.LENGTH_SHORT, true).show();
                    Log.w(TAG, task.getException());
                }
            }, this);
        });
    }

    private void goToMainActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }


}