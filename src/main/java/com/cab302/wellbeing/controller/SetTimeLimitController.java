package com.cab302.wellbeing.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * This class is a controller for the Set Time Limit functionality in the application.
 * It provides methods to handle the time limit setting.
 */
public class SetTimeLimitController {
    @FXML
    public Button btnSaveT;
    public Button btnCancelT;


    @FXML
    public void btnSaveTOnAction(ActionEvent actionEvent) {
    }
    @FXML
    public void btnCancelTOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) btnCancelT.getScene().getWindow();
        stage.close();
    }
}
