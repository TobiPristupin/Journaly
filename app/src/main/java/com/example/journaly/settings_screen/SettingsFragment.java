package com.example.journaly.settings_screen;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.journaly.R;
import com.example.journaly.login.LoginActivity;
import com.example.journaly.login.AuthManager;
import com.example.journaly.model.cloud_storage.CloudStorageManager;
import com.example.journaly.model.users.FirebaseUsersRepository;
import com.example.journaly.model.users.UsersRepository;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import io.reactivex.rxjava3.functions.Consumer;

import static android.app.Activity.RESULT_OK;


public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int SELECT_IMAGE_REQUEST_CODE = 123;
    private UsersRepository usersRepository;

    public SettingsFragment(){

    }

    public static SettingsFragment newInstance(){
        return new SettingsFragment();
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_menu, rootKey);
    }

    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        usersRepository = FirebaseUsersRepository.getInstance();
        initPreferences();
        return root;
    }

    private void initPreferences() {
        findPreference("preference_logout").setOnPreferenceClickListener(preference -> {
            AuthManager.getInstance().logout();
            goToLoginScreen();
            return true;
        });

        findPreference("change_pfp").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openImagePicker();
                return true;
            }
        });
    }

    private void openImagePicker() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_IMAGE_REQUEST_CODE);
    }

    private void goToLoginScreen() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        //Flags prevent user from returning to MainActivityView when pressing back button
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void updateProfilePicture(Uri uri) {
        CloudStorageManager cloudStorageManager = CloudStorageManager.getInstance();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        cloudStorageManager.upload(bitmap, getActivity()).subscribe(uploadedUrl -> {
            usersRepository.updateProfilePicture(uploadedUrl.toString()).subscribe();
        });

//
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        // Compress image to lower quality scale 1 - 100
//        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
//        byte[] bytes = stream.toByteArray();
//        ParseFile parseFile = new ParseFile(bytes);
//        ParseUser user = ParseUser.getCurrentUser();
//        user.put("profile_picture", parseFile);
//        user.saveInBackground();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_IMAGE_REQUEST_CODE) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                updateProfilePicture(selectedImage);
            }
        }
    }
}