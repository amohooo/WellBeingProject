package com.cab302.wellbeing.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * This class is a controller for managing the contact information in the application.
 * Currently, it is an empty class and does not contain any methods or fields.
 * Future implementations will include methods for sending messages through "contact us".
 */
public class ContactController {

    /**
     * This method is used to handle the Cancel button click event.
     * It closes the current window.
     */

    @FXML
    private Button btnCancel;
    @FXML
    private Stage stage;
    public void btnCancelOnAction(ActionEvent e){
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
