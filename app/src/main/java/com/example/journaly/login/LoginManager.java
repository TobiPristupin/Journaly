package com.example.journaly.login;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.example.journaly.model.FirebaseRepository;
import com.example.journaly.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

//Contains all firebase related information for logging in/out, creating users, and getting current user
public class LoginManager {

    private static LoginManager instance = null;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private LoginManager(){
        //private empty constructor for singleton pattern
    }

    public static LoginManager getInstance(){
        if (instance == null){
            instance = new LoginManager();
        }

        return instance;
    }

    public boolean isLoggedIn(){
        return firebaseAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser(){
        return firebaseAuth.getCurrentUser();
    }

    /*
    The activity parameter is passed to the firebase API to handle removing the listener when the activity dies
    NOTE: This method assumes username and password are of a valid format. Caller should check validity beforehand by using
    fieldsAreValid(). Ideally field checking would be merged into the callback response, but I would have to write
    my own callback wrapper and I don't feel like.
    */
    public void signup(String username, String password, OnCompleteListener<AuthResult> onCompleteListener, Activity activity){
        String email = constructEmailFromUsername(username);
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, onCompleteListener);
    }

    /*
    The activity parameter is passed to the firebase API to handle removing the listener when the activity dies
    NOTE: This method assumes username and password are of a valid format. Caller should check validity beforehand by using
    fieldsAreValid(). Ideally field checking would be merged into the callback response, but I would have to write
    my own callback wrapper and I don't feel like.
    */
    public void login(String username, String pwdGuess, OnCompleteListener<AuthResult> onCompleteListener, Activity activity){
        String email = constructEmailFromUsername(username);
        firebaseAuth.signInWithEmailAndPassword(email, pwdGuess).addOnCompleteListener(activity, onCompleteListener);
    }

    public void logout(){
        firebaseAuth.signOut();
    }

    private String constructEmailFromUsername(String username){
        /*
        firebase does not allow login with username and no email, so we're constructing a fake email
        here
        */
        return username + "@journaly.com";
    }

    public static boolean fieldsAreValid(String username, String password){
        if (username == null || password == null || username.length() == 0 || password.length() == 0){
            return false;
        }

        if (!isAlphanumeric(username) || !isAlphanumeric(password)){
            return false;
        }

        if (password.length() < 6){
            return false;
        }

        return true;
    }

    private static boolean isAlphanumeric(String str) {
        char[] charArray = str.toCharArray();
        for(char c:charArray)
        {
            if (!Character.isLetterOrDigit(c))
                return false;
        }
        return true;
    }


}
