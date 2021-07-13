package com.example.journaly.model;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class FirebaseRepository  {

    private static FirebaseRepository instance = null;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference journalDatabaseRef = firebaseDatabase.getReference().child("journal_entries");
    private DatabaseReference userDatabaseRef = firebaseDatabase.getReference().child("users");
    private static Observable<List<JournalEntry>> entriesObservable;

    private FirebaseRepository(){

    }

    public static FirebaseRepository getInstance(){
        if (instance == null){
            instance = new FirebaseRepository();
        }

        return instance;
    }

    public String addJournalEntry(JournalEntry item) {
        //make a reference to where the journal will be added. This will generate an ID for us
        DatabaseReference ref = journalDatabaseRef.push();
        String id = ref.getKey();
        item.setId(id);
        //push to database
        ref.setValue(item);
        return id;
    }

    public void deleteJournalEntry(JournalEntry item) {

    }

    public void updateJournalEntry(JournalEntry item) {

    }

    public Observable<List<JournalEntry>> fetchJournalEntries() {
        return null;
    }

    public void addUser(User user){
        userDatabaseRef.child(user.getUid()).setValue(user);
    }
}
