package com.example.journaly.settings_screen;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.journaly.R;
import com.example.journaly.login.LoginActivity;
import com.example.journaly.login.AuthManager;

import org.jetbrains.annotations.NotNull;


public class SettingsFragment extends PreferenceFragmentCompat {

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
        System.out.println("created");
        initPreferences();
        return root;
    }

    private void initPreferences() {
        findPreference("preference_logout").setOnPreferenceClickListener(preference -> {
            AuthManager.getInstance().logout();
            goToLoginScreen();
            return true;
        });
    }

    private void goToLoginScreen() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        //Flags prevent user from returning to MainActivityView when pressing back button
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}