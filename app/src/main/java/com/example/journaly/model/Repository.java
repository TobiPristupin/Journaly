package com.example.journaly.model;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;


public interface Repository<T> {

    //returns id of added item
    String add(T item);

    void delete(T item);

    void update(T item);

    Observable<List<T>> fetch();
}
