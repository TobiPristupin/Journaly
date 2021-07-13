package com.example.journaly.model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;

import com.example.journaly.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class CloudStorageManager {

    private static CloudStorageManager instance = null;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private CloudStorageManager(){
        //private empty constructor for singleton pattern
    }

    public static CloudStorageManager getInstance(){
        if (instance == null){
            instance = new CloudStorageManager();
        }

        return instance;
    }

    //The activity parameter is passed to the firebase API to handle removing the listener when the activity dies
    public void upload(File file, String filename, OnFailureListener onFailure, OnCompleteListener<UploadTask.TaskSnapshot> onComplete, Activity activity){
        StorageReference imageRef = storage.getReference().child("images/" + filename);
        Uri uri = Uri.fromFile(file);
        UploadTask uploadTask = imageRef.putFile(uri);
        uploadTask.addOnFailureListener(activity, onFailure).addOnCompleteListener(onComplete);
    }

}
