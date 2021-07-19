package com.example.journaly.login;

import android.app.Activity;

import com.example.journaly.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;

//Contains all firebase related information for logging in/out, creating users, and getting current user
public class AuthManager {

    private static AuthManager instance = null;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private AuthManager(){
        //private empty constructor for singleton pattern
    }

    public static AuthManager getInstance(){
        if (instance == null){
            instance = new AuthManager();
        }

        return instance;
    }

    public boolean isLoggedIn(){
        return firebaseAuth.getCurrentUser() != null;
    }

    //fetching the current user id can be done quickly and synchronosuly, so we provide a helper function here
    //since this is an operation done very often. To fetch other information from the user, one must use
    //getCurrentUser which makes a call to the database.
    public String getLoggedInUserId(){
        return firebaseAuth.getCurrentUser().getUid();
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
