package com.cab302.wellbeing.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

/**
 * This class is a controller for the User Agreement functionality in the application.
 * It provides methods to handle the user agreement display.
 */
public class UserAgreementController {
    @FXML
    Button btnAgree; // Button to agree to the user agreement
    @FXML
    Button btnCanc; // Button to cancel the user agreement
    private CheckBox registerCheckbox; // Checkbox to register the user agreement

    /**
     * This method is used to set the checkbox to register the user agreement.
     * @param registerCheckbox - the checkbox to register the user agreement
     */
    public void setRegisterCheckbox(CheckBox registerCheckbox) {
        this.registerCheckbox = registerCheckbox;
    } // Set the checkbox to register the user agreement

    /**
     * This method is used to initialize the user agreement controller.
     */
    @FXML
    public void initialize() {
        btnAgree.setOnAction(e -> agreeAndClose());
        btnCanc.setOnAction(e -> closeWindow());
    }

    /**
     * This method is used to handle the agree button click event.
     * It sets the checkbox to selected and closes the current stage.
     */
    private void agreeAndClose() {
        if (registerCheckbox != null) {
            registerCheckbox.setSelected(true);
        }
        Stage stage = (Stage) btnAgree.getScene().getWindow();
        stage.close();
    }

    /**
     * This method is used to handle the cancel button click event.
     * It closes the current stage.
     */
    private void closeWindow() {
        if (registerCheckbox != null) {
            registerCheckbox.setSelected(false);
        }
        Stage stage = (Stage) btnAgree.getScene().getWindow();
        stage.close();
    }
}


