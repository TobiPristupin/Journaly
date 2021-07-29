package com.example.journaly.home_screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.journaly.R;
import com.example.journaly.common.JournalsListViewerFragment;
import com.example.journaly.databinding.FragmentHomeBinding;
import com.example.journaly.login.AuthManager;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            JournalsListViewerFragment.Mode homeFeedMode = JournalsListViewerFragment.Mode.HOME_FEED;
            String loggedInId = AuthManager.getInstance().getLoggedInUserId();
            Fragment journalsListViewerFragment = JournalsListViewerFragment.newInstance(homeFeedMode, loggedInId);

            getParentFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.home_fragment_container_view, journalsListViewerFragment)
                    .commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);


        return binding.getRoot();
    }


}