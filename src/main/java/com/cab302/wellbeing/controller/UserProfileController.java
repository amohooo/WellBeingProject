package com.cab302.wellbeing.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * This class is a controller for the User Profile functionality in the application.
 * It provides methods to handle the user profile (name, email, password, etc.) of the application.
 */
public class UserProfileController {
    @FXML
    public TextField txtUserName;

    @FXML
    public TextField txtFirstName;

    @FXML
    public TextField txtLastName;

    @FXML
    public TextField txtEmail;

    @FXML
    public TextField txtPassword;

    @FXML
    public Button btnCancel;


    public void displayUserName(String userName){
        this.txtUserName.setText(userName);
    }

    public void displayFirstName(String firstName){
        this.txtFirstName.setText(firstName);
    }

    public void displayLastName(String lastName){
        this.txtLastName.setText(lastName);
    }

    public void displayEmail(String email){
        this.txtEmail.setText(email);
    }

    public void displayPassword(String password){
        this.txtPassword.setText(password);
    }

    public void cancelOnAction(){
        Stage stage = (Stage) txtUserName.getScene().getWindow();
        stage.close();  // Closes the current window
    }
}

