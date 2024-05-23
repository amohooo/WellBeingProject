package com.cab302.wellbeing.model;

import javafx.beans.property.SimpleStringProperty;

public class UserFeedback {
    private SimpleStringProperty colEmail;
    private SimpleStringProperty colFeedback;

    public UserFeedback(String colEmail, String colFeedback){
        this.colEmail = new SimpleStringProperty(colEmail);
        this.colFeedback = new SimpleStringProperty(colFeedback);
    }

    public String getColEmail(){
        return colEmail.get();
    }

    public void setColEmail (String colEmail){
        this.colEmail = new SimpleStringProperty(colEmail);
    }

    public String getColFeedback(){
        return colFeedback.get();
    }

    public void setColFeedback(String feedback){
        this.colFeedback = new SimpleStringProperty(feedback);
    }

}