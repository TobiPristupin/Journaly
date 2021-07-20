package com.example.journaly.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.journaly.MainActivity;
import com.example.journaly.databinding.ActivityLoginBinding;
import com.example.journaly.model.FirebaseUsersRepository;
import com.example.journaly.model.UsersRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import org.parceler.Repository;

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

            if (!AuthManager.fieldsAreValid(username, password)){
                Toasty.error(this, "Invalid credentials", Toast.LENGTH_SHORT, true).show();
                return;
            }

            AuthManager.getInstance().signup(username, password, task -> {
                if (task.isSuccessful()){
                    Log.d(TAG, "Successfully signed up");
                    Toasty.success(this, "Successfully signed up!", Toast.LENGTH_SHORT, true).show();
                    UsersRepository usersRepository = FirebaseUsersRepository.getInstance();
                    FirebaseUser user = task.getResult().getUser();
                    String photoUrl = user.getPhotoUrl() == null ? null : user.getPhotoUrl().toString();
                    usersRepository.createNewUser(user.getUid(), user.getEmail(), photoUrl);
                } else {
                    Toasty.error(this, "Could not sign up. Please try again", Toast.LENGTH_SHORT, true).show();
                    Log.w(TAG, task.getException());
                }
            }, this);
        });
    }

    private void initLogin() {
        binding.loginButton.setOnClickListener(v -> {
            String username = binding.loginUsernameEdittext.getText().toString();
            String pwdGuess = binding.loginPasswordEdittext.getText().toString();

            if (!AuthManager.fieldsAreValid(username, pwdGuess)){
                Toasty.error(this, "Invalid credentials", Toast.LENGTH_SHORT, true).show();
                return;
            }

            AuthManager.getInstance().login(username, pwdGuess, (OnCompleteListener<AuthResult>) task -> {
                if (task.isSuccessful()){
                    Log.d(TAG, "Successfully logged in");
                    goToMainActivity();
                } else {
                    Toasty.error(this, "Could not login. Please try again", Toast.LENGTH_SHORT, true).show();
                    Log.w(TAG, task.getException());
                }
            }, this);
        });
    }

    private void goToMainActivity(){
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }


}