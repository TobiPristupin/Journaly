package com.example.journaly.utils;

import android.view.View;

public class AnimationUtils {


    public static void fadeIn(long duration, long startDelay, View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
            view.animate().setDuration(duration).setStartDelay(startDelay).alpha(1.0f);
        }
    }

    public static void fadeOut(long duration, View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
            view.animate().setDuration(duration).alpha(0.0f);
        }
    }
}
