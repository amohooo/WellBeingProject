package com.cab302.wellbeing.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class is a controller for the WellBeing Tips functionality in the application.
 * It provides methods to handle button clicks and switch scenes.
 */
public class WellBeingTipsController {
    @FXML
    Button btnGoBack, btnVideo, btnOT;
    @FXML
    public Pane paneWebeTip;
    public Label lblBkGrd;
    /**
     * This method is used to handle the video button click event.
     * It switches the scene to the video scene.
     * @param event - the action event that triggered the method
     */
    @FXML
    private void handleVideoButton(ActionEvent event) {
        switchScene(event, SceneType.MEDIA);
    }

    /**
     * This method is used to handle the other tip button click event.
     * It switches the scene to the other tip scene.
     * @param event - the action event that triggered the method
     */
    @FXML
    private void handleOtherTipButton(ActionEvent event) {
        switchScene(event, SceneType.OTHERTIP);
    }

    private String accType;
    public void setUserType(String accType) {
        this.accType = accType;
    }
    /**
     * This enum is used to define the types of scenes that can be switched to.
     */
    public enum SceneType {
        MEDIA, OTHERTIP
    }
    private int userId;
    private String firstName;
    public void setUserId(int userId) {
        this.userId = userId;  // Now you can use this userId to store browsing data linked to the user
        //loadSavedColors();
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
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
        Color backgroundColor = (Color) paneWebeTip.getBackground().getFills().get(0).getFill();
        Color textColor = (Color) btnOT.getTextFill();
        Color buttonColor = (Color) btnGoBack.getBackground().getFills().get(0).getFill();

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
                controller.setUserType(accType);
                controller.applyColors(backgroundColor, textColor, buttonColor);
                controller.applyModeColors();
            }
            if (sceneType == SceneType.OTHERTIP) {
                OtherTipsController controller = fxmlLoader.getController();
                controller.setUserId(userId);  // Pass the user ID to the InternetExplorer controller
                controller.setFirstName(firstName);
                controller.applyModeColors();
            }
            if (sceneType == SceneType.OTHERTIP) {
                OtherTipsController controller = fxmlLoader.getController();
                controller.setUserId(userId);  // Pass the user ID to the InternetExplorer controller
                controller.setFirstName(firstName);
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

    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

        if (btnGoBack != null) {
            btnGoBack.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnVideo != null) {
            btnVideo.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnOT != null) {
            btnOT.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (paneWebeTip != null) {
            paneWebeTip.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
    }

    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    public void applyModeColors() {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode();
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others

        updateLabelBackgroundColor(opacity);
    }

    public void updateLabelBackgroundColor(double opacity) {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity);
        lblBkGrd.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";");
    }

    private String toRgbaColor(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }
    public void btnGoBackOnAction(ActionEvent e){
        Stage stage = (Stage) btnGoBack.getScene().getWindow();
        stage.close();
    }
}