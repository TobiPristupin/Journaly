package com.example.journaly.model;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;


//Handles all firebase db operations regarding journal entries.
public class FirebaseJournalRepository implements JournalRepository {

    private static final String TAG = "FirebaseJournalRepo";
    private static FirebaseJournalRepository instance = null;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference journalDatabaseRef = firebaseDatabase.getReference().child("journal_entries");
    private DatabaseReference userDatabaseRef = firebaseDatabase.getReference().child("users");
    private static Observable<Map<String, JournalEntry>> entriesObservable;

    private FirebaseJournalRepository(){

    }

    public static FirebaseJournalRepository getInstance(){
        if (instance == null){
            instance = new FirebaseJournalRepository();
        }

        return instance;
    }

    public String add(JournalEntry item) {
        //make a reference to where the journal will be added. This will generate an ID for us
        DatabaseReference ref = journalDatabaseRef.push();
        String id = ref.getKey();
        item.setId(id);
        //push to database
        ref.setValue(item);
        return id;
    }

    public void delete(JournalEntry item) {

    }

    public void update(JournalEntry item) {

    }

    public Observable<Map<String, JournalEntry>> fetch() {
        if (entriesObservable == null){
            entriesObservable = createEntriesObservable();
        }

        return entriesObservable;
    }

    private Observable<Map<String, JournalEntry>> createEntriesObservable() {
        return Observable.create((ObservableOnSubscribe<Map<String, JournalEntry>>) emitter -> {
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
    private ValueEventListener createDatabaseConnection(ObservableEmitter<Map<String, JournalEntry>> emitter) {
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

    private void onNewEntries(DataSnapshot dataSnapshot, ObservableEmitter<Map<String, JournalEntry>> emitter) {
        Map<String, JournalEntry> entries = new HashMap<>();
        for (DataSnapshot data : dataSnapshot.getChildren()){
            JournalEntry entry = data.getValue(JournalEntry.class);
            String id = data.getKey();
            entries.put(id, entry);
        }

        emitter.onNext(entries);
    }

    private void onNewEntriesCancelled(DatabaseError databaseError, ObservableEmitter<Map<String, JournalEntry>> emitter) {
        Log.w(TAG, databaseError.getMessage());
        emitter.onError(databaseError.toException());
    }
}
