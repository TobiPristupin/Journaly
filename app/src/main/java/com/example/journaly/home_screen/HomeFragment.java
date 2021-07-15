package com.example.journaly.home_screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.journaly.common.JournalEntryAdapter;
import com.example.journaly.create_screen.CreateActivity;
import com.example.journaly.databinding.FragmentHomeBinding;
import com.example.journaly.login.LoginManager;
import com.example.journaly.model.FirebaseJournalRepository;
import com.example.journaly.model.FirebaseUsersRepository;
import com.example.journaly.model.JournalEntry;
import com.example.journaly.model.JournalRepository;
import com.example.journaly.model.UsersRepository;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private JournalEntryAdapter journalAdapter;
    private List<JournalEntry> allJournals = new ArrayList<>(); //holds all loaded journals
    private List<JournalEntry> displayedJournals = new ArrayList<>(); //holds all currently displayed journals according to filtering
    private JournalRepository journalRepository = FirebaseJournalRepository.getInstance();
    private UsersRepository usersRepository = FirebaseUsersRepository.getInstance();
    private Disposable dataSubscription;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        initViews();
        subscribeToData();

        return binding.getRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (dataSubscription != null) {
            dataSubscription.dispose();
        }
    }

    private void subscribeToData() {
        journalRepository.fetch()
                .map((Function<Map<String, JournalEntry>, List<JournalEntry>>) stringJournalEntryMap -> new ArrayList<>(stringJournalEntryMap.values()))
                .subscribe(journalEntries -> {
                    allJournals.clear();
                    allJournals.addAll(journalEntries);
                }, throwable -> {
                    Log.w(TAG, throwable);
                });

        journalRepository.fetch()
                .map(stringJournalEntryMap -> {
                    //why are we using Java's stream filter instead of RxJava filter? Because RxJava
                    //filter operates only on an observable of items, and in this case we have an
                    //observable of one item, that item being a map containing all journal entries. We
                    //could use observableToIteratable, then filter, then toList, but that introduces some
                    //other issues explained here: https://github.com/ReactiveX/RxJava/issues/3861.
                    return stringJournalEntryMap.values().stream()
                            .filter(journalEntry -> true)
                            .sorted()
                            .collect(Collectors.toList());
                })
                .subscribe(filteredJournalEntries -> {
                    displayedJournals.clear();
                    displayedJournals.addAll(filteredJournalEntries);
                    journalAdapter.notifyDataSetChanged();
                }, throwable -> Log.w(TAG, throwable));
    }

    private void initViews() {
        initRecyclerView();
    }

    private void initRecyclerView() {
        journalAdapter = new JournalEntryAdapter(displayedJournals, usersRepository,
                position -> onJournalItemClick(displayedJournals.get(position)), getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.entriesRecyclerview.setAdapter(journalAdapter);
        binding.entriesRecyclerview.setLayoutManager(layoutManager);
    }

    private void onJournalItemClick(JournalEntry journalEntry) {
        Intent i = new Intent(getContext(), CreateActivity.class);

        CreateActivity.State state;
        if (LoginManager.getInstance().getCurrentUser().getUid().equals(journalEntry.getUserId())){
            state = CreateActivity.State.EDIT;
        } else {
            state = CreateActivity.State.VIEW;
        }

        i.putExtra(CreateActivity.STATE_INTENT_KEY, state);
        i.putExtra(CreateActivity.JOURNAL_ENTRY_INTENT_KEY, Parcels.wrap(journalEntry));
        startActivity(i);
    }

}