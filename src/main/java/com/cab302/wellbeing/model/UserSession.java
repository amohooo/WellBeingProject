package com.cab302.wellbeing.model;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to store and manage the user session.
 */
public class UserSession {
    private static UserSession instance = new UserSession(); // The user session instance
    private int currentUserId; // The current user ID
    private String currentfirstName; // The current user first name
    private Map<String, Integer> currentLimits;  // To store current limits like daily time, etc.

    /**
     * This method is used to initialize the user session.
     */
    private UserSession() {
        currentLimits = new HashMap<>();
    }

    /**
     * This method is used to get the user session instance.
     * @return the user session instance
     */
    public static UserSession getInstance() {
        return instance;
    }

    /**
     * This method is used to get the current user ID.
     * @return the current user ID
     */
    public int getCurrentUserId() {
        return currentUserId;
    } // Ensure a default value
    /**
     * This method is used to get the user ID.
     * @param userId - the user ID
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    } // Ensure a default value

    /**
     * This method is used to set the user session instance.
     * @param userSession - the user session instance
     */
    public static void setInstance(UserSession userSession) {
        instance = userSession;
    }

    /**
     * This method is used to get the current first name.
     * @param firstName - the current first name
     */
    public void setFirstName(String firstName) {
        this.currentfirstName = firstName;
    }

    /**
     * This method is used to get the current first name.
     * @return the current first name
     */
    public String getFirstName() {
        return currentfirstName;
    }
}