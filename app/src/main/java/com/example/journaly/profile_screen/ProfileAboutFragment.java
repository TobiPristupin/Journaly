package com.example.journaly.profile_screen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.journaly.databinding.FragmentProfileAboutBinding;
import com.example.journaly.login.AuthManager;
import com.example.journaly.model.FirebaseUsersRepository;
import com.example.journaly.model.User;
import com.example.journaly.model.UsersRepository;

import org.parceler.Parcels;

import es.dmoral.toasty.Toasty;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

public class ProfileAboutFragment extends Fragment {

    public static final String TAG = "ProfileAboutFragment";

    private static final String USER_PARAM = "userId";

    private FragmentProfileAboutBinding binding;
    private User user;
    private boolean editMode = false; //allow user to edit fields
    private UsersRepository usersRepository;

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
        editMode = AuthManager.getInstance().getLoggedInUserId().equals(user.getUid());
        usersRepository = FirebaseUsersRepository.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileAboutBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

    private void initViews(){
        binding.aboutEdittext.setText(user.getBio());
        binding.contactInfoEdittext.setText(user.getContactInfo());

        if (editMode){
            binding.saveButton.setVisibility(View.VISIBLE);
            initEditTextListeners();
            initSaveButton();
        } else {
            binding.saveButton.setVisibility(View.GONE);
            binding.aboutEdittext.setFocusable(false);
            binding.aboutEdittext.setBackgroundResource(android.R.color.transparent);
            if (user.getBio() == null || user.getBio().equals("")){
                binding.aboutEdittext.setHint("This user has no bio :(");
            }

            binding.contactInfoEdittext.setFocusable(false);
            binding.contactInfoEdittext.setBackgroundResource(android.R.color.transparent);
            if (user.getContactInfo() == null || user.getContactInfo().equals("")){
                binding.contactInfoEdittext.setHint("This user has no contact info :(");
            }
        }

    }

    private void initSaveButton() {
        binding.saveButton.setOnClickListener(v -> {
            String bio = binding.aboutEdittext.getText().toString();
            String about = binding.contactInfoEdittext.getText().toString();

            usersRepository.updateUserBioAndContactInfo(user.getUid(), bio, about).subscribe(() -> {
                Log.i(TAG, "Successfully updated bio/contact info");
                Toasty.success(getContext(), "Success", Toast.LENGTH_SHORT, true).show();
                binding.saveButton.setEnabled(false);
            }, throwable -> {
                Log.w(TAG, throwable);
                Toasty.error(getContext(), "There was an error.", Toast.LENGTH_SHORT, true).show();
            });
        });
    }

    private void initEditTextListeners() {
        binding.aboutEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.saveButton.setEnabled(true);
            }
        });

        binding.contactInfoEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.saveButton.setEnabled(true);
            }
        });
    }

}