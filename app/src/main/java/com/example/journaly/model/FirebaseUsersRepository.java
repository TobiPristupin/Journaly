package com.example.journaly.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.journaly.login.AuthManager;
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
import io.reactivex.rxjava3.core.CompletableEmitter;
import io.reactivex.rxjava3.core.CompletableOnSubscribe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Cancellable;

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

    @Override
    public void createNewUser(String userId, String email, String photoUri) {
        userExistsInDb(userId).subscribe(userExists -> {
            if (userExists) {
                throw new RuntimeException("Attempting to create user that already exists. Aborting since this operation will overwrite existing data");
            }

            User user = new User(userId, photoUri, email, null, null, null, null);
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

    @Override
    public Observable<User> fetchUserFromId(String uid) {
        return Observable.create(emitter -> {
            ValueEventListener dbListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    emitter.onNext(snapshot.getValue(User.class));
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.w(TAG, error.getMessage());
                }
            };

            userDatabaseRef.child(uid).addValueEventListener(dbListener);
            emitter.setCancellable(() -> userDatabaseRef.child("uid").removeEventListener(dbListener));
        });
    }

    public Completable updateUserBio(String bio) {
        String userId = AuthManager.getInstance().getLoggedInUserId();
        return Completable.create(emitter -> userDatabaseRef.child(userId).child("bio").setValue(bio)
                .addOnCompleteListener(task -> emitter.onComplete())
                .addOnFailureListener(e -> emitter.onError(e)));
    }

    public Completable updateUserContactInfo(String contactInfo) {
        String userId = AuthManager.getInstance().getLoggedInUserId();
        return Completable.create(emitter -> userDatabaseRef.child(userId).child("contactInfo").setValue(contactInfo)
                .addOnCompleteListener(task -> emitter.onComplete())
                .addOnFailureListener(e -> emitter.onError(e)));
    }

    @Override
    public Completable updateUserBioAndContactInfo(String bio, String contactInfo){
        return Completable.mergeArray(updateUserBio(bio), updateUserContactInfo(contactInfo));
    }

    @Override
    public Completable follow(String userIdToFollow) {
        String userMakingFollow = AuthManager.getInstance().getLoggedInUserId();
        return Completable.mergeArray(
                addToFollowersList(userMakingFollow, userIdToFollow),
                addToFollowingList(userMakingFollow, userIdToFollow)
        );
    }

    @Override
    public Completable unfollow(String userIdToUnfollow) {
        String userMakingUnfollow = AuthManager.getInstance().getLoggedInUserId();
        return Completable.mergeArray(
                removeFromFollowersList(userMakingUnfollow, userIdToUnfollow),
                removeFromFollowingList(userMakingUnfollow, userIdToUnfollow)
        );
    }

    private Completable addToFollowersList(String userMakingFollow, String userBeingFollowed){
        return Completable.create(emitter -> userDatabaseRef.child(userBeingFollowed).child("followers").push().setValue(userMakingFollow)
                .addOnCompleteListener(task -> emitter.onComplete())
                .addOnFailureListener(e -> emitter.onError(e)));
    }

    private Completable addToFollowingList(String userMakingFollow, String userBeingFollowed){
        return Completable.create(emitter -> userDatabaseRef.child(userMakingFollow).child("following").push().setValue(userBeingFollowed)
                .addOnCompleteListener(task -> emitter.onComplete())
                .addOnFailureListener(e -> emitter.onError(e)));
    }

    private Completable removeFromFollowersList(String userMakingUnfollow, String userBeingUnfollowed){
        return Completable.create(emitter -> {
            userDatabaseRef.child(userBeingUnfollowed).child("followers").orderByValue().equalTo(userMakingUnfollow)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            snapshot.getRef().removeValue().addOnCompleteListener(task -> {
                                emitter.onComplete();
                            }).addOnFailureListener(e -> {
                                emitter.onError(e);
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            emitter.onError(error.toException());
                        }
                    });
        });
    }

    private Completable removeFromFollowingList(String userMakingUnfollow, String userBeingUnfollowed){
        return Completable.create(emitter -> {
            userDatabaseRef.child(userMakingUnfollow).child("following").orderByValue().equalTo(userBeingUnfollowed)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            snapshot.getRef().removeValue().addOnCompleteListener(task -> {
                                emitter.onComplete();
                            }).addOnFailureListener(e -> {
                                emitter.onError(e);
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            emitter.onError(error.toException());
                        }
                    });
        });
    }

}
