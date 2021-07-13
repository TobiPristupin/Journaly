package com.example.journaly.model;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;



/*
Wrapper for FirebaseUser class. Why are we creating a wrapper instead of using FirebaseUser directly?
because firebase has no functionality for fetching a random user given their uid. The only user
the code can access is the currently logged in user. We need a way to fetch any user given their uid,
so we create our own 'users/' path in the database. This is the model for that path.
*/
public class User {

    private String uid;
    private Uri photoUri;
    private String email;
    private String displayName;

    public User(){
        //empty constructor for firebase
    }

    public User(String uid, Uri photoUri, String email, String displayName) {
        this.uid = uid;
        this.photoUri = photoUri;
        this.email = email;
        this.displayName = displayName;
    }

    public User(FirebaseUser user){
        this(user.getUid(), user.getPhotoUrl(), user.getEmail(), user.getDisplayName());
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
