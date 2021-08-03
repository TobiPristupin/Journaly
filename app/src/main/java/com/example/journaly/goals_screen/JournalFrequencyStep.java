package com.example.journaly.goals_screen;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.journaly.R;

import ernestoyaquello.com.verticalstepperform.Step;

public class JournalFrequencyStep extends Step<Pair<Integer, Integer>> {

    private EditText daysInput;
    private EditText timesInput;

    protected JournalFrequencyStep() {
        super("Journal Frequency", "How often you want to journal");
    }

    @Override
    public Pair<Integer, Integer> getStepData() {
        String days = daysInput.getText().toString();
        String times = timesInput.getText().toString();

        if (days.equals("") || times.equals("")){
            return null;
        }

        return new Pair<>(Integer.valueOf(times), Integer.valueOf(days));
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        Pair<Integer, Integer> inputs = getStepData();
        if (inputs == null){
            return "(Empty)";
        }
        return "Journal " + inputs.first + " time(s) every " + inputs.second + " day(s)";
    }

    @Override
    public void restoreStepData(Pair<Integer, Integer> data) {
        timesInput.setText(String.valueOf(data.first));
        daysInput.setText(String.valueOf(data.second));
    }

    @Override
    protected IsDataValid isStepDataValid(Pair<Integer, Integer> stepData) {
        if (stepData == null){
            return new IsDataValid(false);
        }
        boolean isInputValid = stepData.first > 0 && stepData.second > 0;
        String errorMessage = isInputValid ? "" : "Both values must be greater than zero";
        return new IsDataValid(isInputValid, errorMessage);
    }

    @Override
    protected View createStepContentLayout() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View root =  inflater.inflate(R.layout.step_journal_frequency, null, false);
        daysInput = root.findViewById(R.id.journal_days_input);
        timesInput = root.findViewById(R.id.journal_times_input);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                markAsCompletedOrUncompleted(true);
            }
        };

        daysInput.addTextChangedListener(textWatcher);
        timesInput.addTextChangedListener(textWatcher);

        return root;
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
}
