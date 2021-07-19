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
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.example.journaly.R;
import com.example.journaly.databinding.FragmentProfileBinding;
import com.example.journaly.login.AuthManager;
import com.example.journaly.model.FirebaseUsersRepository;
import com.example.journaly.model.User;
import com.example.journaly.model.UsersRepository;
import com.example.journaly.settings_screen.SettingsActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import io.reactivex.rxjava3.functions.Consumer;

public class ProfileFragment extends Fragment {

    private static final String USER_PARAM = "user";
    private static final String ENABLE_BACK_PARAM = "enableBack";

    private FragmentProfileBinding binding;
    private String userId;
    /*
    If this fragment is accessed through bottom navigation bar, we should not have a back button in toolbar.
    If this fragment is accessed by clicking on a user's info, we should have a back button in toolbar.
    */
    private boolean enableBackButton = false;
    private User user;
    private UsersRepository usersRepository;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String userId, boolean enableToolbarBackButton) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(USER_PARAM, userId);
        args.putBoolean(ENABLE_BACK_PARAM, enableToolbarBackButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getArguments().getString(USER_PARAM);
        enableBackButton = getArguments().getBoolean(ENABLE_BACK_PARAM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        usersRepository = FirebaseUsersRepository.getInstance();
        usersRepository.userFromId(userId).subscribe(user -> {
            ProfileFragment.this.user = user;
            initViews();
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOptionsMenu();
    }

    private void initOptionsMenu() {
        if (enableBackButton){
            binding.profileToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
            binding.profileToolbar.setNavigationOnClickListener(v -> {
                getActivity().finish();
            });
        }

        //can only access settings if logged in as current user
        if (AuthManager.getInstance().getLoggedInUserId().equals(userId)){
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