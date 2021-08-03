package com.example.journaly.goals_screen;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;

import com.example.journaly.databinding.ActivityCreateGoalBinding;

import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener;

public class CreateGoalActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_PHONE_NUMBER = 231;
    private ActivityCreateGoalBinding binding;
    private JournalFrequencyStep journalFrequencyStep;
    private ReminderDaysStep reminderDaysStep;
    private ReminderTimeStep reminderTimeStep;
    private ContactMessageStep contactMessageStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGoalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
    }

    private void initViews(){
        initToolbar();
        initSteps();

        binding.stepperForm.setup(new StepperFormListener() {
            @Override
            public void onCompletedForm() {

            }

            @Override
            public void onCancelledForm() {

            }
        }, new JournalFrequencyStep(), new ReminderDaysStep(), new ReminderTimeStep(), new ContactMessageStep(() -> sendSelectContactIntent())).init();
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

            }

            @Override
            public void onCancelledForm() {

            }
        }, journalFrequencyStep, reminderDaysStep, reminderTimeStep, contactMessageStep);
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
                ContactMessageStep step = (ContactMessageStep) binding.stepperForm.getOpenStep();
                step.setContactPhoneNumber(number);
                step.setContactName(name);
            }
        }

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