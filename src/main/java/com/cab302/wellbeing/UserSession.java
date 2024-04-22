package com.cab302.wellbeing;

//public class UserSession {
//    private static UserSession instance = new UserSession();
//    private int currentUserId;
//
//    private UserSession() {}  // Private constructor to prevent external instantiation
//
//    public static UserSession getInstance() {
//        return instance;
//    }
//
//    public int getCurrentUserId() {
//        return currentUserId;
//    }
//
//    public void setCurrentUserId(int userId) {
//        this.currentUserId = userId;
//    }
//}

//package com.cab302.wellbeing;
//
import java.util.HashMap;
import java.util.Map;

public class UserSession {
    private static UserSession instance = new UserSession();
    private int currentUserId;
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
}