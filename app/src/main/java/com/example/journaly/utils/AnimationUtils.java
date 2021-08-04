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

    public static void fadeOut(long duration, Runnable endAction, View... views) {
        for (int i = 0; i < views.length - 1; i ++){
            views[i].setVisibility(View.GONE);
            views[i].animate().setDuration(duration).alpha(0.0f);
        }

        //only enable endAction on the last view animated. We don't want to run endAction for every view.
        views[views.length - 1].setVisibility(View.GONE);
        views[views.length - 1].animate().setDuration(duration).alpha(0.0f).withEndAction(endAction);
    }
}
