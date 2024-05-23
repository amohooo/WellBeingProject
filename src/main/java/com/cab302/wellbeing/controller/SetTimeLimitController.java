package com.cab302.wellbeing.controller;

//import com.cab302.wellbeing.DataBaseConnection;

import com.cab302.wellbeing.AppSettings;
import com.cab302.wellbeing.DataBaseConnection;
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
    @FXML
    public TextField txtHH, txtMM, txtSS;
    @FXML
    private Label lblSetTime, lblActive, lblWhen, lblTitle, lblDot1, lblDot2, lblMsg, lblBkGrd;
    @FXML
    public CheckBox chbActive;
    @FXML
    public RadioButton rdbNotify, rdbAsk, rdbExit;
    @FXML
    private Pane paneSetLimit;
    private DataBaseConnection dbConnection = new DataBaseConnection();
    @FXML
    public Button btnSaveT, btnCancelT;
    private int userId;
    private String firstName;
    private ToggleGroup radioGroup;
    private Timeline countdown;
    public int hours;
    public int minutes;
    public int seconds;
    public boolean active;
    public String limitType;
    public void setTimeLimits(int hours, int minutes, int seconds, boolean active, String limitType) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.active = active;
        this.limitType = limitType;
    }
    private static Color lightColor = Color.web("#bfe7f7");
    private static Color nightColor = Color.web("#777777");
    private static Color autoColor = Color.web("#009ee0");
    private static Color eyeProtectColor = Color.web("#A3CCBE");
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setUserId(int userId) {
        this.userId = userId;
        loadTimeLimits(0, 0, 0, false, "Notify");
    }
    public String accType;
    public void setAccType(String accType) {
        this.accType = accType;
        System.out.println("User account type: " + accType);
    }
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

    private void handleCheckboxAction() {
        boolean isActive = chbActive.isSelected();
        setControlsEnabled(isActive);
    }

    private void handleRadioButtonAction() {
        // Handle radio button actions if needed, currently managed by ToggleGroup
    }

    private void setControlsEnabled(boolean enabled) {
        txtHH.setDisable(!enabled);
        txtMM.setDisable(!enabled);
        txtSS.setDisable(!enabled);
        rdbNotify.setDisable(!enabled);
        rdbAsk.setDisable(!enabled);
        rdbExit.setDisable(!enabled);
        //btnSaveT.setDisable(enabled);
    }

    public void loadTimeLimits(int hours, int minutes, int seconds, boolean active, String limitType) {
        // Set default values to zero if null
        hours = hours == 0 ? 0 : hours;
        minutes = minutes == 0 ? 0 : minutes;
        seconds = seconds == 0 ? 0 : seconds;
        limitType = limitType == null ? "" : limitType;

        txtHH.setText(Integer.toString(hours));
        txtMM.setText(Integer.toString(minutes));
        txtSS.setText(Integer.toString(seconds));

        if (active) {
            chbActive.setSelected(true);
            setControlsEnabled(true);
        } else {
            chbActive.setSelected(false);
            setControlsEnabled(false);
        }

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


    @FXML
    private void saveTimeLimits() {
        Stage stage = (Stage) btnCancelT.getScene().getWindow();

        try {
            if (!chbActive.isSelected()) {
                // Delete the record if chbActive is not selected
                deleteTimeLimits();
                lblMsg.setText("Time limits have been disabled.");
                stage.close();
                switchToMainMenu("saveSetTime");
                return;
            }

            String hhText = txtHH.getText().trim();
            String mmText = txtMM.getText().trim();
            String ssText = txtSS.getText().trim();

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

            int hours = Integer.parseInt(hhText);
            int minutes = Integer.parseInt(mmText);
            int seconds = Integer.parseInt(ssText);

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

            int totalSeconds = (hours * 3600) + (minutes * 60) + seconds;
            boolean active = chbActive.isSelected();
            String limitType = getSelectedLimitType();

            String checkQuery = "SELECT COUNT(*) FROM Limits WHERE UserID = ?";
            String updateQuery = "UPDATE Limits SET LimitType = ?, LimitValue = ?, Active = ? WHERE UserID = ?";
            String insertQuery = "INSERT INTO Limits (UserID, LimitType, LimitValue, Active) VALUES (?, ?, ?, ?)";

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

    private void deleteTimeLimits() {
        String deleteQuery = "DELETE FROM Limits WHERE UserID = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getSelectedLimitType() {
        if (rdbNotify.isSelected()) {
            return "Notify";
        } else if (rdbAsk.isSelected()) {
            return "Ask";
        } else if (rdbExit.isSelected()) {
            return "Exit";
        }
        return "Notify"; // Default to Notify if none are selected
    }
    @FXML
    private void btnCancelTOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) btnCancelT.getScene().getWindow();
        stage.close();
        System.out.println("User account type: " + accType);
        switchToMainMenu("setTime");
    }

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
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
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
}