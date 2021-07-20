package com.example.journaly.search_screen;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.journaly.R;
import com.example.journaly.common.ProfileItemAdapter;
import com.example.journaly.databinding.FragmentSearchBinding;
import com.example.journaly.model.FirebaseUsersRepository;
import com.example.journaly.model.User;
import com.example.journaly.model.UsersRepository;
import com.example.journaly.profile_screen.ProfileActivity;
import com.example.journaly.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SearchFragment extends Fragment {

    public static final String TAG = "SearchFragment";

    /*
    If the levenshtein distance of a username and a search query is <= this threshold, the username
    is matched for the search.
    */
    public static final int LEVENSHTEIN_DISTANCE_THRESHOLD = 3;

    private FragmentSearchBinding binding;
    private ProfileItemAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> displayedUsers = new ArrayList<>();; //users displayed after filtering is applied
    private UsersRepository usersRepository;
    private CompositeDisposable disposable;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        usersRepository = FirebaseUsersRepository.getInstance();
        disposable = new CompositeDisposable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment

        initRecyclerView();
        subscribeToData();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOptionsMenu();
        initSearchFunctionality();
    }

    private void subscribeToData(){
        usersRepository.fetchAllUsers().subscribe(users -> {
            SearchFragment.this.allUsers = users;
        }, throwable -> {
            Log.w(TAG, throwable);
        });
    }

    private void initSearchFunctionality() {
        MenuItem searchItem = binding.searchToolbar.getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconified(true);
        searchView.setQueryHint("Search for a user");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //empty since all filtering is handled in onQueryTextChange
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                displayedUsers.clear();
                allUsers.stream()
                        .filter(user -> StringUtils.levenshteinDistance(newText, user.getDisplayName()) < LEVENSHTEIN_DISTANCE_THRESHOLD)
                        .forEach(user -> displayedUsers.add(user));
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void initOptionsMenu() {
        binding.searchToolbar.inflateMenu(R.menu.search_toolbar_menu);
    }

    private void initRecyclerView(){
        adapter = new ProfileItemAdapter(displayedUsers, position -> {
            Intent i = new Intent(getContext(), ProfileActivity.class);
            i.putExtra(ProfileActivity.INTENT_USER_ID_KEY, displayedUsers.get(position).getUid());
            startActivity(i);
        }, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.searchRecyclerview.setAdapter(adapter);
        binding.searchRecyclerview.setLayoutManager(layoutManager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }
}