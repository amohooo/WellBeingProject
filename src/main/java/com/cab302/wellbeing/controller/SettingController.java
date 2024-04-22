package com.cab302.wellbeing.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingController {
    @FXML
    private void handleModeButton(ActionEvent event) {
        switchScene(event, com.cab302.wellbeing.controller.SettingController.SceneType.MODE);
    }
    @FXML
    private void handleSetTimeButton(ActionEvent event) {
        switchScene(event, com.cab302.wellbeing.controller.SettingController.SceneType.SETTIME);
    }

    public enum SceneType {
        MODE, SETTIME
    }
    public void switchScene(ActionEvent event, com.cab302.wellbeing.controller.SettingController.SceneType sceneType) {
        String fxmlFile = "";
        String title = "TIPS";  // Assuming a common title for simplicity, can be adjusted if needed

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


