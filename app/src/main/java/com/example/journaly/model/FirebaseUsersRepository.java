package com.example.journaly.model;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;

public class FirebaseUsersRepository implements UsersRepository {

    private static final String TAG = "FirebaseUsersRepository";
    private static FirebaseUsersRepository instance = null;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference userDatabaseRef = firebaseDatabase.getReference().child("users");

    private FirebaseUsersRepository(){

    }

    public static FirebaseUsersRepository getInstance(){
        if (instance == null){
            instance = new FirebaseUsersRepository();
        }

        return instance;
    }

    public void add(User user){
        userDatabaseRef.child(user.getUid()).setValue(user);
    }

    public Single<User> userFromId(String uid){
        return Single.create(emitter -> userDatabaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull @NotNull DataSnapshot snapshot) {
                emitter.onSuccess(snapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull @NotNull DatabaseError error) {
                emitter.onError(error.toException());
            }
        }));
    }

}
