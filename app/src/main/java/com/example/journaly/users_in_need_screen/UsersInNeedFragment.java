package com.example.journaly.users_in_need_screen;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.journaly.R;
import com.example.journaly.common.JournalEntryAdapter;
import com.example.journaly.common.UserInNeedItemAdapter;
import com.example.journaly.databinding.FragmentUsersInNeedBinding;
import com.example.journaly.login.AuthManager;
import com.example.journaly.model.journals.JournalEntry;
import com.example.journaly.model.users.FirebaseUsersRepository;
import com.example.journaly.model.users.User;
import com.example.journaly.model.users.UsersRepository;
import com.example.journaly.profile_screen.ProfileActivity;
import com.example.journaly.utils.AnimationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class UsersInNeedFragment extends Fragment {

    public static final String TAG = "UsersInNeedFragment";
    private FragmentUsersInNeedBinding binding;
    private UserInNeedItemAdapter adapter;
    private List<User> usersInNeed = new ArrayList<>();
    private CompositeDisposable compositeDisposable;
    private UsersRepository usersRepository;


    public UsersInNeedFragment() {
        // Required empty public constructor
    }


    public static UsersInNeedFragment newInstance() {
        return new UsersInNeedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUsersInNeedBinding.inflate(inflater, container, false);
        compositeDisposable = new CompositeDisposable();
        usersRepository = FirebaseUsersRepository.getInstance();

        initViews();
        subscribeToData();
        return binding.getRoot();
    }

    private void initViews() {
        showEmptyViews();
        adapter = new UserInNeedItemAdapter(usersInNeed, new UserInNeedItemAdapter.OnEntryClickListener() {

            @Override
            public void onReachOutButtonClick(int position) {
                reachOutToUser(usersInNeed.get(position));
            }

            @Override
            public void onUsernameClick(int position) {
                UsersInNeedFragment.this.onUserClick(usersInNeed.get(position));
            }
        }, getContext());
        binding.usersInNeedRecyclerview.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.usersInNeedRecyclerview.setLayoutManager(layoutManager);
    }

    private void subscribeToData(){
        Disposable subscription = usersRepository.fetchUsersInNeed()
                .subscribe(users -> {
            users = users.stream()
                    .filter(user -> {
                        String loggedInId = AuthManager.getInstance().getLoggedInUserId();
                        boolean isFromLoggedInAccount = user.getUid().equals(loggedInId);
                        boolean isFromFollower = user.getFollowersAsList().contains(loggedInId);
                        return !isFromLoggedInAccount && isFromFollower;
                    })
                    .collect(Collectors.toList());

            if (users.size() > 0){
                hideEmptyViews();
            } else {
                showEmptyViews();
            }

            usersInNeed.clear();
            usersInNeed.addAll(users);
            adapter.notifyDataSetChanged();
        }, throwable -> {
            Log.w(TAG, throwable);
        });

        compositeDisposable.add(subscription);
    }

    private void reachOutToUser(User user){
        showReachOutDialog(user, (dialog, which) -> {
            usersRepository.removeUserInNeed(user.getUid()).subscribe();
        });
    }

    private void showReachOutDialog(User user, DialogInterface.OnClickListener onPositiveClick){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setMessage("This user is in need, and needs someone to talk to. Do you promise to reach out to them?");
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.setPositiveButton("Yes", onPositiveClick);
        builder.create().show();
    }

    private void onUserClick(User user) {
        Intent i = new Intent(getContext(), ProfileActivity.class);
        i.putExtra(ProfileActivity.INTENT_USER_ID_KEY, user.getUid());
        startActivity(i);
    }

    private void showEmptyViews(){
        AnimationUtils.fadeIn(600, 300, binding.searchEmptyIcon, binding.searchEmptyText);
    }

    private void hideEmptyViews(){
        AnimationUtils.fadeOut(600, binding.searchEmptyIcon, binding.searchEmptyText);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}