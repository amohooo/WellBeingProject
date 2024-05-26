package com.cab302.wellbeing.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * This class is used to store and manage user feedback.
 */
public class UserFeedback {
    private SimpleStringProperty colEmail; // The email of the user
    private SimpleStringProperty colFeedback; // The feedback of the user

    /**
     * This method is used to initialize the user feedback.
     * @param colEmail - the email of the user
     * @param colFeedback - the feedback of the user
     */
    public UserFeedback(String colEmail, String colFeedback){
        this.colEmail = new SimpleStringProperty(colEmail);
        this.colFeedback = new SimpleStringProperty(colFeedback);
    }
}