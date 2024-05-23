package com.cab302.wellbeing.model;
import java.util.HashMap;
import java.util.Map;

public class UserSession {
    private static UserSession instance = new UserSession();
    private int currentUserId;
    private String currentfirstName;
    private Map<String, Integer> currentLimits;  // To store current limits like daily time, etc.

    private UserSession() {
        currentLimits = new HashMap<>();
    }

    public static UserSession getInstance() {
        return instance;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void setLimit(String type, int value) {
        currentLimits.put(type, value);
    }

    public int getLimit(String type) {
        return currentLimits.getOrDefault(type, 0);
    }
    public static void setInstance(UserSession userSession) {
        instance = userSession;
    }
    public void setFirstName(String firstName) {
        this.currentfirstName = firstName;
    }
    public String getFirstName() {
        return currentfirstName;
    }
}