package com.example.journaly.goals_screen;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.Pair;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;

import com.example.journaly.R;
import com.example.journaly.databinding.ActivityCreateGoalBinding;
import com.example.journaly.model.users.Contact;
import com.example.journaly.model.users.FirebaseUsersRepository;
import com.example.journaly.model.users.Goal;
import com.example.journaly.model.users.UsersRepository;
import com.example.journaly.notifications.AlarmSender;

import java.util.ArrayList;
import java.util.List;

import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener;
import es.dmoral.toasty.Toasty;

public class CreateGoalActivity extends AppCompatActivity {

    public static final String TAG = "CreateGoalActivity";
    private static final int REQUEST_SELECT_PHONE_NUMBER = 231;
    private ActivityCreateGoalBinding binding;
    private JournalFrequencyStep journalFrequencyStep;
    private ReminderDaysStep reminderDaysStep;
    private ReminderTimeStep reminderTimeStep;
    private ContactMessageStep contactMessageStep;


    private UsersRepository usersRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGoalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usersRepository = FirebaseUsersRepository.getInstance();

        initViews();
    }

    private void initViews(){
        initToolbar();
        initSteps();
    }

    private void initSteps() {
        journalFrequencyStep = new JournalFrequencyStep();
        reminderDaysStep = new ReminderDaysStep();
        reminderTimeStep = new ReminderTimeStep();
        contactMessageStep = new ContactMessageStep(() -> {
            sendSelectContactIntent();
        });

        binding.stepperForm.setup(new StepperFormListener() {
            @Override
            public void onCompletedForm() {
                onFormCompleted();
            }

            @Override
            public void onCancelledForm() {

            }
        }, journalFrequencyStep, reminderDaysStep, reminderTimeStep, contactMessageStep).init();
    }

    private void onFormCompleted() {
        showLoadingIndicator();

        Pair<Integer, Integer> frequency = journalFrequencyStep.getStepData();
        int timesFrequency = frequency.first;
        int daysFrequency = frequency.second;

        List<String> daysToRemind = convertBooleanDaysToList(reminderDaysStep.getStepData());

        Pair<Integer, Integer> reminderTime = reminderTimeStep.getStepData();
        int reminderHour = reminderTime.first;
        int reminderMinute = reminderTime.second;

        Contact contactMessage = contactMessageStep.getStepData();

        Goal goal = new Goal(timesFrequency, daysFrequency, daysToRemind, reminderHour, reminderMinute, contactMessage);
        usersRepository.updateGoal(goal).subscribe(() -> {
            new AlarmSender(CreateGoalActivity.this).setAlarms(daysToRemind, reminderHour, reminderMinute);
            hideLoadingIndicator();
            CreateGoalActivity.this.onBackPressed();
        }, throwable -> {
            hideLoadingIndicator();
            Log.w(TAG, throwable);
            Toasty.error(this, "There was an error creating the goal", Toasty.LENGTH_SHORT, true).show();
        });
    }

    private void initToolbar() {
        setSupportActionBar(binding.createGoalToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void sendSelectContactIntent(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
            extractContactInfoAndUpdateStep(data);
        }

    }

    private void extractContactInfoAndUpdateStep(@org.jetbrains.annotations.Nullable Intent data) {
        // Get the URI and query the content provider for the phone number
        Uri contactUri = data.getData();
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Contactables.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
        // If the cursor returned is valid, get the phone number
        if (cursor != null && cursor.moveToFirst()) {
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.DISPLAY_NAME);

            String number = cursor.getString(numberIndex);
            String name = cursor.getString(nameIndex);

            //We assume that if we sent a contact intent, then the current step is ContactMessageStep, so we can safely cast.
            contactMessageStep.setContactPhoneNumber(number);
            contactMessageStep.setContactName(name);
        }
    }

    private List<String> convertBooleanDaysToList(boolean[] weekDays){
        String[] weekDayStrings = getResources().getStringArray(R.array.week_days_extended);
        List<String> selectedWeekDayStrings = new ArrayList<>();
        for (int i = 0; i < weekDayStrings.length; i++) {
            if (weekDays[i]) {
                selectedWeekDayStrings.add(weekDayStrings[i]);
            }
        }

        return selectedWeekDayStrings;
    }

    private void showLoadingIndicator(){
        binding.createGoalProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator(){
        binding.createGoalProgressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}