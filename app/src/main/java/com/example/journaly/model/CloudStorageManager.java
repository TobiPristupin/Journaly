package com.example.journaly.model;

import android.app.Activity;
import android.net.Uri;

import com.example.journaly.login.AuthManager;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;

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
    public Single<Uri> upload(File file, Activity activity){
        return Single.create(new SingleOnSubscribe<Uri>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Uri> emitter) throws Throwable {
                StorageReference imageRef = storage.getReference().child("images/" + generateRandomFilename());
                Uri uri = Uri.fromFile(file);
                UploadTask uploadTask = imageRef.putFile(uri);
                uploadTask.addOnFailureListener(activity, e -> emitter.onError(e))
                .addOnSuccessListener(activity, taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uploadUrl -> emitter.onSuccess(uploadUrl));
                });
            }
        });
    }

    private String generateRandomFilename(){
        return AuthManager.getInstance().getLoggedInUserId() + "-" + System.currentTimeMillis();
    }

}
