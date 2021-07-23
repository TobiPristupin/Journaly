package com.example.journaly.model.users;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;

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
    @Nullable
    private String bio;
    @Nullable
    private String contactInfo;
    @Nullable
    private Map<String, String> followers;
    @Nullable
    private Map<String, String> following;

    //Threshold used to determine if a user is "in need".
    private double negativityThreshold;

    @Exclude
    //Threshold used to determine if a user can have their negativity threshold increased
    private final double positivityThreshold = 5;

    //Default negativity threshold that all users start with.
    @Exclude
    public static final double DEFAULT_NEGATIVITY_THRESHOLD = -3.5;

    /*
    To avoid double counting an entry when determining if a user is in need, we keep track of the
    id of the last analyzed entry. A more detailed explanation of the algorithm used to determine if
    a user is in need can be found in UserInNeedUtils.java
    */
    private String idOfLastJournalEntryAnalyzed;

    //is this user currently in need
    private boolean isInNeed;

    public User() {
        //empty constructor for firebase
    }

    public User(String uid,
                @Nullable String photoUri,
                String email,
                @Nullable String bio,
                @Nullable String contactInfo,
                @Nullable Map<String, String> followers,
                @Nullable Map<String, String> following,
                double negativityThreshold,
                String idOfLastJournalEntryAnalyzed,
                boolean isInNeed) {
        this.uid = uid;
        this.photoUri = photoUri;
        this.email = email;
        this.bio = bio;
        this.contactInfo = contactInfo;
        this.followers = followers;
        this.following = following;
        this.negativityThreshold = negativityThreshold;
        this.idOfLastJournalEntryAnalyzed = idOfLastJournalEntryAnalyzed;
        this.isInNeed = isInNeed;
    }

    public static User withDefaultValues(String uid, @Nullable String photoUri, String email){
        return new User(uid, photoUri, email, null, null, null, null, User.DEFAULT_NEGATIVITY_THRESHOLD, "", false);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public @Nullable String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(@Nullable String photoUri) {
        this.photoUri = photoUri;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        //emails are of the form username@journaly.com, so we must extract username
        return this.email.split("@")[0];
    }

    public @Nullable String getBio() {
        return bio;
    }

    public void setBio(@Nullable String bio) {
        this.bio = bio;
    }

    public @Nullable String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(@Nullable String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public @Nullable Map<String, String> getFollowers() {
        return followers;
    }

    public @Nullable List<String> getFollowersAsList() {
        if (followers == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(followers.values());
    }

    public void setFollowers(@Nullable Map<String, String> followers) {
        this.followers = followers;
    }

    @Nullable
    public Map<String, String> getFollowing() {
        return following;
    }

    public @Nullable List<String> getFollowingAsList() {
        if (following == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(following.values());
    }

    public void setFollowing(@Nullable Map<String, String> following) {
        this.following = following;
    }

    public double getNegativityThreshold() {
        return negativityThreshold;
    }

    public void setNegativityThreshold(double negativityThreshold) {
        this.negativityThreshold = negativityThreshold;
    }

    public String getIdOfLastJournalEntryAnalyzed() {
        return idOfLastJournalEntryAnalyzed;
    }

    public void setIdOfLastJournalEntryAnalyzed(String idOfLastJournalEntryAnalyzed) {
        this.idOfLastJournalEntryAnalyzed = idOfLastJournalEntryAnalyzed;
    }

    public boolean isInNeed() {
        return isInNeed;
    }

    public void setInNeed(boolean inNeed) {
        isInNeed = inNeed;
    }

    @Exclude
    public double getPositivityThreshold() {
        return positivityThreshold;
    }
}
