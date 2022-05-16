package com.example.talentspartner.models;

public class Friendship {

    // Properties
    private String userId;
    private String partnerId;
    private boolean hasFriendship;

    // Constructors
    public Friendship() {
    }

    public Friendship(String userId, String partnerId, boolean hasFriendship) {
        this.userId = userId;
        this.partnerId = partnerId;
        this.hasFriendship = hasFriendship;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public boolean isHasFriendship() {
        return hasFriendship;
    }

    public void setHasFriendship(boolean hasFriendship) {
        this.hasFriendship = hasFriendship;
    }
}
