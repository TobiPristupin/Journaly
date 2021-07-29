package com.example.journaly.profile_screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.journaly.R;
import com.example.journaly.databinding.FragmentProfileBinding;
import com.example.journaly.login.AuthManager;
import com.example.journaly.model.avatar.AvatarApiClient;
import com.example.journaly.model.users.FirebaseUsersRepository;
import com.example.journaly.model.users.User;
import com.example.journaly.model.users.UsersRepository;
import com.example.journaly.settings_screen.SettingsActivity;
import com.example.journaly.utils.AnimationUtils;
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";
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
    private CompositeDisposable disposables;
    private final boolean viewsInitialized = false;

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
        disposables = new CompositeDisposable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        subscribeToUserData();

        return binding.getRoot();
    }

    private void subscribeToUserData() {
        usersRepository = FirebaseUsersRepository.getInstance();
        Disposable subscription = usersRepository.fetchUserFromId(userId).subscribe(user -> {
            //This is an observable stream. Everytime user corresponding to userId is modified in
            //database, this code will run
            ProfileFragment.this.user = user;
            initViewsDependentOnUser();
        }, throwable -> {
            Log.w(TAG, throwable);
        });
        disposables.add(subscription);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOptionsMenu();
    }

    private void initOptionsMenu() {
        if (enableBackButton) {
            binding.profileToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
            binding.profileToolbar.setNavigationOnClickListener(v -> {
                getActivity().finish();
            });
        }

        //can only access settings if logged in as current user
        if (AuthManager.getInstance().getLoggedInUserId().equals(userId)) {
            binding.profileToolbar.inflateMenu(R.menu.profile_toolbar_menu);
            binding.profileToolbar.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_settings:
                        Intent i = new Intent(getContext(), SettingsActivity.class);
                        startActivity(i);
                        return true;
                }

                return false;
            });
        }
    }

    private void initViewsDependentOnUser() {
        if (user.getPhotoUri() != null) {
            Glide.with(this).load(user.getPhotoUri()).fallback(R.drawable.default_profile).into(binding.userPfp);
        } else {
            GlideToVectorYou.justLoadImage(getActivity(), AvatarApiClient.generateAvatarUri(user.getDisplayName()), binding.userPfp);
        }
        binding.profileUsername.setText(user.getDisplayName());
        initFollowFunctionality();

        initTabs();
    }

    private void initTabs() {
        binding.profileViewpager.setSaveEnabled(false);

        binding.profileViewpager.setAdapter(new ProfileSectionAdapter(this, user));
        new TabLayoutMediator(binding.profileTabs, binding.profileViewpager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Profile");
                    break;
                case 1:
                    tab.setText("Journals");
                    break;
                case 2:
                    tab.setText("Goals");
                    break;
            }
        }).attach();
    }

    private void initFollowFunctionality() {
        List<String> followers = user.getFollowersAsList();
        List<String> following = user.getFollowingAsList();
        int followerCount = followers == null ? 0 : followers.size();
        int followIngCount = following == null ? 0 : following.size();
        binding.followersCount.setText(String.valueOf(followerCount));
        binding.followingCount.setText(String.valueOf(followIngCount));

        String loggedInId = AuthManager.getInstance().getLoggedInUserId();
        if (!user.getUid().equals(loggedInId)) {
            AnimationUtils.fadeIn(500, 200, binding.followButton);
            //logged in user is viewing another user
            if (followers != null && followers.contains(loggedInId)) { //already following
                binding.followButton.setBackgroundColor(getResources().getColor(R.color.unfollow_red));
                binding.followButton.setText("Unfollow");
            } else {
                binding.followButton.setBackgroundColor(getResources().getColor(R.color.follow_green));
                binding.followButton.setText("Follow");
            }
        }

        binding.followButton.setOnClickListener(v -> {
            if (binding.followButton.getText().equals("Follow")) {
                usersRepository.follow(user.getUid()).subscribe(() -> {
                    Log.i(TAG, "Successfully followed");
                    initFollowFunctionality(); //refresh all data
                }, throwable -> {
                    Log.w(TAG, throwable);
                });
            } else {
                usersRepository.unfollow(user.getUid()).subscribe(() -> {
                    Log.i(TAG, "Successfully unfollowed");
                    initFollowFunctionality();
                }, throwable -> {
                    Log.w(TAG, throwable);
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }
}