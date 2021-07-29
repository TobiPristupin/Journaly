package com.example.journaly.profile_screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.journaly.R;

public class ProfileGoalsFragment extends Fragment {


    public ProfileGoalsFragment() {
        // Required empty public constructor
    }

    public static ProfileGoalsFragment newInstance(String param1, String param2) {
        return new ProfileGoalsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_goals, container, false);
    }
}