package com.example.journaly.model.journals;

import android.util.Log;

import com.example.journaly.login.AuthManager;
import com.example.journaly.model.users.FirebaseUsersRepository;
import com.example.journaly.model.users.UserInNeedUtils;
import com.example.journaly.model.users.UsersRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;


//Handles all firebase db operations regarding journal entries.
public class FirebaseJournalRepository implements JournalRepository {

    private static final String TAG = "FirebaseJournalRepo";
    private static FirebaseJournalRepository instance = null;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference journalDatabaseRef = firebaseDatabase.getReference().child("journal_entries");
    private DatabaseReference usersInNeedDatabaseRef = firebaseDatabase.getReference().child("users_in_need");
    private static Observable<List<JournalEntry>> entriesObservable;

    private UsersRepository usersRepository = FirebaseUsersRepository.getInstance();

    private FirebaseJournalRepository() {

    }

    public static FirebaseJournalRepository getInstance() {
        if (instance == null) {
            instance = new FirebaseJournalRepository();
        }

        return instance;
    }

    public String addOrUpdate(JournalEntry item) {
        //if item has id it's because it was previously inserted in db, so we must update instead of insert
        if (item.getId() != null) {
            update(item);
            return item.getId();
        }

        //make a reference to where the journal will be added. This will generate an ID for us
        DatabaseReference ref = journalDatabaseRef.push();
        String id = ref.getKey();
        item.setId(id);
        //push to database
        ref.setValue(item);
        return id;
    }

    public void delete(JournalEntry item) {
        DatabaseReference ref = journalDatabaseRef.child(item.getId());
        ref.setValue(null);
    }

    public void update(JournalEntry item) {
        DatabaseReference ref = journalDatabaseRef.child(item.getId());
        ref.setValue(item);
    }

    @Override
    public Observable<List<JournalEntry>> fetch() {
        /*
        we don't want to create a new observable every time (which in turn creates a new db connection).
        Instead we want to cache the observable. We can do this since this class is a singleton.
        */
        if (entriesObservable == null) {
            entriesObservable = createEntriesObservable();
        }

        return entriesObservable;
    }

    private Observable<List<JournalEntry>> createEntriesObservable() {
        return Observable.create((ObservableOnSubscribe<List<JournalEntry>>) emitter -> {
            //create a firebase db listener that relays data to the emitter
            ValueEventListener databaseListener = createDatabaseConnection(emitter);
            journalDatabaseRef.addValueEventListener(databaseListener);
            //when the emitter is canceled, remember to cancel the database listener also
            emitter.setCancellable(() -> journalDatabaseRef.removeEventListener(databaseListener));
        }).replay(1).refCount();
        /*
        replay(1) is very important: it makes the observable always remember the last stream of data emitted.
        So when a new activity/fragment subscribes to this observer, they will immediately receive the last stream of data.
        replay saves us from having to manually keep a cache.
        */
    }

    //creates a listener to the firebase database that will relay all db data to an RxJava emitter
    private ValueEventListener createDatabaseConnection(ObservableEmitter<List<JournalEntry>> emitter) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onNewEntries(dataSnapshot, emitter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onNewEntriesCancelled(databaseError, emitter);
            }
        };
    }

    private void onNewEntries(DataSnapshot dataSnapshot, ObservableEmitter<List<JournalEntry>> emitter) {
        List<JournalEntry> entries = new ArrayList<>();
        for (DataSnapshot data : dataSnapshot.getChildren()) {
            JournalEntry entry = data.getValue(JournalEntry.class);
            entries.add(entry);
        }

        emitter.onNext(entries); //notify observers of new data
        performUsersInNeedDetection(entries);
    }

    private void onNewEntriesCancelled(DatabaseError databaseError, ObservableEmitter<List<JournalEntry>> emitter) {
        Log.w(TAG, databaseError.getMessage());
    }

    //This method will determine if the current user has to be considered "in need". If so, it will do
    //the necessary updates, which are: placing user in users_in_need table in db, and updating their threshold
    private void performUsersInNeedDetection(List<JournalEntry> entries) {
        String loggedInId = AuthManager.getInstance().getLoggedInUserId();
        usersRepository.fetchUserFromId(loggedInId).take(1).subscribe(user -> {
            if (user.isInNeed()){
                return;
            }

            UserInNeedUtils.Response  response = UserInNeedUtils.isUserInNeed(entries, user);
            if (response.isInNeed()){
                usersInNeedDatabaseRef.push().setValue(user.getUid());
                usersRepository.updateInNeed(true).subscribe();
            }

            if (user.getNegativityThreshold() != response.getUpdatedNegativityThreshold()){
                usersRepository.updateThreshold(response.getUpdatedNegativityThreshold()).subscribe();
            }

            if (!user.getIdOfLastJournalEntryAnalyzed().equals(response.getLastEntryIdAnalyzed())){
                usersRepository.updateLastAnalyzed(response.getLastEntryIdAnalyzed()).subscribe();
            }
        });
    }
}
