package com.example.journaly.model.journals;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class JournalEntry implements Comparable<JournalEntry> {

    private String title;
    private String text;
    private String id;
    private long createdAt;
    private boolean isPublic;
    private boolean containsImage;
    private String userId;
    @Nullable
    private String imageUri;

    /*
    range of [-inf, inf].
     Not to be confused with Mood, which maps a sentiment to one of three categories (NEGATIVE. NEUTRAL, POSITIVE)
    */
    private double sentiment;

    public JournalEntry() {
        //Empty constructor required for firebase
    }

    public JournalEntry(String title, String text, long createdAt, boolean isPublic, double sentiment, String userId, boolean containsImage, @org.jetbrains.annotations.Nullable String imageUri) {
        this.title = title;
        this.text = text;
        this.id = null; //no id since it is generated afterwards by the database
        this.createdAt = createdAt;
        this.isPublic = isPublic;
        this.sentiment = sentiment;
        this.containsImage = containsImage;
        this.userId = userId;
        this.imageUri = imageUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public double getSentiment() {
        return sentiment;
    }

    public void setSentiment(double sentiment) {
        this.sentiment = sentiment;
    }

    @Exclude
    /*
    don't store an enum in database since it causes issues with serialization.
    there is no need to store mood anyways since mood is derived from sentiment
    */
    public Mood getMood() {
        return Mood.fromSentiment(this.sentiment);
    }

    public boolean getContainsImage() {
        return containsImage;
    }

    public void setContainsImage(boolean containsImage) {
        this.containsImage = containsImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public @org.jetbrains.annotations.Nullable String getImageUri() {
        return imageUri;
    }

    public void setImageUri(@org.jetbrains.annotations.Nullable String imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    public int compareTo(JournalEntry o) {
        //bigger date timestamp -> newer post -> considered smaller
        return (int) (o.createdAt - this.createdAt);
    }
}
