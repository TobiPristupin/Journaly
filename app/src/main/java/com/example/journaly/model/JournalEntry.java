package com.example.journaly.model;

import android.net.Uri;

import androidx.annotation.Nullable;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel(Parcel.Serialization.BEAN)
public class JournalEntry {

    //Using int constants instead of an enum since firebase cannot serialize enums
    public static final int POSITIVE_MOOD = 1;
    public static final int NEUTRAL_MOOD = 0;
    public static final int NEGATIVE_MOOD = -1;

    private String title;
    private String text;
    private String id;
    private long date;
    private boolean isPublic;
    private boolean containsImage;
    private int mood;
    private String userId;
    @Nullable
    private String imageUri;

    public JournalEntry(){
        //Empty constructor required for firebase
    }

    public JournalEntry(String title, String text, long date, boolean isPublic, int mood, String userId, boolean containsImage, @org.jetbrains.annotations.Nullable String imageUri) {
        this.title = title;
        this.text = text;
        this.id = null; //no id since it is generated afterwards by the database
        this.date = date;
        this.isPublic = isPublic;
        this.mood = mood;
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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public int getMood() {
        return mood;
    }

    public void setMood(int mood) {
        this.mood = mood;
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
}
