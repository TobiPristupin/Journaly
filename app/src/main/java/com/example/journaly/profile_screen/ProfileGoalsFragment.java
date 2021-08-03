package com.example.journaly.profile_screen;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.fragment.app.Fragment;

import com.example.journaly.databinding.FragmentProfileGoalsBinding;
import com.example.journaly.goals_screen.CreateGoalActivity;
import com.example.journaly.model.users.User;
import com.example.journaly.utils.AnimationUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProfileGoalsFragment extends Fragment {

    private FragmentProfileGoalsBinding binding;


    public ProfileGoalsFragment() {
        // Required empty public constructor
    }

    public static ProfileGoalsFragment newInstance(User user) {
        return new ProfileGoalsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileGoalsBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

    private void initViews() {
        AnimationUtils.fadeIn(500, 500, binding.noGoalImage, binding.noGoalButton, binding.noGoalText);

        binding.noGoalButton.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), CreateGoalActivity.class);
            startActivity(i);
        });
    }

}