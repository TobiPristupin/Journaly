package com.example.journaly.model;

import com.google.firebase.auth.FirebaseUser;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface UsersRepository {

    void createNewUser(String userId, String email, String photoUri);

    Single<User> userFromId(String id);

    Completable updateUserBio(String userId, String bio);

    Completable updateUserContactInfo(String userId, String contactInfo);

    Completable updateUserBioAndContactInfo(String userId, String bio, String contactInfo);
}
