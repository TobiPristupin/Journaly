package com.example.journaly.model.users;

public class Contact {

    private String name;
    private String phoneNumber;

    //message to send to this contact when the user does not meets their goal
    private String messageToSend;

    public Contact(){

    }

    public Contact(String name, String phoneNumber, String messageToSend) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.messageToSend = messageToSend;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessageToSend() {
        return messageToSend;
    }

    public void setMessageToSend(String messageToSend) {
        this.messageToSend = messageToSend;
    }
}
