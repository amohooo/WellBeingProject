package com.cab302.wellbeing.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;


public class MainMenuController{
    @FXML
    public Button btnLogOut;
    @FXML
    public Label lblName;
    private int userId;
    private String firstName;
    @FXML
    private void handleInternetButton(ActionEvent event) {
        switchScene(event, SceneType.INTERNET);
    }
    @FXML
    private void handleReportButton(ActionEvent event) {
        switchScene(event, SceneType.REPORT);
    }
    @FXML
    private void handleWebeButton(ActionEvent event) {
        switchScene(event, SceneType.WEBE);
    }
    @FXML
    private void handleUserProfileButton(ActionEvent event) {
        switchScene(event, SceneType.USER_PROFILE);
    }
    @FXML
    private void handleUserSettingButton(ActionEvent event) {
        switchScene(event, SceneType.SETTING);
    }
    @FXML
    private void handleContactButton(ActionEvent event) {
        switchScene(event, SceneType.CONTACT);
    }

    public void displayName(String firstName) {
        lblName.setText(firstName + ", wish you are having a bright day!");
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public enum SceneType {
        INTERNET, REPORT, WEBE, USER_PROFILE, SETTING, CONTACT
    }
    public void switchScene(ActionEvent event, SceneType sceneType) {
        String fxmlFile = "";
        String title = "Explorer";  // Assuming a common title for simplicity, can be adjusted if needed

        switch (sceneType) {
            case INTERNET:
                fxmlFile = "/com/cab302/wellbeing/InternetExplorer.fxml";
                break;
            case REPORT:
                fxmlFile = "/com/cab302/wellbeing/Report.fxml";
                break;
            case WEBE:
                fxmlFile = "/com/cab302/wellbeing/WellBeingTips.fxml";
                break;
            case USER_PROFILE:
                fxmlFile = "/com/cab302/wellbeing/UserProfile.fxml";
                break;
            case SETTING:
                fxmlFile = "/com/cab302/wellbeing/Setting.fxml";
                break;
            case CONTACT:
                fxmlFile = "/com/cab302/wellbeing/Contact.fxml";
                break;
            default:
                System.err.println("Unsupported scene type: " + sceneType);
                return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            // Check if the scene is INTERNET and then set the user ID
            if (sceneType == SceneType.INTERNET) {
                InternetExplorerController controller = fxmlLoader.getController();
                controller.setUserId(userId);  // Pass the user ID to the InternetExplorer controller
                controller.setFirstName(firstName);  // Pass the user ID to the InternetExplorer controller
            }
            if (sceneType == SceneType.WEBE) {
                WellBeingTipsController controller = fxmlLoader.getController();
                controller.setUserId(userId);  // Pass the user ID
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

//    public void btnLogOutOnAction(ActionEvent e){
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Logout Confirmation");
//        alert.setHeaderText("Logging out");
//        alert.setContentText("Are you sure you want to log out?");
//
//        Optional<ButtonType> result = alert.showAndWait();
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            try {
//                // Close the current window
//                Stage stage = (Stage) btnLogOut.getScene().getWindow();
//                stage.close();
//
//                // Load and display the login page
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/Login.fxml"));
//                Parent root = loader.load();
//                Stage loginStage = new Stage();
//                loginStage.setTitle("Login");
//                loginStage.setScene(new Scene(root));
//                loginStage.show();
//            } catch (IOException ex) {
//                System.err.println("Error loading Login.fxml: " + ex.getMessage());
//                ex.printStackTrace();
//            }
//        }
//    }
    protected Alert createAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }

    public void btnLogOutOnAction(ActionEvent e) {
        Alert alert = createAlert(Alert.AlertType.CONFIRMATION, "Logout Confirmation", "Logging out", "Are you sure you want to log out?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Close the current window
                Stage stage = (Stage) btnLogOut.getScene().getWindow();
                stage.close();

                // Load and display the login page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/Login.fxml"));
                Parent root = loader.load();
                Stage loginStage = new Stage();
                loginStage.setTitle("Login");
                loginStage.setScene(new Scene(root));
                loginStage.show();
            } catch (IOException ex) {
                System.err.println("Error loading Login.fxml: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void closeCurrentWindow() {
        Stage stage = (Stage) btnLogOut.getScene().getWindow();
        stage.close();
    }

}