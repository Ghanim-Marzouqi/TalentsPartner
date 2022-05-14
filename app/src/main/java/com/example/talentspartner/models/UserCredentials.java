package com.example.talentspartner.models;

public class UserCredentials {

    // Properties
    private String email;
    private String password;
    private boolean isRemembered;

    // Constructors
    public UserCredentials() {
    }

    public UserCredentials(String email, String password, boolean isRemembered) {
        this.email = email;
        this.password = password;
        this.isRemembered = isRemembered;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRemembered() {
        return isRemembered;
    }

    public void setRemembered(boolean remembered) {
        isRemembered = remembered;
    }
}
