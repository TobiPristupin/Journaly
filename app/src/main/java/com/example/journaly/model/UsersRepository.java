package com.example.journaly.model;

import com.google.firebase.auth.FirebaseUser;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/*
Note that methods that perform a change to a user profile do not take in a userId. This is an added
security measure, because a user X should not modify the profile of user Y. Instead, the implementation
of this interface should get the id of the currently logged in user through other means.
*/
public interface UsersRepository {

    void createNewUser(String userId, String email, String photoUri);

    //returns an observable so client can listen to changes
    Observable<User> fetchUserFromId(String id);

    Completable updateUserBio(String bio);

    Completable updateUserContactInfo(String contactInfo);

    Completable updateUserBioAndContactInfo(String bio, String contactInfo);

    Completable follow(String userIdToFollow);

    Completable unfollow(String userIdToUnfollow);

}
