package com.cab302.wellbeing.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class is a controller for the Settings menu in the application.
 * It provides two linked switch scenes set time limits and set modes.
 */
public class SettingController {

    @FXML
    public Button btnMode;
    public Button btnSetTime;
    public Button btnBack;

    @FXML
    private Stage stage;
    public void btnBackOnAction(ActionEvent e){
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }


    /**
     * This method is used to handle the Mode button click event.
     * It switches the scene to the Mode scene.
     * @param event - the action event that triggered the method
     */
    @FXML
    private void handleModeButton(ActionEvent event) {
        switchScene(event, SceneType.MODE);
    }

    /**
     * This method is used to handle the Set Time button click event.
     * It switches the scene to the Set Time scene.
     * @param event - the action event that triggered the method
     */
    @FXML
    private void handleSetTimeButton(ActionEvent event) {
        switchScene(event, SceneType.SETTIME);
    }



    /**
     * This enum is used to define the types of scenes that can be switched to.
     */
    public enum SceneType {
        MODE, SETTIME
    }

    /**
     * This method is used to switch the scene based on the specified scene type.
     * It loads the fxml file for the scene and displays it in a new window.
     * @param event - the action event that triggered the method
     * @param sceneType - the type of scene to switch to
     */
    public void switchScene(ActionEvent event, SceneType sceneType) {
        String fxmlFile = "";
        String title = "TIPS";

        switch (sceneType) {
            case MODE:
                fxmlFile = "/com/cab302/wellbeing/Mode.fxml";
                break;
            case SETTIME:
                fxmlFile = "/com/cab302/wellbeing/SetTimeLimit.fxml";
                break;
            default:
                System.err.println("Unsupported scene type: " + sceneType);
                return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root1));
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
