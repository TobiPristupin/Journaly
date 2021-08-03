package com.example.journaly.profile_screen;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.journaly.common.JournalsListViewerFragment;
import com.example.journaly.login.AuthManager;
import com.example.journaly.model.users.User;

import org.jetbrains.annotations.NotNull;

public class ProfileSectionAdapter extends FragmentStateAdapter {

    private final User user;
    //True if {user} is the currently logged in user on this device
    private boolean isUserLoggedIn;

    public ProfileSectionAdapter(@NonNull @NotNull Fragment fragment, User user) {
        super(fragment);
        this.user = user;
        this.isUserLoggedIn = user.getUid().equals(AuthManager.getInstance().getLoggedInUserId());
    }


    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ProfileAboutFragment.newInstance(user);
            case 1:
                JournalsListViewerFragment.Mode profileMode = JournalsListViewerFragment.Mode.USER_PROFILE;
                return JournalsListViewerFragment.newInstance(profileMode, user.getUid());
            case 2:
                assert isUserLoggedIn; //This case should never run if the user we're viewing is not the one currently logged in
                return ProfileGoalsFragment.newInstance(user);
        }

        throw new RuntimeException("Unreachable");
    }

    @Override
    public int getItemCount() {
        if (isUserLoggedIn){
            return 3; //include the "Goals" tab
        }

        return 2; //Do not include the "Goal" tab
    }
}
