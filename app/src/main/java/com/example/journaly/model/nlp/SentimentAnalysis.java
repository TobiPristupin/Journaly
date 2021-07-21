package com.example.journaly.model.nlp;

public class SentimentAnalysis {

    private double magnitude;
    private double score;

    public SentimentAnalysis(double magnitude, double score) {
        this.magnitude = magnitude;
        this.score = score;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
