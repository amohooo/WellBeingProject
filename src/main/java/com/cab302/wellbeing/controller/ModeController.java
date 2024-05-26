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

/**
 * This class is responsible for controlling the mode settings.
 * It provides functionalities such as saving the selected mode and applying color themes.
 */
public class ModeController {

    @FXML
    private RadioButton lightRadioButton; // Radio button for light mode
    @FXML
    private RadioButton nightRadioButton; // Radio button for night mode
    @FXML
    private RadioButton autoRadioButton; // Radio button for auto mode
    @FXML
    private CheckBox eyeProtectCheckBox; // Check box for eye protection mode
    @FXML
    private Pane paneMode; // Pane for the mode
    @FXML
    private Label lblBkGrd; // Label for the background
    /**
     * Button for save mode.
     */
    @FXML
    public Button btnSaveM; // Button for saving the mode
    /**
     * Button for cancel mode.
     */
    @FXML
    public Button btnCancelM; // Button for cancelling the mode
    private int userId; // ID of the current user
    private String firstName; // First name of the user
    @FXML
    private ToggleGroup modeGroup; // Toggle group for the mode

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        setUpEventHandlers(); // Set up event handlers
        applyCurrentMode(); // Apply the current mode
    }

    /**
     * Sets the first name of the user.
     *
     * @param firstName The first name of the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName; // Parse the first name
        System.out.println("firstName: " + firstName); // Print the first name
    }

    /**
     * Sets the ID of the user.
     *
     * @param userId The ID of the user
     */
    public void setUserId(int userId) {
        this.userId = userId; // Parse the user ID
        System.out.println("userId: " + userId); // Print the user ID
    }
    /**
     * The account type of the user.
     */
    public String accType; // Account type of the user

    /**
     * Sets the account type of the user.
     *
     * @param accType The account type of the user
     */
    public void setAccType(String accType) {
        this.accType = accType;
    }

    /**
     * Applies the current mode.
     */
    private void applyCurrentMode() {
        String currentMode = AppSettings.getCurrentMode(); // Get the current mode
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.7; // Default to 0% opacity for auto mode
        updateLabelBackgroundColor(opacity); // Update the label background color
        // Select the appropriate radio button based on the current mode
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

    /**
     * Sets up the event handlers.
     */
    private void setUpEventHandlers() {
        eyeProtectCheckBox.setOnAction(event -> handleModeSelection()); // Handle the mode selection
        lightRadioButton.setOnAction(event -> handleModeSelection()); // Handle the mode selection
        nightRadioButton.setOnAction(event -> handleModeSelection()); // Handle the mode selection
        autoRadioButton.setOnAction(event -> handleModeSelection()); // Handle the mode selection
    }

    /**
     * Handles the mode selection.
     */
    public void handleModeSelection() {
        String currentMode = AppSettings.MODE_AUTO; // Default mode
        // Determine the selected mode
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
        AppSettings.setCurrentMode(currentMode); // Set the current mode
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.7; // 0% for auto, 70% for others
        // Update the label background color
        if (eyeProtectCheckBox.isSelected() || lightRadioButton.isSelected() || nightRadioButton.isSelected()){
            applyModeColors();
        } else {
            updateLabelBackgroundColor(0);
        }
    }

    /**
     * Updates the background color of the label.
     *
     * @param opacity The opacity of the color
     */
    public void updateLabelBackgroundColor(double opacity) {
        // Update the background color of the label
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity); // Get the background color
        lblBkGrd.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";"); // Set the background color
    }

    /**
     * Converts a Color object to an RGBA color string.
     *
     * @param color The Color object
     * @return The RGBA color string
     */
    private String toRgbaColor(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }

    /**
     * Applies the color theme based on the current mode.
     * @param actionEvent save mode action event
     */
    @FXML
    public void btnSaveMOnAction(ActionEvent actionEvent) {
        saveSelectedMode(); // Save the selected mode
        switchToMainMenu("Mode"); // Switch to the main menu

        Stage stage = (Stage) btnSaveM.getScene().getWindow();
        stage.close();
    }

    /**
     * Saves the selected mode.
     */
    private void saveSelectedMode() {
        String selectedMode = AppSettings.getCurrentMode(); // Get the selected mode
        AppSettings.saveModeToDatabase(userId, selectedMode); // Save the mode to the database
    }

    /**
     * Switches to the main menu.
     *
     * @param source The source of the switch
     */
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

    /**
     * Cancels the mode selection.
     *
     * @param actionEvent The action event
     */
    @FXML
    public void btnCancelMOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) btnCancelM.getScene().getWindow();
        stage.setOnHidden(event -> switchToMainMenu("Mode"));
        stage.close();
    }

    /**
     * Applies the color theme based on the current mode.
     * @param backgroundColor The background color
     * @param textColor The text color
     * @param buttonColor The button color
     */
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

    /**
     * Converts a Color object to a hex color string.
     *
     * @param color The Color object
     * @return The hex color string
     */
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * Applies the color theme based on the current mode.
     */
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