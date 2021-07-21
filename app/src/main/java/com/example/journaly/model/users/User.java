package com.example.journaly.model.users;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/*
Wrapper for FirebaseUser class. Why are we creating a wrapper instead of using FirebaseUser directly?
because firebase has no functionality for fetching a random user given their uid. The only user
the code can access is the currently logged in user. We need a way to fetch any user given their uid,
so we create our own 'users/' path in the database. This is the model for the data object we store in that path.
*/
@Parcel(Parcel.Serialization.BEAN)
public class User {

    private String uid;
    @Nullable
    private String photoUri;
    private String email;
    @Nullable private String bio;
    @Nullable private String contactInfo;
    @Nullable private Map<String, String> followers;
    @Nullable private Map<String, String> following;

    public User(){
        //empty constructor for firebase
    }

    public User(String uid,
                @org.jetbrains.annotations.Nullable String photoUri,
                String email,
                @org.jetbrains.annotations.Nullable String bio,
                @org.jetbrains.annotations.Nullable String contactInfo,
                @org.jetbrains.annotations.Nullable Map<String, String> followers,
                @org.jetbrains.annotations.Nullable Map<String, String> following) {
        this.uid = uid;
        this.photoUri = photoUri;
        this.email = email;
        this.bio = bio;
        this.contactInfo = contactInfo;
        this.followers = followers;
        this.following = following;
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

    public @org.jetbrains.annotations.Nullable String getBio() {
        return bio;
    }

    public void setBio(@org.jetbrains.annotations.Nullable String bio) {
        this.bio = bio;
    }

    public @org.jetbrains.annotations.Nullable String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(@org.jetbrains.annotations.Nullable String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public @org.jetbrains.annotations.Nullable Map<String, String> getFollowers() {
        return followers;
    }

    public @org.jetbrains.annotations.Nullable List<String> getFollowersAsList() {
        if (followers == null){
            return new ArrayList<>();
        }
        return new ArrayList<>(followers.values());
    }

    public void setFollowers(@org.jetbrains.annotations.Nullable Map<String, String> followers) {
        this.followers = followers;
    }

    @Nullable
    public Map<String, String> getFollowing() {
        return following;
    }

    public @org.jetbrains.annotations.Nullable List<String> getFollowingAsList() {
        if (following == null){
            return new ArrayList<>();
        }
        return new ArrayList<>(following.values());
    }

    public void setFollowing(@org.jetbrains.annotations.Nullable Map<String, String> following) {
        this.following = following;
    }
}
