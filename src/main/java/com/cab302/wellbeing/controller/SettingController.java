package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class is a controller for the Settings menu in the application.
 * It provides three linked switch scenes: set time limits, set modes, and set theme.
 */
public class SettingController {
    /**
     * Button for set time, theme, back, mode.
     */
    @FXML
    public Button btnSetTime, btnTheme, btnBack, btnMode; // Buttons for the setting menu
    /**
     * Label for the setting menu.
     */
    @FXML
    public Label lblSetting; // Label for the setting menu
    @FXML
    private AnchorPane paneSetting; // Anchor pane for the setting menu
    @FXML
    private Label lblBkGrd; // Label for the background
    private DataBaseConnection dbConnection = new DataBaseConnection(); // Database connection
    private int userId; // ID of the current user
    private String firstName; // First name of the user
    private String accType; // Account type of the user
    /**
     * int hours.
     */
    public int hours; // Hours for the time limit
    /**
     * int minutes.
     */
    public int minutes; // Minutes for the time limit
    /**
     * int seconds.
     */
    public int seconds; // Seconds for the time limit
    /**
     * boolean active.
     */
    public boolean active; // Active status for the time limit
    /**
     * String limitType.
     */
    public String limitType; // Type of the time limit
    private static final Color DEFAULT_COLOR = Color.web("#009ee0"); // Default color
    private static final Color DEFAULT_TEXT_COLOR = Color.web("#ffffff"); // Default text color

    private static Color lightColor = Color.web("#bfe7f7"); // Light color
    private static Color nightColor = Color.web("#777777"); // Night color
    private static Color autoColor = Color.web("#009ee0"); // Auto color
    private static Color eyeProtectColor = Color.web("#A3CCBE"); // Eye protect color
    private MainMenuController mainMenuController; // Main menu controller

    private static SettingController instance; // Singleton instance

    /**
     * This method is used to get the singleton instance of the SettingController.
     * @return the singleton instance of the SettingController
     */
    public static SettingController getInstance() {
        if (instance == null) {
            instance = new SettingController(); // Create a new instance if it is null
        }
        return instance;
    }

    /**
     * This method is used to set the current user account.
     * @param accType - the account type
     */
    public void setAccType(String accType) {
        this.accType = accType;
    }

    /**
     * This method is used to set the current user firstName.
     * @param firstName - the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * This method is used to set the current user ID.
     * @param userId - the user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("userId: " + userId);
        System.out.println("accType: " + accType);
    }

    /**
     * This method is used to set the back button. when clicking on the back button, it will switch to the main menu.
     * @param e - the action event
     */
    public void btnBackOnAction(ActionEvent e) {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
        switchToMainMenu("Setting");
    }

    /**
     * This method is used to set the mode button. when clicking on the mode button, it will switch to the mode menu.
     * @param event - the action event
     */
    @FXML
    private void handleModeButton(ActionEvent event) {
        switchScene(event, SceneType.MODE);
    }

    /**
     * This method is used to set the time button. when clicking on the time button, it will switch to the time menu.
     * @param event - the action event
     */
    @FXML
    private void handleSetTimeButton(ActionEvent event) {
        switchScene(event, SceneType.SETTIME);
    }

    /**
     * This method is used to set the theme button. when clicking on the theme button, it will switch to the theme menu.
     * @param event - the action event
     */
    @FXML
    private void handleThemeButton(ActionEvent event) {
        switchScene(event, SceneType.THEME);
    }

    /**
     * This method is used to set the time limits.
     * @param hours - the hours
     * @param minutes - the minutes
     * @param seconds - the seconds
     * @param active - the active status
     * @param limitType - the limit type
     */
    public void setTimeLimits(int hours, int minutes, int seconds, boolean active, String limitType) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.active = active;
        this.limitType = limitType;
    }

    /**
     * This enum is used to define the types of scenes that can be switched to.
     */
    public enum SceneType {
        /**
         * The mode scene type.
         */
        MODE,

        /**
         * The set time scene type.
         */
        SETTIME,

        /**
         * The theme scene type.
         */
        THEME
    }

    /**
     * This method is used to switch the scene to the specified scene type.
     * @param event - the action event
     * @param sceneType - the scene type
     */
    public void switchScene(ActionEvent event, SceneType sceneType) {
        String fxmlFile = "";
        String title = "TIPS";
        switch (sceneType) {
            case MODE:
                fxmlFile = "/com/cab302/wellbeing/Mode.fxml"; // Set the fxml file for the mode scene
                break;
            case SETTIME:
                fxmlFile = "/com/cab302/wellbeing/SetTimeLimit.fxml"; // Set the fxml file for the time scene
                break;
            case THEME:
                fxmlFile = "/com/cab302/wellbeing/theme.fxml"; // Set the fxml file for the theme scene
                break;
            default:
                System.err.println("Unsupported scene type: " + sceneType); // Print an error message
                return;
        }
        // Get the background color, text color, and button color
        try {
            Color backgroundColor = (Color) paneSetting.getBackground().getFills().get(0).getFill();
            Color textColor = (Color) btnBack.getTextFill();
            Color buttonColor = (Color) btnBack.getBackground().getFills().get(0).getFill();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();

            // Get the current stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);

            // Pass the user ID and main menu controller if the controller is of the appropriate type
            if (sceneType == SceneType.THEME) {
                themeController controller = fxmlLoader.getController();
                controller.setUserId(userId);
                controller.setParentController(this);
                controller.setFirstName(firstName);
                controller.setAccType(accType);
                controller.setMainMenuController(mainMenuController);
                controller.applyColors(backgroundColor, textColor, buttonColor);
                controller.applyModeColors();
                System.out.println("userId: " + userId);
            }
            if (sceneType == SceneType.MODE) {
                ModeController controller = fxmlLoader.getController();
                controller.setUserId(userId);
                controller.setFirstName(firstName);
                controller.setAccType(accType);
                controller.applyColors(backgroundColor, textColor, buttonColor);
                applyModeColors();
                System.out.println("userId: " + userId);
            }
            if (sceneType == SceneType.SETTIME) {
                SetTimeLimitController controller = fxmlLoader.getController();
                controller.setUserId(userId);
                controller.setFirstName(firstName);
                controller.setAccType(accType);
                System.out.println("accType: " + accType);
                controller.setTimeLimits(hours, minutes, seconds, active, limitType);
                controller.applyColors(backgroundColor, textColor, buttonColor);
                controller.applyModeColors();
                System.out.println("userId: " + userId);
            }

            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method is used to switch to the main menu.
     * @param source - the source of the switch
     */
    private void switchToMainMenu(String source) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/MainMenu.fxml"));
            Parent root = fxmlLoader.load();
            MainMenuController mainMenuController = fxmlLoader.getController();
            mainMenuController.setFirstName(firstName);
            mainMenuController.setUserId(userId, source);
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
     * This method is used to set the main menu controller.
     * @param backgroundColor - the background color
     * @param textColor - the text color
     * @param buttonColor - the button color
     */

    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

        if (paneSetting != null) {
            paneSetting.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
        if (lblSetting != null) {
            lblSetting.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (btnSetTime != null) {
            btnSetTime.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnTheme != null) {
            btnTheme.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnBack != null) {
            btnBack.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnMode != null) {
            btnMode.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
    }

    /**
     * This method is used to get the hex color of the specified color.
     * @param color - the color
     * @return the hex color of the specified color
     */
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * This method is used to apply the color theme based on the current mode.
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

    /**
     * This method is used to update the background color of the label.
     * @param opacity - the opacity
     */
    public void updateLabelBackgroundColor(double opacity) {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity);
        lblBkGrd.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";");
    }

    /**
     * This method is used to convert a Color object to an RGBA color string.
     * @param color - the color
     * @return the RGBA color string
     */
    private String toRgbaColor(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }
}