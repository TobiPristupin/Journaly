package com.example.journaly.model.nlp;

import io.reactivex.rxjava3.core.Single;

public interface NlpRepository {

    public Single<SentimentAnalysis> performSentimentAnalysis(String text);
}
