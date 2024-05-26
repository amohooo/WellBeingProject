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

/**
 * This class is responsible for controlling the theme.
 * It provides functionalities such as changing the theme colors.
 */
public class themeController {
    /**
     * Color pickers for background, text, and button.
     */
    @FXML
    public ColorPicker clpBkGrd, clpTxt, clpBtn; // Color pickers for background, text, and button
    /**
     * Buttons for back, default, and save.
     */
    @FXML
    private Button btnBack, btnDft, btnSav; // Buttons for back, default, and save
    /**
     * Anchor pane for the main theme.
     */
    @FXML
    private AnchorPane mainPane; // Main pane for the theme
    /**
     * Labels for the background, button, and font.
     */
    @FXML
    private Label lblBkGrd, lblButton, lblFont; // Labels for the background, button, and font
    /**
     * Label for the background with opacity.
     */
    @FXML
    private Label lblBkGrdA; // Label for the background with opacity
    private String firstName; // First name of the user
    private static final Color DEFAULT_COLOR = Color.web("#009ee0"); // Default color
    private static final Color DEFAULT_TEXT_COLOR = Color.web("#ffffff"); // Default text color
    private static Color lightColor = Color.web("#bfe7f7"); // Light color
    private static Color nightColor = Color.web("#777777"); // Night color
    private static Color autoColor = Color.web("#009ee0"); // Auto color
    private static Color eyeProtectColor = Color.web("#A3CCBE"); // Eye protect color
    private int userId; // This will be set dynamically
    /**
     * This method is used to parse the user's first name.
     * @param firstName - the first name that parsed from previous scene
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    } // Parse the user's first name

    /**
     * This method is used to parse the user's account type.
     */
    public String accType;

    /**
     * This method is used to set the user's account type.
     * @param accType - the account type that parsed from previous scene
     */
    public void setAccType(String accType) {
        this.accType = accType;
    } // Parse the user's account type
    private DataBaseConnection dbConnection = new DataBaseConnection(); // Database connection
    private SettingController parentController; // Reference to the parent controller
    private MainMenuController mainMenuController; // Reference to the main menu controller

    /**
     * This method is used to parse the user's ID.
     * @param userId - the user ID that parsed from previous scene
     */
    public void setUserId(int userId) {
        this.userId = userId;
        //loadSavedColors();
    }

    /**
     * This method is used to parse the parent controller.
     * @param parentController - the parent controller that parsed from previous scene
     */
    public void setParentController(SettingController parentController) {
        this.parentController = parentController;
    }

    /**
     * This method is used to parse the main menu controller.
     * @param mainMenuController - the main menu controller that parsed from previous scene
     */
    public void setMainMenuController(MainMenuController mainMenuController) {
        this.mainMenuController = mainMenuController;
    }

    /**
     * This method is used initialize the theme controller.
     */
    @FXML
    private void initialize() {
        setUpEventHandlers();
    }

    /**
     * This method is used to load the saved colors from the database.
     */
    private void setUpEventHandlers() {
        clpBkGrd.setOnAction(this::handleChangeBackgroundColor);
        clpTxt.setOnAction(this::handleChangeTextColor);
        clpBtn.setOnAction(this::handleChangeButtonColor);
        btnBack.setOnAction(this::handleBackButton);
        btnDft.setOnAction(this::handleDefaultColor);
        btnSav.setOnAction(this::handleSaveColors);
    }

    /**
     * This method is used to load the handler for the change background color event.
     * @param event - the color to change to
     */
    @FXML
    public void handleChangeBackgroundColor(ActionEvent event) {
        if (clpBkGrd != null) {
            Color selectedColor = clpBkGrd.getValue();
            changeBackgroundColor(selectedColor);
            updateParentColors();
        }
    }

    /**
     * This method is used to handle the change text color event.
     * @param event - the color to change to
     */
    @FXML
    public void handleChangeTextColor(ActionEvent event) {
        if (clpTxt != null) {
            Color selectedColor = clpTxt.getValue();
            changeTextColor(selectedColor);
            updateParentColors();
        }
    }

    /**
     * This method is used to handle the change button color event.
     * @param event - the color to change to
     */
    @FXML
    public void handleChangeButtonColor(ActionEvent event) {
        if (clpBtn != null) {
            Color selectedColor = clpBtn.getValue();
            changeButtonColor(selectedColor);
            updateParentColors();
        }
    }

    /**
     * This method is used to load the saved colors from the database.
     * @param color - the color to change to
     */
    private void changeBackgroundColor(Color color) {
        String hex = getHexColor(color);
        if (mainPane != null) {
            mainPane.setStyle("-fx-background-color: " + hex + ";");
        }
    }

    /**
     * This method is used to load the saved colors from the database.
     * @param color - the color to change to
     */
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
        updateButtonTextColor(hex); // Update button text color
    }

    /**
     * This method is used to load the saved colors from the database.
     * @param color - the color to change to
     */
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
        if (clpBkGrd != null) {
            clpBkGrd.setStyle("-fx-background-color: " + hex + ";");
        }
        if (clpTxt != null) {
            clpTxt.setStyle("-fx-background-color: " + hex + ";");
        }
        if (clpBtn != null) {
            clpBtn.setStyle("-fx-background-color: " + hex + ";");
        }
    }

    /**
     * This method is used to update the button text color.
     * @param textColorHex - the text color to change to
     */
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
     * This method is used to handle the back button event.
     * @param event - the action event that triggered the method
     */
    @FXML
    public void handleBackButton(ActionEvent event) {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
        switchToMainMenu("theme");
    }

    /**
     * This method is used to handle the default color event.
     * @param event - the action event that triggered the method
     */
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

    /**
     * This method is used to handle the save colors event.
     * @param event - the action event that triggered the method
     */
    @FXML
    public void handleSaveColors(ActionEvent event) {
        saveColorsToDatabase(clpBkGrd.getValue(), clpTxt.getValue(), clpBtn.getValue());
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
        switchToMainMenu("theme");
    }

    /**
     * This method is used to saved colors to the database.
     * @param backgroundColor - the background color to save
     * @param textColor - the text color to save
     * @param buttonColor - the button color to save
     */
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

    /**
     * This method is used to check if the user exists in the database.
     * @param userId - the user ID to check
     * @return true if the user exists, false otherwise
     */
    private boolean doesUserExist(int userId) {
        String query = "SELECT COUNT(*) FROM useraccount WHERE userId = ?"; // Query to check if the user exists
        // Try to check if the user exists in the database
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

    /**
     * This method is used to get the hex color from the color.
     * @param color - the color to get the hex color from
     */
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * This method is used to load the default colors.
     */
    private void loadDefaultColors() {

        changeBackgroundColor(DEFAULT_COLOR);
        changeTextColor(DEFAULT_TEXT_COLOR);
        changeButtonColor(DEFAULT_COLOR);

        clpBkGrd.setValue(DEFAULT_COLOR);
        clpTxt.setValue(DEFAULT_TEXT_COLOR);
        clpBtn.setValue(DEFAULT_COLOR);

    }

    /**
     * This method is used to apply the colors.
     * @param backgroundColor - the background color to apply
     * @param textColor - the text color to apply
     * @param buttonColor - the button color to apply
     */
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

    /**
     * This method is used to update the parent colors.
     */
    private void updateParentColors() {
        if (parentController != null) {
            parentController.applyColors(clpBkGrd.getValue(), clpTxt.getValue(), clpBtn.getValue());
        }
        if (mainMenuController != null) {
            mainMenuController.applyColors(clpBkGrd.getValue(), clpTxt.getValue(), clpBtn.getValue());
            mainMenuController.applyModeColors();
        }
    }

    /**
     * This method is used to apply the mode colors.
     */
    public void applyModeColors() {
        if (lblBkGrdA == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode();
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others

        updateLabelBackgroundColor(opacity);
    }

    /**
     * This method is used to update the background color of the label.
     * @param opacity - the opacity of the background color
     */
    public void updateLabelBackgroundColor(double opacity) {
        if (lblBkGrdA == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity);
        lblBkGrdA.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";");
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
