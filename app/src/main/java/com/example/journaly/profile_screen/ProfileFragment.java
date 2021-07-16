package com.example.journaly.profile_screen;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.journaly.R;
import com.example.journaly.databinding.FragmentProfileBinding;
import com.example.journaly.model.User;
import com.example.journaly.settings_screen.SettingsActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

public class ProfileFragment extends Fragment {

    private static final String USER_PARAM = "param1";

    private FragmentProfileBinding binding;
    private User user;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(User user) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable(USER_PARAM, Parcels.wrap(user));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = Parcels.unwrap(getArguments().getParcelable(USER_PARAM));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment

        initViews();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOptionsMenu();
    }

    private void initOptionsMenu() {
        binding.profileToolbar.inflateMenu(R.menu.profile_toolbar_menu);
        binding.profileToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.action_settings:
                    Intent i = new Intent(getContext(), SettingsActivity.class);
                    startActivity(i);
                    return true;
            }

            return false;
        });
    }

    private void initViews(){
        Glide.with(this).load(user.getPhotoUri()).fallback(R.drawable.default_profile).into(binding.userPfp);
        binding.profileUsername.setText(user.getDisplayName());
        initTabs();
    }

    private void initTabs() {
        binding.profileViewpager.setSaveEnabled(false);

        binding.profileViewpager.setAdapter(new ProfileSectionAdapter(this, user));
        new TabLayoutMediator(binding.profileTabs, binding.profileViewpager, (tab, position) -> {
            switch (position){
                case 0:
                    tab.setText("Profile");
                    break;
                case 1:
                    tab.setText("Journals");
                    break;
            }
        }).attach();
    }
}