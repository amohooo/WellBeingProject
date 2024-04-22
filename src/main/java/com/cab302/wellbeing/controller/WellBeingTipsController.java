package com.cab302.wellbeing.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * This class is a controller for the WellBeing Tips functionality in the application.
 * It provides methods to handle button clicks and switch scenes.
 */
public class WellBeingTipsController {

    /**
     * This method is used to handle the video button click event.
     * It switches the scene to the video scene.
     * @param event - the action event that triggered the method
     */
    @FXML
    private void handleVideoButton(ActionEvent event) {
        switchScene(event, WellBeingTipsController.SceneType.MEDIA);
    }

    /**
     * This method is used to handle the other tip button click event.
     * It switches the scene to the other tip scene.
     * @param event - the action event that triggered the method
     */
    @FXML
    private void handleOtherTipButton(ActionEvent event) {
        switchScene(event, WellBeingTipsController.SceneType.OTHERTIP);
    }

    /**
     * This enum is used to define the types of scenes that can be switched to.
     */
    public enum SceneType {
        MEDIA, OTHERTIP
    }
    private int userId;
    public void setUserId(int userId) {
        this.userId = userId;  // Now you can use this userId to store browsing data linked to the user
    }

    /**
     * This method is used to switch the scene based on the specified scene type.
     * It loads the fxml file for the scene and displays it in a new window.
     * @param event - the action event that triggered the method
     * @param sceneType - the type of scene to switch to
     */
    public void switchScene(ActionEvent event, WellBeingTipsController.SceneType sceneType) {
        String fxmlFile = "";
        String title = "TIPS";

        switch (sceneType) {
            case MEDIA:
                fxmlFile = "/com/cab302/wellbeing/Media.fxml";
                break;
            case OTHERTIP:
                fxmlFile = "/com/cab302/wellbeing/OtherTips.fxml";
                break;
            default:
                System.err.println("Unsupported scene type: " + sceneType);
                return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            if (sceneType == SceneType.MEDIA) {
                MediaController controller = fxmlLoader.getController();
                controller.setUserId(userId);  // Pass the user ID to the InternetExplorer controller
            }
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