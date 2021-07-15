package com.example.journaly.model;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;


public interface JournalRepository {

    //returns id of added item
    String addOrUpdate(JournalEntry journalEntry);

    void delete(JournalEntry journalEntry);

    Observable<Map<String, JournalEntry>> fetch();
}
