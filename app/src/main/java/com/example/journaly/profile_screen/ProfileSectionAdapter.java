package com.example.journaly.profile_screen;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.journaly.common.JournalsListViewerFragment;
import com.example.journaly.model.User;

import org.jetbrains.annotations.NotNull;

public class ProfileSectionAdapter extends FragmentStateAdapter  {

    private User user;

    public ProfileSectionAdapter(@NonNull @NotNull Fragment fragment, User user) {
        super(fragment);
        this.user = user;
    }



    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return ProfileAboutFragment.newInstance(user);
            case 1:
                JournalsListViewerFragment.Mode profileMode = JournalsListViewerFragment.Mode.USER_PROFILE;
                return JournalsListViewerFragment.newInstance(profileMode, user.getUid());
        };

        throw new RuntimeException("Unreachable");
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
