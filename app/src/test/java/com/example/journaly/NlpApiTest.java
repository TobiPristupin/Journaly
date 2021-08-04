package com.example.journaly;

import com.example.journaly.model.nlp.CloudNlpClient;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NlpApiTest {


    @Test
    public void performSentimentAnalysisWithGoogleNLP() {
        CloudNlpClient.getInstance().performSentimentAnalysis("I love you").blockingSubscribe(sentimentAnalysis -> {
            assertTrue(sentimentAnalysis.getScore() > 0);
        }, throwable -> {
            fail(throwable.toString());
        });
    }
}
