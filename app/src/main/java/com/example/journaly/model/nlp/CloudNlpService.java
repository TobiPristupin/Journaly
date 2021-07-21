package com.example.journaly.model.nlp;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CloudNlpService {

    @POST("./documents:analyzeSentiment/")
    Single<SentimentAnalysis> getSentimentAnalysis(@Body AnalyzableText document, @Query("key") String apiKey);
}
