package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class ModeController {

    @FXML
    private RadioButton lightRadioButton;
    @FXML
    private RadioButton nightRadioButton;
    @FXML
    private RadioButton autoRadioButton;
    @FXML
    private CheckBox eyeProtectCheckBox;
    @FXML
    private Pane paneMode;
    @FXML
    private Label lblBkGrd;

    @FXML
    public Button btnSaveM;

    @FXML
    public Button btnCancelM;
    private int userId;
    private String firstName;

    @FXML
    private ToggleGroup modeGroup;

    @FXML
    public void initialize() {
        setUpEventHandlers();
        applyCurrentMode();
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        System.out.println("firstName: " + firstName);
    }

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("userId: " + userId);
    }
    public String accType;
    public void setAccType(String accType) {
        this.accType = accType;
    }

    private void applyCurrentMode() {
        String currentMode = AppSettings.getCurrentMode();
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.7; // Default to 0% opacity for auto mode

        updateLabelBackgroundColor(opacity);

        switch (currentMode) {
            case AppSettings.MODE_LIGHT:
                lightRadioButton.setSelected(true);
                break;
            case AppSettings.MODE_NIGHT:
                nightRadioButton.setSelected(true);
                break;
            case AppSettings.MODE_AUTO:
                autoRadioButton.setSelected(true);
                break;
            case AppSettings.MODE_EYEPROTECT:
                eyeProtectCheckBox.setSelected(true);
                break;
        }
    }

    private void setUpEventHandlers() {
        eyeProtectCheckBox.setOnAction(event -> handleModeSelection());
        lightRadioButton.setOnAction(event -> handleModeSelection());
        nightRadioButton.setOnAction(event -> handleModeSelection());
        autoRadioButton.setOnAction(event -> handleModeSelection());
    }

    public void handleModeSelection() {
        String currentMode = AppSettings.MODE_AUTO; // Default mode

        if (eyeProtectCheckBox.isSelected()) {
            modeGroup.selectToggle(null);
            currentMode = AppSettings.MODE_EYEPROTECT;
        } else if (lightRadioButton.isSelected()) {
            currentMode = AppSettings.MODE_LIGHT;
        } else if (nightRadioButton.isSelected()) {
            currentMode = AppSettings.MODE_NIGHT;
        } else if (autoRadioButton.isSelected()) {
            currentMode = AppSettings.MODE_AUTO;
        }

        AppSettings.setCurrentMode(currentMode);
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.7; // 0% for auto, 70% for others
        if (eyeProtectCheckBox.isSelected() || lightRadioButton.isSelected() || nightRadioButton.isSelected()){
            applyModeColors();
        } else {
            updateLabelBackgroundColor(0);
        }
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

    @FXML
    public void btnSaveMOnAction(ActionEvent actionEvent) {
        saveSelectedMode();
        switchToMainMenu("Mode");

        Stage stage = (Stage) btnSaveM.getScene().getWindow();
        stage.close();
    }

    private void saveSelectedMode() {
        String selectedMode = AppSettings.getCurrentMode();
        AppSettings.saveModeToDatabase(userId, selectedMode);
    }

    private void switchToMainMenu(String source) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/MainMenu.fxml"));
            Parent root = fxmlLoader.load();
            MainMenuController mainMenuController = fxmlLoader.getController();
            mainMenuController.setFirstName(firstName);
            mainMenuController.setUserId(userId, source);
            mainMenuController.setAccType(accType);
            mainMenuController.applyModeColors();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Main Menu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void btnCancelMOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) btnCancelM.getScene().getWindow();
        stage.setOnHidden(event -> switchToMainMenu("Mode"));
        stage.close();
    }

    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

        if (paneMode != null) {
            paneMode.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
        if (lightRadioButton != null) {
            lightRadioButton.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (nightRadioButton != null) {
            nightRadioButton.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (autoRadioButton != null) {
            autoRadioButton.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (eyeProtectCheckBox != null) {
            eyeProtectCheckBox.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (btnSaveM != null) {
            btnSaveM.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnCancelM != null) {
            btnCancelM.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
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

}