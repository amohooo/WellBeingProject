package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.animation.Timeline;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is a controller for the Set Time Limit functionality in the application.
 * It provides methods to handle the time limit setting.
 */
public class SetTimeLimitController {
    /**
     * Text fields for hours, minutes, and seconds
     */
    @FXML
    public TextField txtHH, txtMM, txtSS; // Text fields for hours, minutes, and seconds
    /**
     * Labels for the lblSetTime, lblActive, lblWhen, lblTitle, lblDot1, lblDot2, lblMsg, lblBkGrd
     */
    @FXML
    private Label lblSetTime;
    @FXML
    private Label lblActive;
    @FXML
    private Label lblWhen;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblDot1;
    @FXML
    private Label lblDot2;
    /**
     * Label for the message
     */
    @FXML
    public Label lblMsg;
    @FXML
    private Label lblBkGrd; // Label for the set time
    /**
     * Check box for the active status
     */
    @FXML
    public CheckBox chbActive; // Check box for the active status
    /**
     * Radio buttons for the limit type
     */
    @FXML
    public RadioButton rdbNotify, rdbAsk, rdbExit; // Radio buttons for the limit type
    /**
     * Pane for the set limit
     */
    @FXML
    private Pane paneSetLimit; // Pane for the set limit
    /**
     * Database connection
     */
    private DataBaseConnection dbConnection = new DataBaseConnection(); // Database connection
    /**
     * Buttons for the save and cancel
     */
    @FXML
    public Button btnSaveT, btnCancelT; // Buttons for saving and cancelling the time limit
    private int userId; // ID of the current user
    private String firstName; // First name of the user
    private ToggleGroup radioGroup; // Toggle group for the radio buttons
    private Timeline countdown; // Timeline for the countdown
    /**
     * Integer for hours
     */
    public int hours; // Hours for the time limit
    /**
     * Integer for minutes
     */
    public int minutes; // Minutes for the time limit
    /**
     * Integer for seconds
     */
    public int seconds; // Seconds for the time limit
    /**
     * Boolean for active
     */
    public boolean active; // Active status for the time limit
    /**
     * String for limit type
     */
    public String limitType; // Limit type for the time limit

    /**
     * This method is used to parse the time limits.
     * @param hours - the hours that parsed from previous scene
     * @param minutes - the minutes that parsed from previous scene
     * @param seconds - the seconds that parsed from previous scene
     * @param active - the active status that parsed from previous scene
     * @param limitType - the limit type that parsed from previous scene
     */
    public void setTimeLimits(int hours, int minutes, int seconds, boolean active, String limitType) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.active = active;
        this.limitType = limitType;
    }
    private static Color lightColor = Color.web("#bfe7f7"); // Color for light mode
    private static Color nightColor = Color.web("#777777"); // Color for night mode
    private static Color autoColor = Color.web("#009ee0"); // Color for auto mode
    private static Color eyeProtectColor = Color.web("#A3CCBE"); // Color for eye protection mode

    /**
     * This method is used to set the first name of the user.
     * @param firstName - the first name that parsed from previous scene
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * This method is used to set the ID of the user.
     * @param userId - the user ID that parsed from previous scene
     */
    public void setUserId(int userId) {
        this.userId = userId;
        loadTimeLimits(0, 0, 0, false, "Notify");
    }
    /**
     * This method is used to set the account type of the user.
     */
    public String accType; // Account type of the user

    /**
     * This method is used to set the account type of the user.
     * @param accType - the account type that parsed from previous scene
     */
    public void setAccType(String accType) {
        this.accType = accType;
        System.out.println("User account type: " + accType);
    }

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Group radio buttons
        radioGroup = new ToggleGroup();
        rdbNotify.setToggleGroup(radioGroup);
        rdbAsk.setToggleGroup(radioGroup);
        rdbExit.setToggleGroup(radioGroup);
        rdbNotify.setSelected(true); // Set default selection

        // Add listeners for the checkbox and radio buttons
        chbActive.setOnAction(event -> handleCheckboxAction());
        rdbNotify.setOnAction(event -> handleRadioButtonAction());
        rdbAsk.setOnAction(event -> handleRadioButtonAction());
        rdbExit.setOnAction(event -> handleRadioButtonAction());
    }

    /**
     * This method is used to handle the checkbox action.
     */
    private void handleCheckboxAction() {
        boolean isActive = chbActive.isSelected();
        setControlsEnabled(isActive);
    }

    /**
     * This method is used to handle the radio button action.
     * now managed by ToggleGroup
     */
    private void handleRadioButtonAction() {
        // Handle radio button actions if needed, currently managed by ToggleGroup
    }

    /**
     * This method is used to set the controls enabled or disabled.
     * @param enabled - the boolean value to set the controls enabled or disabled
     */
    private void setControlsEnabled(boolean enabled) {
        txtHH.setDisable(!enabled);
        txtMM.setDisable(!enabled);
        txtSS.setDisable(!enabled);
        rdbNotify.setDisable(!enabled);
        rdbAsk.setDisable(!enabled);
        rdbExit.setDisable(!enabled);
        //btnSaveT.setDisable(enabled);
    }

    /**
     * This method is used to load the time limits.
     * @param hours - the hours that parsed from previous scene
     * @param minutes - the minutes that parsed from previous scene
     * @param seconds - the seconds that parsed from previous scene
     * @param active - the active status that parsed from previous scene
     * @param limitType - the limit type that parsed from previous scene
     */
    public void loadTimeLimits(int hours, int minutes, int seconds, boolean active, String limitType) {
        // Set default values to zero if null
        hours = hours == 0 ? 0 : hours; // Set default value to zero if null
        minutes = minutes == 0 ? 0 : minutes; // Set default value to zero if null
        seconds = seconds == 0 ? 0 : seconds; // Set default value to zero if null
        limitType = limitType == null ? "" : limitType; // Set default value to empty if null

        txtHH.setText(Integer.toString(hours)); // Set the hours
        txtMM.setText(Integer.toString(minutes)); // Set the minutes
        txtSS.setText(Integer.toString(seconds)); // Set the seconds
        // Set the active status and limit type, and enable or disable the controls else set to default
        if (active) {
            chbActive.setSelected(true);
            setControlsEnabled(true);
        } else {
            chbActive.setSelected(false);
            setControlsEnabled(false);
        }
        // Set the limit type
        switch (limitType) {
            case "Notify":
                rdbNotify.setSelected(true);
                break;
            case "Ask":
                rdbAsk.setSelected(true);
                break;
            case "Exit":
                rdbExit.setSelected(true);
                break;
            default:
                lblMsg.setText("Select a limit type");
                break;
        }
    }

    /**
     * This method is used to save the time limits.
     */
    @FXML
    public void saveTimeLimits() {
        Stage stage = (Stage) btnCancelT.getScene().getWindow(); // Get the current stage
        // Check if the active status is not selected
        try {
            if (!chbActive.isSelected()) {
                // Delete the record if chbActive is not selected
                deleteTimeLimits();
                lblMsg.setText("Time limits have been disabled.");
                stage.close();
                switchToMainMenu("saveSetTime");
                return;
            }

            String hhText = txtHH.getText().trim(); // Get the hours text
            String mmText = txtMM.getText().trim(); // Get the minutes text
            String ssText = txtSS.getText().trim(); // Get the seconds text

            if (hhText.isEmpty()) {
                hhText = "0";
            }
            if (mmText.isEmpty()) {
                mmText = "0";
            }
            if (ssText.isEmpty()) {
                ssText = "0";
            }

            if (hhText.length() > 2 || mmText.length() > 2 || ssText.length() > 2) {
                lblMsg.setText("Please enter a valid time");
                return;
            }

            int hours = Integer.parseInt(hhText); // Parse the hours
            int minutes = Integer.parseInt(mmText); // Parse the minutes
            int seconds = Integer.parseInt(ssText); // Parse the seconds
            // Check if the hours, minutes, and seconds are valid
            if (hours < 0 || hours > 23) {
                lblMsg.setText("Hours must be between 0 and 23");
                return;
            } else if (minutes < 0 || minutes > 59) {
                lblMsg.setText("Minutes must be between 0 and 59");
                return;
            } else if (seconds < 0 || seconds > 59) {
                lblMsg.setText("Seconds must be between 0 and 59");
                return;
            }

            int totalSeconds = (hours * 3600) + (minutes * 60) + seconds; // Calculate the total seconds
            boolean active = chbActive.isSelected(); // Get the active status
            String limitType = getSelectedLimitType(); // Get the limit type

            String checkQuery = "SELECT COUNT(*) FROM Limits WHERE UserID = ?"; // Query to check if the record exists
            String updateQuery = "UPDATE Limits SET LimitType = ?, LimitValue = ?, Active = ? WHERE UserID = ?"; // Query to update the record
            String insertQuery = "INSERT INTO Limits (UserID, LimitType, LimitValue, Active) VALUES (?, ?, ?, ?)"; // Query to insert the record
            // Try to update or insert the record
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                 PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                 PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

                checkStmt.setInt(1, userId);

                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Record exists, perform update
                    updateStmt.setString(1, limitType);
                    updateStmt.setInt(2, totalSeconds);
                    updateStmt.setBoolean(3, active);
                    updateStmt.setInt(4, userId);
                    updateStmt.executeUpdate();
                } else {
                    // Record does not exist, perform insert
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, limitType);
                    insertStmt.setInt(3, totalSeconds);
                    insertStmt.setBoolean(4, active);
                    insertStmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            lblMsg.setText("Please type in a valid time");
        }
        stage.close();
        switchToMainMenu("saveSetTime");
    }

    /**
     * This method is used to delete the time limits.
     */
    private void deleteTimeLimits() {
        String deleteQuery = "DELETE FROM Limits WHERE UserID = ?"; // Query to delete the record
        // Try to delete the record
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to get the selected limit type.
     * @return the selected limit type
     */
    private String getSelectedLimitType() {
        // Return the selected limit type
        if (rdbNotify.isSelected()) {
            return "Notify";
        } else if (rdbAsk.isSelected()) {
            return "Ask";
        } else if (rdbExit.isSelected()) {
            return "Exit";
        }
        return "Notify"; // Default to Notify if none are selected
    }

    /**
     * This method is used to cancel the time limits.
     */
    @FXML
    private void btnCancelTOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) btnCancelT.getScene().getWindow();
        stage.close();
        System.out.println("User account type: " + accType);
        switchToMainMenu("setTime");
    }

    /**
     * This method is used to apply the colors to the components.
     * @param backgroundColor - the background color to apply
     * @param textColor - the text color to apply
     * @param buttonColor - the button color to apply
     */
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

        if (paneSetLimit != null) {
            paneSetLimit.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
        if (lblSetTime != null) {
            lblSetTime.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblSetTime != null) {
            lblSetTime.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblWhen != null) {
            lblWhen.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblTitle != null) {
            lblTitle.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblActive != null) {
            lblActive.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblDot1 != null) {
            lblDot1.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblDot2 != null) {
            lblDot2.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (chbActive != null) {
            chbActive.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (rdbNotify != null) {
            rdbNotify.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (rdbAsk != null) {
            rdbAsk.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (rdbExit != null) {
            rdbExit.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (btnSaveT != null) {
            btnSaveT.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnCancelT != null) {
            btnCancelT.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
    }

    /**
     * This method is used to get the hex color.
     * @param color - the color to get the hex value
     * @return the hex value of the color
     */
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * This method is used to apply the color theme based on the current mode.
     */
    private void switchToMainMenu(String source) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/MainMenu.fxml"));
            Parent root = fxmlLoader.load();
            MainMenuController mainMenuController = fxmlLoader.getController();
            mainMenuController.setFirstName(firstName);
            mainMenuController.setUserId(userId, source);
            System.out.println("User account type: " + accType);
            mainMenuController.setAccTypeToMain(accType);
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
     * This method is used to apply the color theme based on the current mode.
     */
    public void applyModeColors() {
        // Get the current mode
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode(); // Get the current mode
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others
        updateLabelBackgroundColor(opacity); // Update the label background color
    }

    /**
     * This method is used to update the background color of the label.
     * @param opacity - the opacity of the background color
     */
    public void updateLabelBackgroundColor(double opacity) {
        // Update the background color of the label
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity);
        lblBkGrd.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";");
    }

    /**
     * This method is used to convert a Color object to an RGBA color string.
     * @param color - the Color object to convert
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