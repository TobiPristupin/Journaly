package com.example.journaly.model;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;



/*
Wrapper for FirebaseUser class. Why are we creating a wrapper instead of using FirebaseUser directly?
because firebase has no functionality for fetching a random user given their uid. The only user
the code can access is the currently logged in user. We need a way to fetch any user given their uid,
so we create our own 'users/' path in the database. This is the model for the data object we store in that path.
*/
public class User {

    private String uid;
    @Nullable
    private String photoUri;
    private String email;

    public User(){
        //empty constructor for firebase
    }

    public User(String uid, String photoUri, String email) {
        this.uid = uid;
        this.photoUri = photoUri;
        this.email = email;
    }

    public User(FirebaseUser user){
        this(user.getUid(), null, user.getEmail());
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public @org.jetbrains.annotations.Nullable String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(@org.jetbrains.annotations.Nullable String photoUri) {
        this.photoUri = photoUri;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName(){
        //emails are of the form username@journaly.com, so we must extract username
        return this.email.split("@")[0];
    }

}
