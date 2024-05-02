package com.cab302.wellbeing.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * This class is a controller for the Mode functionality in the application.
 * It provides methods to handle the mode (Dark, Light or Auto) of the application.
 */
public class ModeController {

    @FXML
    public Button btnSaveM;

    @FXML
    public Button btnCancelM;
    
    @FXML
    public void btnSaveMOnAction(ActionEvent actionEvent) {
    }

    @FXML
    public void btnCancelMOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) btnCancelM.getScene().getWindow();
        stage.close();
    }
}
