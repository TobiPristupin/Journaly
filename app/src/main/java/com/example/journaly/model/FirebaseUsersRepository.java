package com.example.journaly.model;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

//Use this class for creating a new user object in database, and fetching user data from database. If
//you want to quickly know who is the currently logged in user, use AuthManager class. This class interfaces
//with the Firebase Database Service, AuthManager interfaces with Firebase Auth Service, hence the separation.
public class FirebaseUsersRepository implements UsersRepository {

    private static final String TAG = "FirebaseUsersRepository";
    private static FirebaseUsersRepository instance = null;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference userDatabaseRef = firebaseDatabase.getReference().child("users");

    private FirebaseUsersRepository() {

    }

    public static FirebaseUsersRepository getInstance() {
        if (instance == null) {
            instance = new FirebaseUsersRepository();
        }

        return instance;
    }

    public void createNewUser(String userId, String email, String photoUri) {
        userExistsInDb(userId).subscribe(userExists -> {
            if (userExists) {
                throw new RuntimeException("Attempting to create user that already exists. Aborting since this operation will overwrite existing data");
            }

            User user = new User(userId, photoUri, email, null, null);
            userDatabaseRef.child(userId).setValue(user);
        });
    }

    private Single<Boolean> userExistsInDb(String userId) {
        return Single.create(emitter -> {
            DatabaseReference ref = userDatabaseRef.child(userId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    emitter.onSuccess(snapshot.exists());
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    emitter.onError(error.toException());
                }
            });
        });
    }

    public Single<User> userFromId(String uid) {
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

    public Completable updateUserBio(String userId, String bio) {
        return Completable.create(emitter -> userDatabaseRef.child(userId).child("bio").setValue(bio)
                .addOnCompleteListener(task -> emitter.onComplete()).addOnFailureListener(e -> emitter.onError(e)));
    }

    public Completable updateUserContactInfo(String userId, String contactInfo) {
        return Completable.create(emitter -> userDatabaseRef.child(userId).child("contactInfo").setValue(contactInfo)
                .addOnCompleteListener(task -> emitter.onComplete()).addOnFailureListener(e -> emitter.onError(e)));
    }

    public Completable updateUserBioAndContactInfo(String userId, String bio, String contactInfo){
        return Completable.mergeArray(updateUserBio(userId, bio), updateUserContactInfo(userId, contactInfo));
    }

}
