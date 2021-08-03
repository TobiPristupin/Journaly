package com.example.journaly.goals_screen;

import android.content.Intent;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.journaly.R;
import com.example.journaly.model.users.Contact;

import ernestoyaquello.com.verticalstepperform.Step;

public class ContactMessageStep extends Step<Contact> {


    public interface OnSelectContactClickListener {
        void onCLick();
    }

    private Contact contact = null;
    private OnSelectContactClickListener contactClickListener;
    private TextView contactInfoTextView;
    private EditText messageToSendEditText;
    private Button selectContactBtn;


    protected ContactMessageStep(OnSelectContactClickListener onSelectContactClickListener) {
        super("Keep me accountable", "Select one of your contacts, and write an embarrassing message. If you fail to meet your goal, Journaly will automatically send the message to your contact (Optional)");
        this.contactClickListener = onSelectContactClickListener;
    }

    @Override
    public Contact getStepData() {
        return contact;
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return contact == null ? "(Empty)" : contact.getName();
    }

    @Override
    public void restoreStepData(Contact data) {
        this.contact = data;
        if (contact != null){
            messageToSendEditText.setText(contact.getMessageToSend());
            contactInfoTextView.setText(contact.getName());
        }

    }

    @Override
    protected IsDataValid isStepDataValid(Contact stepData) {
        if (stepData == null){ //if we have no data, data is valid since this is an optional field
            return new IsDataValid(true);
        }

        boolean valid = stepData.getName() != null && stepData.getMessageToSend() != null;
        String errorMessage = valid ? "" : "Please fill in all fields";
        return new IsDataValid(valid, errorMessage);
    }

    @Override
    protected View createStepContentLayout() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View root = inflater.inflate(R.layout.step_contact_message, null, false);

        contactInfoTextView = root.findViewById(R.id.contact_info);
        messageToSendEditText = root.findViewById(R.id.contact_message_edittext);
        selectContactBtn = root.findViewById(R.id.select_contact_button);

        selectContactBtn.setOnClickListener(v -> {
            //Since we want to send a contact picker intent here, but that intent requires
            //onActivityResult, we let the calling activity handle the intent.
            contactClickListener.onCLick();
        });

        messageToSendEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (contact == null){
                    contact = new Contact();
                }

                contact.setMessageToSend(s.toString());
            }
        });

        return root;
    }

    /*
    When selecting a contact, a contact intent is sent, which then needs to be caught in onActivityResult.
    a Step class has no onActivityResult, so we allow the activity that owns this step to handle that
    functionality through the OnSelectContactClickListener. Once the activity receives the contact data,
    it will call setContactName and setContactPhoneNumber to pass in the data.
    */
    public void setContactName(String name){
        if (contact == null){
            contact = new Contact();
        }

        contact.setName(name);
        contactInfoTextView.setText(name);
    }

    public void setContactPhoneNumber(String phoneNumber){
        if (contact == null){
            contact = new Contact();
        }

        contact.setPhoneNumber(phoneNumber);
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
