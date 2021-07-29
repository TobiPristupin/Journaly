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
import com.example.journaly.model.journals.FirebaseJournalRepository;
import com.example.journaly.model.journals.JournalEntry;
import com.example.journaly.model.journals.JournalRepository;
import com.example.journaly.model.users.FirebaseUsersRepository;
import com.example.journaly.model.users.User;
import com.example.journaly.model.users.UsersRepository;
import com.example.journaly.profile_screen.ProfileActivity;
import com.example.journaly.utils.AnimationUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;

public class JournalsListViewerFragment extends Fragment {

    public static final String TAG = "JournalsListViewerFragment";
    private static final String MODE_PARAM = "mode";
    private static final String USER_PARAM = "user";
    private FragmentJournalsListViewerBinding binding;
    private Mode mode;
    private String userId;
    private User user;
    private JournalEntryAdapter journalAdapter;
    private final List<JournalEntry> displayedJournals = new ArrayList<>(); //holds all currently displayed journals according to filtering
    private final JournalRepository journalRepository = FirebaseJournalRepository.getInstance();
    private final UsersRepository usersRepository = FirebaseUsersRepository.getInstance();
    private final List<Disposable> dataSubscriptions = new ArrayList<>();

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
        /*
        use .take(1) since we don't want continuous updates on this user (if the user we're to be modified in the database).
        we just want to fetch it once.
        */
        Disposable disposable = usersRepository.fetchUserFromId(AuthManager.getInstance().getLoggedInUserId()).take(1)
                .doOnNext(user -> {
                    JournalsListViewerFragment.this.user = user;
                })
                .flatMap((Function<User, ObservableSource<List<JournalEntry>>>) user -> {
                    return journalRepository.fetch();
                })
                .subscribe(entries -> {
                    updateRecyclerView(entries);
                }, throwable -> {
                    Log.w(TAG, throwable);
                });


        dataSubscriptions.add(disposable);
    }

    private void updateRecyclerView(List<JournalEntry> entries) {
        List<JournalEntry> filtered = entries.stream()
                .filter(this::filterPost)
                .sorted(Comparator.comparingLong(JournalEntry::getDate)
                        .reversed())
                .collect(Collectors.toList());

        if (filtered.size() > 0) {
            hideEmptyViews();
        } else {
            showEmptyViews();
        }


        displayedJournals.clear();
        displayedJournals.addAll(filtered);
        journalAdapter.notifyDataSetChanged();
    }

    private void initViews() {
        showEmptyViews();

        journalAdapter = new JournalEntryAdapter(displayedJournals, usersRepository, new JournalEntryAdapter.OnEntryClickListener() {
            @Override
            public void onUsernameClick(int position) {
                onJournalUsernameClick(displayedJournals.get(position));
            }

            @Override
            public void onEntryClick(int position) {
                onJournalItemClick(displayedJournals.get(position));
            }
        }, getActivity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.entriesRecyclerview.setAdapter(journalAdapter);
        binding.entriesRecyclerview.setLayoutManager(layoutManager);
    }

    private void onJournalUsernameClick(JournalEntry entry) {
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
        String loggedInId = AuthManager.getInstance().getLoggedInUserId();
        if (mode == Mode.USER_PROFILE) {
            if (userId.equals(loggedInId)) { //if the user we're viewing is the same one that is logged in
                //show all posts from that user
                return journalEntry.getUserId().equals(userId);
            } else { //user we're viewing is not the one logged in
                //show public posts only
                return journalEntry.getUserId().equals(userId) && journalEntry.isPublic();
            }
        } else if (mode == Mode.HOME_FEED) {
            if (journalEntry.getUserId().equals(loggedInId)) {
                return true; //show personal posts
            } else {
                //show posts from other users if post is public and logged in user follows creator
                return journalEntry.isPublic() && user.getFollowingAsList().contains(journalEntry.getUserId());
            }
        }

        throw new RuntimeException("Unreachable");
    }

    private void showEmptyViews() {
        AnimationUtils.fadeIn(800, 500, binding.journalsViewerNoEntriesText);
    }

    private void hideEmptyViews() {
        AnimationUtils.fadeOut(800, binding.journalsViewerNoEntriesText);
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

    public enum Mode {
        USER_PROFILE, //show posts only from certain user
        HOME_FEED, //show home feed
    }
}