package com.example.journaly.model.users;

import androidx.annotation.Nullable;

import org.parceler.Parcel;

import java.util.List;

@Parcel(Parcel.Serialization.BEAN)
public class Goal {

    //Journal {timesFrequency} times every {daysFrequency} days
    private int timesFrequency;
    private int daysFrequency;

    @Nullable
    private List<String> reminderDays;

    //Hour and minute to send reminder. -1 if no reminder.
    private int reminderHour;
    private int reminderMinute;

    //Contact to message if goal is not met
    @Nullable
    private Contact contact;

    private long createdAt;

    //Unix time for the last time that this goal was failed. Used to avoid failing a goal repeatedly.
    private long lastFailTime;

    public Goal(){
        //empty constructor for parcel
    }

    public Goal(int timesFrequency, int daysFrequency, List<String> reminderDays, int reminderHour, int reminderMinute, @Nullable Contact contact) {
        this.timesFrequency = timesFrequency;
        this.daysFrequency = daysFrequency;
        this.reminderDays = reminderDays;
        this.reminderHour = reminderHour;
        this.reminderMinute = reminderMinute;
        this.lastFailTime = -1;
        this.createdAt = System.currentTimeMillis();

        if (contact == null || !contact.isInValidState()){
            this.contact = null;
            return;
        } else {
            this.contact = contact;
        }
    }

    public int getTimesFrequency() {
        return timesFrequency;
    }

    public void setTimesFrequency(int timesFrequency) {
        this.timesFrequency = timesFrequency;
    }

    public int getDaysFrequency() {
        return daysFrequency;
    }

    public void setDaysFrequency(int daysFrequency) {
        this.daysFrequency = daysFrequency;
    }

    public List<String> getReminderDays() {
        return reminderDays;
    }

    public void setReminderDays(List<String> reminderDays) {
        this.reminderDays = reminderDays;
    }

    public int getReminderHour() {
        return reminderHour;
    }

    public void setReminderHour(int reminderHour) {
        this.reminderHour = reminderHour;
    }

    public int getReminderMinute() {
        return reminderMinute;
    }

    public void setReminderMinute(int reminderMinute) {
        this.reminderMinute = reminderMinute;
    }

    @Nullable
    public Contact getContact() {
        return contact;
    }

    public void setContact(@Nullable Contact contact) {
        if (contact == null || !contact.isInValidState()){
            this.contact = null;
            return;
        }
        this.contact = contact;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastFailTime() {
        return lastFailTime;
    }

    public void setLastFailTime(long lastFailTime) {
        this.lastFailTime = lastFailTime;
    }

    public boolean remindersEnabled(){
        return reminderDays != null && reminderDays.size() > 0 && reminderHour != -1 && reminderMinute != -1;
    }

    public boolean contactMessageEnabled(){
        return this.contact != null;
    }
}
