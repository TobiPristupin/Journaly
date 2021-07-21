package com.example.journaly.model.nlp;

import com.example.journaly.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CloudNlpClient implements NlpRepository {

    private static CloudNlpService api = null;
    private static CloudNlpClient instance = null;
    private static final String BASE_URL = "https://language.googleapis.com/v1/";
    private static final String API_KEY  = BuildConfig.GOOGLE_CLOUD_API_KEY;

    private CloudNlpClient(){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(AnalyzableText.class, new AnalyzableTextSerializer());
        builder.registerTypeAdapter(SentimentAnalysis.class, new SentimentDeserializer());
        Gson gson = builder.create();


        api = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(CloudNlpService.class);
    }

    public static CloudNlpClient getInstance(){
        if (instance == null){
            instance = new CloudNlpClient();
        }

        return instance;
    }

    @Override
    public Single<SentimentAnalysis> performSentimentAnalysis(String text){
        AnalyzableText analyzableText = new AnalyzableText(text);
        return api.getSentimentAnalysis(analyzableText, API_KEY);
    }

}
