package com.example.journaly.model.avatar;

import android.net.Uri;

import java.io.IOException;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AvatarApiClient {

    public static final String BASE_URL = "https://avatars.dicebear.com/api/jdenticon/";

    public static Uri generateAvatarUri(String username){
        return Uri.parse(BASE_URL + username + ".svg" + "?background=%23ffffff&radius=50&margin=15");
    }

}
