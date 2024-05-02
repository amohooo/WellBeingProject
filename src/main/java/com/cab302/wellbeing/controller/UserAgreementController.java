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
    Button btnAgree;
    @FXML
    Button btnCanc;
    private CheckBox registerCheckbox;

    public void setRegisterCheckbox(CheckBox registerCheckbox) {
        this.registerCheckbox = registerCheckbox;
    }
    @FXML
    public void initialize() {
        btnAgree.setOnAction(e -> agreeAndClose());
        btnCanc.setOnAction(e -> closeWindow());
    }

    private void agreeAndClose() {
        if (registerCheckbox != null) {
            registerCheckbox.setSelected(true);
        }
        Stage stage = (Stage) btnAgree.getScene().getWindow();
        stage.close();
    }

    private void closeWindow() {
        if (registerCheckbox != null) {
            registerCheckbox.setSelected(false);
        }
        Stage stage = (Stage) btnAgree.getScene().getWindow();
        stage.close();
    }
}


