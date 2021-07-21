package com.example.journaly.model.nlp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class AnalyzableTextSerializer implements JsonSerializer<AnalyzableText> {


    @Override
    public JsonElement serialize(AnalyzableText src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject doc = new JsonObject();
        doc.addProperty("content", src.getContent());
        doc.addProperty("type", src.getType());
        doc.addProperty("language", src.getLanguage());

        JsonObject main = new JsonObject();
        main.add("document", doc);

        return main;
    }
}
