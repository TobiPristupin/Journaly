package com.example.journaly.model.users;

import java.util.List;
import java.util.Optional;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

/*
Note that methods that perform a change to a user profile do not take in a userId. This is an added
security measure, because a user X should not modify the profile of user Y. Instead, the implementation
of this interface should get the id of the currently logged in user through other means.
*/
public interface UsersRepository {

    void createNewUser(String userId, String email, String photoUri);

    //returns an observable so client can listen to changes
    Observable<User> fetchUserFromId(String id);

    Observable<List<User>> fetchAllUsers();

    Observable<List<User>> fetchUsersInNeed();

    Observable<Optional<Goal>> fetchUserGoal();

    Completable updateUserBio(String bio);

    Completable updateUserContactInfo(String contactInfo);

    Completable updateUserBioAndContactInfo(String bio, String contactInfo);

    Completable follow(String userIdToFollow);

    Completable unfollow(String userIdToUnfollow);

    Completable addUserInNeed(String userId);

    Completable removeUserInNeed(String userId);

    Completable updateThreshold(double negativityThreshold);

    Completable updateLastAnalyzed(String lastAnalyzed);

    Completable updateInNeed(String userId, boolean inNeed);

    Completable updateProfilePicture(String url);

    Completable updateGoal(Goal goal);

    Completable deleteGoal();

}
