package com.example.journaly.sms;

import android.telephony.SmsManager;

import com.example.journaly.model.users.Contact;

public class SmsSender {

    public static void sendSms(Contact contact){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(contact.getPhoneNumber(), null, contact.getMessageToSend(), null, null);
    }
}
