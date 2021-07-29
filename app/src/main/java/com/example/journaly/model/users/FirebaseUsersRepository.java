package com.example.journaly.model.users;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.journaly.login.AuthManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;

//Use this class for creating a new user object in database, and fetching user data from database. If
//you want to quickly know who is the currently logged in user, use AuthManager class. This class interfaces
//with the Firebase Database Service, AuthManager interfaces with Firebase Auth Service, hence the separation.
public class FirebaseUsersRepository implements UsersRepository {

    private static final String TAG = "FirebaseUsersRepository";
    private static FirebaseUsersRepository instance = null;
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference userDatabaseRef = firebaseDatabase.getReference().child("users");
    private final DatabaseReference usersInNeedDatabaseRef = firebaseDatabase.getReference().child("users_in_need");
    private Observable<List<User>> allUsersObservable = null;

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

            User user = User.withDefaultValues(userId, photoUri, email);
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

    @Override
    public Observable<List<User>> fetchAllUsers() {
        /*
        we don't want to create a new observable every time (which in turn creates a new db connection).
        Instead we want to cache the observable. We can do this since this class is a singleton.
        */
        if (allUsersObservable == null) {
            allUsersObservable = createAllUsersObservable();
        }

        return allUsersObservable;
    }

    private Observable<List<User>> createAllUsersObservable() {
        return Observable.create((ObservableOnSubscribe<List<User>>) emitter -> {
            //create a firebase db listener that relays data to the emitter
            ValueEventListener dbListener = createDatabaseConnectionForListOfUsers(emitter);
            userDatabaseRef.addValueEventListener(dbListener);
            //when the emitter is canceled, remember to cancel the database listener also
            emitter.setCancellable(() -> userDatabaseRef.removeEventListener(dbListener));
        }).replay(1).refCount();
        /*
        replay(1) is very important: it makes the observable always remember the last stream of data emitted.
        So when a new activity/fragment subscribes to this observer, they will immediately receive the last stream of data.
        replay saves us from having to manually keep a cache.
        */
    }

    @Override
    public Observable<List<User>> fetchUsersInNeed() {
        return createUsersInNeedObservable();
    }

    private Observable<List<User>> createUsersInNeedObservable() {
        return Observable.create(emitter -> {
            usersInNeedDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    List<ObservableSource<User>> observableSources = new ArrayList<>();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        observableSources.add(fetchUserFromId(data.getValue(String.class)).take(1));
                    }
                    Observable.merge(observableSources).toList().subscribe(users -> emitter.onNext(users));
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.w(TAG, error.getMessage());
                }
            });
        });
    }

    private ValueEventListener createDatabaseConnectionForListOfUsers(ObservableEmitter<List<User>> emitter) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    users.add(data.getValue(User.class));
                }
                emitter.onNext(users);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.w(TAG, error.getMessage());
            }
        };
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
    public Completable updateUserBioAndContactInfo(String bio, String contactInfo) {
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

    @Override
    public Completable addUserInNeed(String userId) {
        Completable addToDb = Completable.create(emitter -> {
            usersInNeedDatabaseRef.orderByValue().equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) { //make sure it doesn't exist before adding just in case
                        usersInNeedDatabaseRef.push().setValue(userId);
                    }
                    emitter.onComplete();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.w(TAG, error.getMessage());
                    emitter.onError(error.toException());
                }
            });
        });

        return Completable.mergeArray(addToDb, updateInNeed(userId, true));
    }

    @Override
    public Completable removeUserInNeed(String userId) {
        Completable removeFromDb = Completable.create(emitter -> {
            usersInNeedDatabaseRef.orderByValue().equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Log.w(TAG, "User to remove from in need does not exist");
                        return;
                    }
                    snapshot.getChildren().forEach(dataSnapshot -> {
                        usersInNeedDatabaseRef.child(dataSnapshot.getKey()).setValue(null);
                    });
                    emitter.onComplete();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.w(TAG, error.getMessage());
                    emitter.onError(error.toException());
                }
            });
        });

        return Completable.mergeArray(removeFromDb, updateInNeed(userId, false));
    }

    private Completable addToFollowersList(String userMakingFollow, String userBeingFollowed) {
        return Completable.create(emitter -> userDatabaseRef.child(userBeingFollowed).child("followers").push().setValue(userMakingFollow)
                .addOnCompleteListener(task -> emitter.onComplete())
                .addOnFailureListener(e -> emitter.onError(e)));
    }

    private Completable addToFollowingList(String userMakingFollow, String userBeingFollowed) {
        return Completable.create(emitter -> userDatabaseRef.child(userMakingFollow).child("following").push().setValue(userBeingFollowed)
                .addOnCompleteListener(task -> emitter.onComplete())
                .addOnFailureListener(e -> emitter.onError(e)));
    }

    private Completable removeFromFollowersList(String userMakingUnfollow, String userBeingUnfollowed) {
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

    private Completable removeFromFollowingList(String userMakingUnfollow, String userBeingUnfollowed) {
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

    @Override
    public Completable updateThreshold(double negativityThreshold) {
        return Completable.create(emitter -> {
            String loggedInId = AuthManager.getInstance().getLoggedInUserId();
            userDatabaseRef.child(loggedInId).child("negativityThreshold").setValue(negativityThreshold)
                    .addOnCompleteListener(task -> emitter.onComplete())
                    .addOnFailureListener(e -> emitter.onError(e));
        });
    }

    @Override
    public Completable updateLastAnalyzed(String lastAnalyzed) {
        return Completable.create(emitter -> {
            String loggedInId = AuthManager.getInstance().getLoggedInUserId();
            userDatabaseRef.child(loggedInId).child("idOfLastJournalEntryAnalyzed").setValue(lastAnalyzed)
                    .addOnCompleteListener(task -> emitter.onComplete())
                    .addOnFailureListener(e -> emitter.onError(e));
        });
    }

    @Override
    public Completable updateProfilePicture(String url) {
        return Completable.create(emitter -> {
            String loggedInId = AuthManager.getInstance().getLoggedInUserId();
            userDatabaseRef.child(loggedInId).child("photoUri").setValue(url)
                    .addOnCompleteListener(task -> emitter.onComplete())
                    .addOnFailureListener(e -> emitter.onError(e));
        });
    }

    @Override
    public Completable updateInNeed(String userId, boolean inNeed) {
        return Completable.create(emitter -> {
            String loggedInId = AuthManager.getInstance().getLoggedInUserId();
            userDatabaseRef.child(userId).child("inNeed").setValue(inNeed)
                    .addOnCompleteListener(task -> emitter.onComplete())
                    .addOnFailureListener(e -> emitter.onError(e));
        });
    }
}
