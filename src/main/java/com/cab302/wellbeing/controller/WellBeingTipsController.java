package com.cab302.wellbeing.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

public class WellBeingTipsController {
    @FXML
    private void handleVideoButton(ActionEvent event) {
        switchScene(event, WellBeingTipsController.SceneType.VIDEO);
    }
    @FXML
    private void handleOtherTipButton(ActionEvent event) {
        switchScene(event, WellBeingTipsController.SceneType.OTHERTIP);
    }

    public enum SceneType {
        VIDEO, OTHERTIP
    }
    public void switchScene(ActionEvent event, WellBeingTipsController.SceneType sceneType) {
        String fxmlFile = "";
        String title = "TIPS";  // Assuming a common title for simplicity, can be adjusted if needed

        switch (sceneType) {
            case VIDEO:
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
            stage.setTitle(title);
            stage.setScene(new Scene(root1));
            stage.setResizable(true);
            stage.getIcons().add(new Image("com/cab302/wellbeing/image/physical-wellbeing.png"));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
