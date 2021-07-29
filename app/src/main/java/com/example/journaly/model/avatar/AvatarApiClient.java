package com.example.journaly.model.avatar;

import android.net.Uri;

public class AvatarApiClient {

    public static final String BASE_URL = "https://avatars.dicebear.com/api/jdenticon/";

    public static Uri generateAvatarUri(String username) {
        return Uri.parse(BASE_URL + username + ".svg" + "?background=%23ffffff&radius=50&margin=15");
    }

}
