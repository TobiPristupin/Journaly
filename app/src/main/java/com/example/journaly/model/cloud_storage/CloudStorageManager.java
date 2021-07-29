package com.example.journaly.model.cloud_storage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;

import com.example.journaly.login.AuthManager;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

import io.reactivex.rxjava3.core.Single;

public class CloudStorageManager {

    private static CloudStorageManager instance = null;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private CloudStorageManager() {
        //private empty constructor for singleton pattern
    }

    public static CloudStorageManager getInstance() {
        if (instance == null) {
            instance = new CloudStorageManager();
        }

        return instance;
    }

    //The activity parameter is passed to the firebase API to handle removing the listener when the activity dies
    public Single<Uri> upload(File file, Activity activity) {
        return Single.create(emitter -> {
            StorageReference imageRef = storage.getReference().child("images/" + generateRandomFilename());
            Uri uri = Uri.fromFile(file);
            UploadTask uploadTask = imageRef.putFile(uri);
            uploadTask.addOnFailureListener(activity, e -> emitter.onError(e))
                    .addOnSuccessListener(activity, taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uploadUrl -> emitter.onSuccess(uploadUrl));
                    });
        });
    }

    //The activity parameter is passed to the firebase API to handle removing the listener when the activity dies
    public Single<Uri> upload(Bitmap bitmap, Activity activity) {
        return Single.create(emitter -> {
            StorageReference imageRef = storage.getReference().child("images/" + generateRandomFilename());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.
                    addOnFailureListener(activity, e -> emitter.onError(e))
                    .addOnSuccessListener(activity, taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uploadUrl -> emitter.onSuccess(uploadUrl));
                    });
        });
    }

    private String generateRandomFilename() {
        return AuthManager.getInstance().getLoggedInUserId() + "-" + System.currentTimeMillis();
    }

}
