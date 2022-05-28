package com.example.talentspartner.models;

public class Conversation {

    // Properties
    private String messageFrom;
    private String messageTo;
    private String message;
    private String timestamp;

    // Constructors
    public Conversation() {
    }

    public Conversation(String messageFrom, String messageTo, String message, String timestamp) {
        this.messageFrom = messageFrom;
        this.messageTo = messageTo;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }

    public String getMessageTo() {
        return messageTo;
    }

    public void setMessageTo(String messageTo) {
        this.messageTo = messageTo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
