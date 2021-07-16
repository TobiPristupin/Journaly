package com.example.journaly.profile_screen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.journaly.R;
import com.example.journaly.databinding.FragmentProfileAboutBinding;
import com.example.journaly.login.LoginManager;
import com.example.journaly.model.User;

import org.parceler.Parcel;
import org.parceler.Parcels;

public class ProfileAboutFragment extends Fragment {

    private static final String USER_PARAM = "param1";

    private FragmentProfileAboutBinding binding;
    private User user;
    private boolean editMode = false; //allow user to edit fields

    public ProfileAboutFragment() {
        // Required empty public constructor
    }

    public static ProfileAboutFragment newInstance(User user) {
        ProfileAboutFragment fragment = new ProfileAboutFragment();
        Bundle args = new Bundle();
        args.putParcelable(USER_PARAM, Parcels.wrap(user));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = Parcels.unwrap(getArguments().getParcelable(USER_PARAM));
        editMode = LoginManager.getInstance().getCurrentUser().getUid().equals(user.getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileAboutBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }
}