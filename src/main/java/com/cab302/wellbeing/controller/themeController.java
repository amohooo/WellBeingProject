package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class themeController {

    @FXML
    public ColorPicker clpBkGrd, clpTxt, clpBtn;
    @FXML
    private Button btnBack, btnDft, btnSav;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private Label lblBkGrd, lblButton, lblFont;

    @FXML
    private Label lblBkGrdA;
    private String firstName;

    private static final Color DEFAULT_COLOR = Color.web("#009ee0");
    private static final Color DEFAULT_TEXT_COLOR = Color.web("#ffffff");

    private static Color lightColor = Color.web("#bfe7f7");
    private static Color nightColor = Color.web("#777777");
    private static Color autoColor = Color.web("#009ee0");
    private static Color eyeProtectColor = Color.web("#A3CCBE");
    private int userId; // This will be set dynamically
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String accType;
    public void setAccType(String accType) {
        this.accType = accType;
    }
    private DataBaseConnection dbConnection = new DataBaseConnection();
    private SettingController parentController; // Reference to the parent controller
    private MainMenuController mainMenuController; // Reference to the main menu controller

    public void setUserId(int userId) {
        this.userId = userId;
        //loadSavedColors();
    }

    public void setParentController(SettingController parentController) {
        this.parentController = parentController;
    }

    public void setMainMenuController(MainMenuController mainMenuController) {
        this.mainMenuController = mainMenuController;
    }

    @FXML
    private void initialize() {
        setUpEventHandlers();
    }

    private void setUpEventHandlers() {
        clpBkGrd.setOnAction(this::handleChangeBackgroundColor);
        clpTxt.setOnAction(this::handleChangeTextColor);
        clpBtn.setOnAction(this::handleChangeButtonColor);
        btnBack.setOnAction(this::handleBackButton);
        btnDft.setOnAction(this::handleDefaultColor);
        btnSav.setOnAction(this::handleSaveColors);
    }

    @FXML
    public void handleChangeBackgroundColor(ActionEvent event) {
        if (clpBkGrd != null) {
            Color selectedColor = clpBkGrd.getValue();
            changeBackgroundColor(selectedColor);
            updateParentColors();
        }
    }

    @FXML
    public void handleChangeTextColor(ActionEvent event) {
        if (clpTxt != null) {
            Color selectedColor = clpTxt.getValue();
            changeTextColor(selectedColor);
            updateParentColors();
        }
    }

    @FXML
    public void handleChangeButtonColor(ActionEvent event) {
        if (clpBtn != null) {
            Color selectedColor = clpBtn.getValue();
            changeButtonColor(selectedColor);
            updateParentColors();
        }
    }

    private void changeBackgroundColor(Color color) {
        String hex = getHexColor(color);
        if (mainPane != null) {
            mainPane.setStyle("-fx-background-color: " + hex + ";");
        }
    }

    private void changeTextColor(Color color) {
        String hex = getHexColor(color);

        if (lblBkGrd != null) {
            lblBkGrd.setStyle("-fx-text-fill: " + hex + ";");
        }
        if (lblButton != null) {
            lblButton.setStyle("-fx-text-fill: " + hex + ";");
        }
        if (lblFont != null) {
            lblFont.setStyle("-fx-text-fill: " + hex + ";");
        }
        updateButtonTextColor(hex);
    }

    private void changeButtonColor(Color color) {
        String hex = getHexColor(color);
        if (btnBack != null) {
            btnBack.setStyle("-fx-background-color: " + hex + "; -fx-text-fill: " + getHexColor(clpTxt.getValue()) + ";");
        }
        if (btnDft != null) {
            btnDft.setStyle("-fx-background-color: " + hex + "; -fx-text-fill: " + getHexColor(clpTxt.getValue()) + ";");
        }
        if (btnSav != null) {
            btnSav.setStyle("-fx-background-color: " + hex + "; -fx-text-fill: " + getHexColor(clpTxt.getValue()) + ";");
        }
    }
    private void updateButtonTextColor(String textColorHex) {
        if (btnBack != null) {
            btnBack.setStyle("-fx-text-fill: " + textColorHex + "; -fx-background-color: " + getHexColor(clpBtn.getValue()) + ";");
        }
        if (btnDft != null) {
            btnDft.setStyle("-fx-text-fill: " + textColorHex + "; -fx-background-color: " + getHexColor(clpBtn.getValue()) + ";");
        }
        if (btnSav != null) {
            btnSav.setStyle("-fx-text-fill: " + textColorHex + "; -fx-background-color: " + getHexColor(clpBtn.getValue()) + ";");
        }
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
    public void handleBackButton(ActionEvent event) {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
        switchToMainMenu("theme");
    }

    @FXML
    public void handleDefaultColor(ActionEvent event) {
        loadDefaultColors();
        if (parentController != null) {
            parentController.applyColors(DEFAULT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_COLOR);
        }
        if (mainMenuController != null) {
            mainMenuController.applyColors(DEFAULT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_COLOR);
        }
    }

    @FXML
    public void handleSaveColors(ActionEvent event) {
        saveColorsToDatabase(clpBkGrd.getValue(), clpTxt.getValue(), clpBtn.getValue());
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
        switchToMainMenu("theme");
    }

    private void saveColorsToDatabase(Color backgroundColor, Color textColor, Color buttonColor) {
        if (dbConnection == null) {
            System.err.println("Database connection is null.");
            return;
        }

        // Ensure the UserID exists in the useraccount table
        if (!doesUserExist(userId)) {
            System.err.println("UserID " + userId + " does not exist in useraccount table.");
            return;
        }

        String backgroundColorHex = getHexColor(backgroundColor);
        String textColorHex = getHexColor(textColor);
        String buttonColorHex = getHexColor(buttonColor);

        String updateColorQuery = "INSERT INTO ColorSettings (UserID, BackgroundColor, TextColor, ButtonColor, ButtonTextColor) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "BackgroundColor = VALUES(BackgroundColor), "
                + "TextColor = VALUES(TextColor), "
                + "ButtonColor = VALUES(ButtonColor), "
                + "ButtonTextColor = VALUES(ButtonTextColor)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(updateColorQuery)) {
            statement.setInt(1, userId);
            statement.setString(2, backgroundColorHex);
            statement.setString(3, textColorHex);
            statement.setString(4, buttonColorHex);
            statement.setString(5, textColorHex); // Assuming button text color is same as text color
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean doesUserExist(int userId) {
        String query = "SELECT COUNT(*) FROM useraccount WHERE userId = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    private void loadDefaultColors() {

        changeBackgroundColor(DEFAULT_COLOR);
        changeTextColor(DEFAULT_TEXT_COLOR);
        changeButtonColor(DEFAULT_COLOR);

        clpBkGrd.setValue(DEFAULT_COLOR);
        clpTxt.setValue(DEFAULT_TEXT_COLOR);
        clpBtn.setValue(DEFAULT_COLOR);

    }
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

        if (mainPane != null) {
            mainPane.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
        if (lblBkGrd != null) {
            lblBkGrd.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblButton != null) {
            lblButton.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblFont != null) {
            lblFont.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (btnBack != null) {
            btnBack.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnDft != null) {
            btnDft.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnSav != null) {
            btnSav.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (clpBkGrd != null) {
            clpBkGrd.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (clpTxt != null) {
            clpTxt.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (clpBtn != null) {
            clpBtn.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
    }

    private void updateParentColors() {
        if (parentController != null) {
            parentController.applyColors(clpBkGrd.getValue(), clpTxt.getValue(), clpBtn.getValue());
        }
        if (mainMenuController != null) {
            mainMenuController.applyColors(clpBkGrd.getValue(), clpTxt.getValue(), clpBtn.getValue());
            mainMenuController.applyModeColors();
        }
    }

    public void applyModeColors() {
        if (lblBkGrdA == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode();
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others

        updateLabelBackgroundColor(opacity);
    }

    public void updateLabelBackgroundColor(double opacity) {
        if (lblBkGrdA == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity);
        lblBkGrdA.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";");
    }

    private String toRgbaColor(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }
}
