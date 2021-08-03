package com.example.journaly.goals_screen;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.journaly.R;

import ernestoyaquello.com.verticalstepperform.Step;

public class ReminderDaysStep extends Step<boolean[]> {

    private boolean[] alarmDays;
    private View daysStepContent;

    public ReminderDaysStep() {
        super("Remind me", "Days to send reminder notifications (optional)");
    }

    @NonNull
    @Override
    protected View createStepContentLayout() {

        // We create this step view by inflating an XML layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        daysStepContent = inflater.inflate(R.layout.step_days_of_week_layout, null, false);
        setupAlarmDays();

        return daysStepContent;
    }

    @Override
    protected void onStepOpened(boolean animated) {
        // No need to do anything here
    }

    @Override
    protected void onStepClosed(boolean animated) {
        // No need to do anything here
    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {
        // No need to do anything here
    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {
        // No need to do anything here
    }

    @Override
    public boolean[] getStepData() {
        return alarmDays;
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        String[] weekDayStrings = getContext().getResources().getStringArray(R.array.week_days_extended);
        List<String> selectedWeekDayStrings = new ArrayList<>();
        for (int i = 0; i < weekDayStrings.length; i++) {
            if (alarmDays[i]) {
                selectedWeekDayStrings.add(weekDayStrings[i]);
            }
        }

        if (selectedWeekDayStrings.size() == 0){
            return "(Empty)";
        }

        return TextUtils.join(", ", selectedWeekDayStrings);
    }

    @Override
    public void restoreStepData(boolean[] data) {
        alarmDays = data;
        setupAlarmDays();
    }

    @Override
    protected IsDataValid isStepDataValid(boolean[] stepData) {
        return new IsDataValid(true);
    }

    private void setupAlarmDays() {
        boolean firstSetup = alarmDays == null;
        alarmDays = firstSetup ? new boolean[7] : alarmDays;

        final String[] weekDays = getContext().getResources().getStringArray(R.array.week_days);
        for(int i = 0; i < weekDays.length; i++) {
            final int index = i;
            final View dayLayout = getDayLayout(index);

            updateDayLayout(index, dayLayout, false);

            if (dayLayout != null) {
                dayLayout.setOnClickListener(v -> {
                    alarmDays[index] = !alarmDays[index];
                    updateDayLayout(index, dayLayout, true);
                    markAsCompletedOrUncompleted(true);
                });

                final TextView dayText = dayLayout.findViewById(R.id.day);
                dayText.setText(weekDays[index]);
            }
        }
    }

    private View getDayLayout(int i) {
        int id = daysStepContent.getResources().getIdentifier(
                "day_" + i, "id", getContext().getPackageName());
        return daysStepContent.findViewById(id);
    }

    private void updateDayLayout(int dayIndex, View dayLayout, boolean useAnimations) {
        if (alarmDays[dayIndex]) {
            markAlarmDay(dayIndex, dayLayout, useAnimations);
        } else {
            unmarkAlarmDay(dayIndex, dayLayout, useAnimations);
        }
    }

    private void markAlarmDay(int dayIndex, View dayLayout, boolean useAnimations) {
        alarmDays[dayIndex] = true;

        if (dayLayout != null) {
            Drawable bg = ContextCompat.getDrawable(getContext(), ernestoyaquello.com.verticalstepperform.R.drawable.circle_step_done);
            int colorPrimary = ContextCompat.getColor(getContext(), R.color.primary);
            bg.setColorFilter(new PorterDuffColorFilter(colorPrimary, PorterDuff.Mode.SRC_IN));
            dayLayout.setBackground(bg);

            TextView dayText = dayLayout.findViewById(R.id.day);
            dayText.setTextColor(Color.rgb(255, 255, 255));
        }
    }

    private void unmarkAlarmDay(int dayIndex, View dayLayout, boolean useAnimations) {
        alarmDays[dayIndex] = false;

        dayLayout.setBackgroundResource(0);

        TextView dayText = dayLayout.findViewById(R.id.day);
        int colour = ContextCompat.getColor(getContext(), R.color.primary);
        dayText.setTextColor(colour);
    }
}