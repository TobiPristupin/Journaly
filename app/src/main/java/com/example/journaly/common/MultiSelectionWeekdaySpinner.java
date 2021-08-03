package com.example.journaly.common;


import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;


import androidx.appcompat.app.AlertDialog;

import com.example.journaly.utils.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiSelectionWeekdaySpinner extends androidx.appcompat.widget.AppCompatSpinner implements
        DialogInterface.OnMultiChoiceClickListener {

    List<String> weekdays = null;
    boolean[] selection = null;
    ArrayAdapter adapter;

    public MultiSelectionWeekdaySpinner(Context context) {
        super(context);

        adapter = new ArrayAdapter(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(adapter);
    }

    public MultiSelectionWeekdaySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        adapter = new ArrayAdapter(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(adapter);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (selection != null && which < selection.length) {
            selection[which] = isChecked;

            adapter.clear();
            adapter.add(buildSelectedItemString());
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
    }

    @Override
    public boolean performClick() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(weekdays.toArray(new CharSequence[0]), selection, this);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                // Do nothing
            }
        });

        builder.show();

        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setWeekdays(List<String> weekdays) {
        this.weekdays = weekdays;
        selection = new boolean[this.weekdays.size()];
        adapter.clear();
        adapter.add("");
        Arrays.fill(selection, false);
    }

    public void setSelection(List<String> selection) {
        for (int i = 0; i < this.selection.length; i++) {
            this.selection[i] = false;
        }

        for (String sel : selection) {
            for (int j = 0; j < weekdays.size(); ++j) {
                if (weekdays.get(j).equals(sel)) {
                    this.selection[j] = true;
                }
            }
        }

        adapter.clear();
        adapter.add(buildSelectedItemString());
    }

    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < weekdays.size(); ++i) {
            if (selection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }

                foundOne = true;

                sb.append(DateUtils.getShortenedWeekday(weekdays.get(i)));
            }
        }

        return sb.toString();
    }

    public List<String> getSelectedItems() {
        List<String> selectedItems = new ArrayList<>();

        for (int i = 0; i < weekdays.size(); ++i) {
            if (selection[i]) {
                selectedItems.add(weekdays.get(i));
            }
        }

        return selectedItems;
    }
}
