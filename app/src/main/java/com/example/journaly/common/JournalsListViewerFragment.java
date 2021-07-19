package com.example.journaly.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.journaly.create_screen.CreateActivity;
import com.example.journaly.databinding.FragmentJournalsListViewerBinding;
import com.example.journaly.login.AuthManager;
import com.example.journaly.model.FirebaseJournalRepository;
import com.example.journaly.model.FirebaseUsersRepository;
import com.example.journaly.model.JournalEntry;
import com.example.journaly.model.JournalRepository;
import com.example.journaly.model.User;
import com.example.journaly.model.UsersRepository;
import com.example.journaly.profile_screen.ProfileActivity;
import com.example.journaly.profile_screen.ProfileFragment;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;

public class JournalsListViewerFragment extends Fragment {

    public enum Mode {
        USER_PROFILE, //show posts only from certain user
        HOME_FEED, //show home feed
    }

    public static final String TAG = "JournalsListViewerFragment";
    private FragmentJournalsListViewerBinding binding;

    private static final String MODE_PARAM = "mode";
    private static final String USER_PARAM = "user";
    private Mode mode;
    private String userId;

    private JournalEntryAdapter journalAdapter;
    private List<JournalEntry> allJournals = new ArrayList<>(); //holds all loaded journals
    private List<JournalEntry> displayedJournals = new ArrayList<>(); //holds all currently displayed journals according to filtering
    private JournalRepository journalRepository = FirebaseJournalRepository.getInstance();
    private UsersRepository usersRepository = FirebaseUsersRepository.getInstance();
    private List<Disposable> dataSubscriptions = new ArrayList<>();


    public JournalsListViewerFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(Mode mode, String userId) {
        Fragment fragment = new JournalsListViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MODE_PARAM, mode);
        bundle.putString(USER_PARAM, userId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractDataFromArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJournalsListViewerBinding.inflate(inflater, container, false);
        initViews();
        subscribeToData();
        return binding.getRoot();
    }

    private void extractDataFromArguments() {
        Bundle bundle = getArguments();
        mode = (Mode) bundle.getSerializable(MODE_PARAM);
        userId = bundle.getString(USER_PARAM);
    }

    private void subscribeToData() {
        Disposable subscription1 = journalRepository.fetch()
                .map((Function<Map<String, JournalEntry>, List<JournalEntry>>) stringJournalEntryMap -> new ArrayList<>(stringJournalEntryMap.values()))
                .subscribe(journalEntries -> {
                    allJournals.clear();
                    allJournals.addAll(journalEntries);
                }, throwable -> {
                    Log.w(TAG, throwable);
                });

        Disposable subscription2 = journalRepository.fetch()
                .map(stringJournalEntryMap -> {
                    //why are we using Java's stream filter instead of RxJava filter? Because RxJava
                    //filter operates only on an observable of items, and in this case we have an
                    //observable of one item, that item being a map containing all journal entries. We
                    //could use observableToIteratable, then filter, then toList, but that introduces some
                    //other issues explained here: https://github.com/ReactiveX/RxJava/issues/3861.
                    return stringJournalEntryMap.values().stream()
                            .filter(journalEntry -> filterPost(journalEntry))
                            .sorted()
                            .collect(Collectors.toList());
                })
                .subscribe(filteredJournalEntries -> {
                    displayedJournals.clear();
                    displayedJournals.addAll(filteredJournalEntries);
                    journalAdapter.notifyDataSetChanged();
                }, throwable -> Log.w(TAG, throwable));

        dataSubscriptions.add(subscription1);
        dataSubscriptions.add(subscription2);
    }

    private void initViews() {
        journalAdapter = new JournalEntryAdapter(displayedJournals, usersRepository, new JournalEntryAdapter.OnEntryClickListener() {
            @Override
            public void onUsernameClick(int position) {
                onJournalUsernameClick(displayedJournals.get(position));
            }

            @Override
            public void onEntryClick(int position) {
                onJournalItemClick(displayedJournals.get(position));
            }
        }, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.entriesRecyclerview.setAdapter(journalAdapter);
        binding.entriesRecyclerview.setLayoutManager(layoutManager);
    }

    private void onJournalUsernameClick(JournalEntry entry){
        Intent i = new Intent(getContext(), ProfileActivity.class);
        i.putExtra(ProfileActivity.INTENT_USER_ID_KEY, entry.getUserId());
        startActivity(i);
    }

    private void onJournalItemClick(JournalEntry journalEntry) {
        Intent i = new Intent(getContext(), CreateActivity.class);

        CreateActivity.Mode mode;
        if (AuthManager.getInstance().getLoggedInUserId().equals(journalEntry.getUserId())) {
            mode = CreateActivity.Mode.EDIT;
        } else {
            mode = CreateActivity.Mode.VIEW;
        }

        i.putExtra(CreateActivity.STATE_INTENT_KEY, mode);
        i.putExtra(CreateActivity.JOURNAL_ENTRY_INTENT_KEY, Parcels.wrap(journalEntry));
        startActivity(i);
    }

    private boolean filterPost(JournalEntry journalEntry) {
        if (mode == Mode.USER_PROFILE) {
            //if the user we're viewing is the same one that is logged in
            if (userId.equals(AuthManager.getInstance().getLoggedInUserId())) {
                //show all posts from that user
                return journalEntry.getUserId().equals(userId);
            } else { //user we're viewing is not the one logged in
                //show public posts only
                return journalEntry.getUserId().equals(userId) && journalEntry.isPublic();
            }
        } else if (mode == Mode.HOME_FEED) {
            //show personal posts or public posts from other users
            return journalEntry.getUserId().equals(userId) || journalEntry.isPublic();
        }

        throw new RuntimeException("Unreachable");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        for (Disposable disposable : dataSubscriptions) {
            if (disposable != null) {
                disposable.dispose();
            }
        }
    }
}