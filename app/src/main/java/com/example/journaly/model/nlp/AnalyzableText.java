package com.example.journaly.model.nlp;


//Model class for text that will be sent to Google Cloud for analysis
public class AnalyzableText {

    private String content;
    private String type;
    private String language;

    public AnalyzableText(String text) {
        this.content = text;
        this.type = "PLAIN_TEXT";
        this.language = "en";
    }

    public AnalyzableText(){
        //empty constructor for serialization
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
