package com.example.journaly.goals_screen;

import android.app.TimePickerDialog;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.journaly.R;

import ernestoyaquello.com.verticalstepperform.Step;

public class ReminderTimeStep extends Step<Pair<Integer, Integer>> {

    private TextView timeTextView;
    private ImageView dropdown;

    private Integer mHourOfDay = -1;
    private Integer mMinute = -1;

    public ReminderTimeStep(){
        super("Remind me", "Time of reminder notifications (optional)");
    }

    @Override
    public Pair<Integer, Integer> getStepData() {
        return new Pair<>(mHourOfDay, mMinute);
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        if (mHourOfDay == -1){
            return "(Empty)";
        }
        return formatTime(mHourOfDay, mMinute);
    }

    @Override
    public void restoreStepData(Pair<Integer, Integer> data) {
        mHourOfDay = data.first;
        mMinute = data.second;
        timeTextView.setText(formatTime(mHourOfDay, mMinute));
    }

    @Override
    protected IsDataValid isStepDataValid(Pair<Integer, Integer> stepData) {
        //will always be valid, since TimePicker dialog will always
        //output valid values.
        return new IsDataValid(true);
    }

    @Override
    protected View createStepContentLayout() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View root = layoutInflater.inflate(R.layout.step_remind_me_time, null);
        timeTextView = root.findViewById(R.id.remind_me_time_textview);
        dropdown = root.findViewById(R.id.reminder_time_dropdown);

        timeTextView.setText("No time set");
        dropdown.setOnClickListener(v -> {
            showTimePickerDialog();
        });

        return root;
    }

    private void showTimePickerDialog() {
        TimePickerDialog dialog = new TimePickerDialog(getContext(), (TimePickerDialog.OnTimeSetListener) (view, hourOfDay, minute) -> {
            this.mHourOfDay = hourOfDay;
            this.mMinute = minute;
            timeTextView.setText(formatTime(hourOfDay, minute));
        }, 8, 0, true);
        dialog.show();
    }

    @Override
    protected void onStepOpened(boolean animated) {

    }

    @Override
    protected void onStepClosed(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {

    }

    private String formatTime(int hour, int minute){
        return String.format("%02d", hour) + ":" + String.format("%02d", minute);
    }
}
