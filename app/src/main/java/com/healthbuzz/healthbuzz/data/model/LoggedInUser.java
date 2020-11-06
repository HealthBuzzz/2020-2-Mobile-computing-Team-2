package com.healthbuzz.healthbuzz.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private int id;
    private String name;

    public LoggedInUser(int userId, String displayName) {
        this.id = userId;
        this.name = displayName;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return name;
    }
}