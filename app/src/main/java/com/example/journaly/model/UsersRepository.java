package com.example.journaly.model;

import io.reactivex.rxjava3.core.Single;

public interface UsersRepository {

    void add(User user);

    Single<User> userFromId(String id);
}
