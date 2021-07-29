package com.example.journaly.model.journals;

//Mood of a journal entru. Not to be confused wtih sentiment. Sentiment is a value
public enum Mood {
    POSITIVE(1),
    NEUTRAL(0),
    NEGATIVE(-1);

    private final int value;

    Mood(int value) {
        this.value = value;
    }

    public static Mood fromSentiment(double sentiment) {
        if (sentiment >= -0.25 && sentiment <= 0.25) {
            return Mood.NEUTRAL;
        } else if (sentiment >= 0.25) {
            return Mood.POSITIVE;
        }

        return Mood.NEGATIVE;
    }

    public int toInt() {
        return this.value;
    }

}
