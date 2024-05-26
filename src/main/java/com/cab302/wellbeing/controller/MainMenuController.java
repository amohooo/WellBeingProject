package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * This class is responsible for controlling the main menu.
 * It provides functionalities such as switching scenes, logging out, and applying color themes.
 */
public class MainMenuController {
    /**
     * Button for btnLogOut, btnExplorer, btnReport, btnWebe, btnUser, btnSetting, btnContact
     */
    @FXML
    public Button btnLogOut, btnExplorer, btnReport, btnWebe, btnUser, btnSetting, btnContact; // Buttons for logging out and switching scenes
    /**
     * Label for lblName
     */
    @FXML
    public Label lblName; // Label for the user's name
    /**
     * Pane for paneMenu
     */
    @FXML
    public Pane paneMenu; // Pane for the menu
    /**
     * Label for lblBkGrd
     */
    @FXML
    private Label lblBkGrd; // Label for the background
    private int userId; // ID of the current user
    private int remainingTime; // Remaining time in seconds
    private String firstName; // First name of the user
    private Timeline countdown; // Timeline for the countdown
    private Timeline notificationTimeline; // Timeline for the notification
    private int totalSeconds; // Total seconds for the countdown
    private boolean notifySelected; // Whether the notification option is selected
    private boolean askSelected; // Whether the ask option is selected
    private boolean exitSelected; // Whether the exit option is selected
    private String limitType; // Type of the time limit
    private int limitValue; // Value of the time limit
    private boolean active; // Whether the time limit is active

    private DataBaseConnection dbConnection = new DataBaseConnection(); // Database connection
    private String accType; // Account type of the user
    private static final Color DEFAULT_COLOR = Color.web("#009ee0"); // Default color for the menu
    private static final Color DEFAULT_TEXT_COLOR = Color.web("#ffffff"); // Default text color for the menu
    private static MainMenuController instance; // Singleton instance

    /**
     * Returns the singleton instance of the MainMenuController.
     *
     * @return The singleton instance of the MainMenuController
     */
    public static MainMenuController getInstance() {
        if (instance == null) {
            instance = new MainMenuController();
        }
        return instance;
    }

    /**
     * Handles the Internet button action.
     * Switch to the Internet scene.
     * @param event The action event
     */
    @FXML
    private void handleInternetButton(ActionEvent event) {
        switchScene(event, SceneType.INTERNET);
    }

    /**
     * Handles the Report button action.
     * Switch to the Report scene.
     * @param event The action event
     */
    @FXML
    private void handleReportButton(ActionEvent event) {
        switchScene(event, SceneType.REPORT);
    }

    /**
     * Handles the Wellbeing Tips button action.
     * Switch to the Wellbeing Tips scene.
     * @param event The action event
     */
    @FXML
    private void handleWebeButton(ActionEvent event) {
        switchScene(event, SceneType.WEBE);
    }

    /**
     * Handles the User Profile button action.
     * Switch to the User Profile scene.
     * @param event The action event
     */
    @FXML
    private void handleUserProfileButton(ActionEvent event) {
        switchScene(event, SceneType.USER_PROFILE);
    }

    /**
     * Handles the Setting button action.
     * Switch to the Setting scene.
     * @param event The action event
     */
    @FXML
    private void handleUserSettingButton(ActionEvent event) {
        switchScene(event, SceneType.SETTING);
    }

    /**
     * Handles the Contact button action.
     * Switch to the Contact scene.
     * @param event The action event
     */
    @FXML
    private void handleContactButton(ActionEvent event) {
        switchScene(event, SceneType.CONTACT);
    }

    /**
     * Initializes the main menu.
     */
    private void loadRemainingTime() {
        String query = "SELECT LimitType, LimitValue, Active, RemainingTime FROM Limits WHERE UserID = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                limitType = rs.getString("LimitType");
                limitValue = rs.getInt("LimitValue");
                active = rs.getBoolean("Active");
                remainingTime = rs.getInt("RemainingTime");

                totalSeconds = limitValue;
                notifySelected = "Notify".equals(limitType);
                askSelected = "Ask".equals(limitType);
                exitSelected = "Exit".equals(limitType);

                if (active) {
                    if (remainingTime > 0) {
                        resumeCountdownTimer();
                    } else {
                        startCountdownTimer(totalSeconds, active);
                    }
                }
            } else {
                limitType = null;
                limitValue = 0;
                active = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the time limits from the database.
     */
    private void loadTimeLimits() {
        String query = "SELECT LimitType, LimitValue, Active FROM Limits WHERE UserID = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                limitType = rs.getString("LimitType");
                limitValue = rs.getInt("LimitValue");
                active = rs.getBoolean("Active");

                totalSeconds = limitValue;
                notifySelected = "Notify".equals(limitType);
                askSelected = "Ask".equals(limitType);
                exitSelected = "Exit".equals(limitType);

                // Stop any existing timer before starting a new one

                stopCountdownTimer();
                if (active) {
                    startCountdownTimer(totalSeconds, active);
                }
            } else {
                limitType = null;
                limitValue = 0;
                active = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pauses the countdown timer.
     */
    private void pauseCountdownTimer() {
        if (countdown != null) {
            remainingTime = totalSeconds;
            countdown.stop();
            updateRemainingTimeInDatabase(remainingTime); // Save remaining time to the database
        }
        if (notificationTimeline != null) {
            notificationTimeline.stop();
        }
    }

    /**
     * Updates the remaining time in the database.
     * @param remainingTime The remaining time in seconds
     */
    private void updateRemainingTimeInDatabase(int remainingTime) {
        String updateQuery = "UPDATE Limits SET RemainingTime = ? WHERE UserID = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setInt(1, remainingTime);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resumes the countdown timer.
     */
    private void resumeCountdownTimer() {
        startCountdownTimer(remainingTime, active);
    }

    /**
     * Stops the countdown timer.
     */
    private void stopCountdownTimer() {
        if (countdown != null) {
            countdown.stop();
            countdown = null;
        }
        if (notificationTimeline != null) {
            notificationTimeline.stop();
            notificationTimeline = null;
        }
    }

    /**
     * Starts the countdown timer.
     * @param initialTotalSeconds The initial total seconds
     * @param active Whether the timer is active
     */
    private void startCountdownTimer(int initialTotalSeconds, boolean active) {
        if (!active) {
            return;
        }

        totalSeconds = initialTotalSeconds;
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (totalSeconds <= 0) {
                // Time is up, handle the notification
                if (notifySelected) {
                    showNotification();
                } else if (askSelected) {
                    showAskDialog();
                } else if (exitSelected) {
                    System.exit(0);
                }
                countdown.stop(); // Stop the countdown timer
            } else {
                totalSeconds--;
            }
        }));
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    /**
     * Shows a notification.
     */
    private void showNotification() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Time's Up");
            alert.setHeaderText(null);
            alert.setContentText(firstName + ", your time is up!");
            alert.showAndWait();

            // Start the notification timeline to show the alert after 5 seconds
            startNotificationTimer();
        });
    }

    /**
     * Starts the notification timer.
     */
    private void startNotificationTimer() {
        if (notificationTimeline != null) {
            notificationTimeline.stop();
        }
        notificationTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> showNotification()));
        notificationTimeline.setCycleCount(1); // Only trigger once
        notificationTimeline.play();
    }

    /**
     * Shows an ask dialog.
     */
    private void showAskDialog() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Time's Up");
            alert.setHeaderText(null);
            alert.setContentText("Do you want to delay for 5 minutes or exit?");

            ButtonType delayButton = new ButtonType("Delay 5m");
            ButtonType delayButton2 = new ButtonType("Delay 15m");
            ButtonType delayButton3 = new ButtonType("Delay 30m");
            ButtonType exitButton = new ButtonType("Exit App", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(delayButton, delayButton2, delayButton3, exitButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == delayButton) {
                    startCountdownTimer(300, true); // Delay for 5 minutes (300 seconds)
                } else if (response == delayButton2) {
                    startCountdownTimer(900, true); // Delay for 15 minutes (900 seconds)
                } else if (response == delayButton3) {
                    startCountdownTimer(1800, true); // Delay for 30 minutes (1800 seconds)
                } else if (response == exitButton) {
                    System.exit(0);
                }
            });
        });
    }

    /**
     * Sets the user ID.
     * @param userId The user ID
     * @param source The source
     */
    public void setUserId(int userId, String source) {
        this.userId = userId;
        if (this.firstName == null) {
            this.firstName = fetchFirstNameFromDatabase(userId);
        }
        System.out.println("firstName: " + firstName);
        if (this.accType == null) {
            this.accType = fetchAccTypeFromDatabase(userId);
        }
        System.out.println("User account type: " + accType);
        applyModeColors();
        loadSavedColors();
        AppSettings.loadModeFromDatabase(userId);

        // When Main Menu is loaded from login page or from setTimeLimits page by saving the time, load the time limits
        // else load the remaining time
        if ("login".equals(source) || "saveSetTime".equals(source)) {
            loadTimeLimits();
        } else {
            loadRemainingTime();
        }
    }

    /**
     * Sets the first name.
     * @param firstName The first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Displays the user's name.
     * @param firstName The first name
     */
    public void displayName(String firstName) {
        lblName.setText(firstName + ", wish you are having a bright day!");
    }

    /**
     * Fetches the first name from the database.
     * @param userId The user ID
     * @return The first name
     */
    private String fetchFirstNameFromDatabase(int userId) {
        String query = "SELECT firstName FROM useraccount WHERE userId = ?";
        try (Connection conn = new DataBaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("firstName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Admin"; // Default value if the user is not found
    }

    /**
     * Fetches the account type from the database.
     * @param userId The user ID
     * @return The account type
     */
    private String fetchAccTypeFromDatabase(int userId) {
        String query = "SELECT accType FROM useraccount WHERE userId = ?";
        try (Connection conn = new DataBaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("accType");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Admin"; // Default value if the user is not found
    }

    /**
     * Sets the account type.
     * @param accType The account type
     */
    public void setAccType(String accType) {
        this.accType = accType;
    }

    /**
     * Sets the account type to main.
     * @param accType The account type
     */
    public void setAccTypeToMain(String accType) {
        this.accType = accType;
    }

    /**
     * Represents the scene type.
     * The scene type can be INTERNET, REPORT, WEBE, USER_PROFILE, SETTING, or CONTACT.
     * The scene type is used to switch the scene.
     */
    public enum SceneType {
        /**
         * Represents the internet scene type.
         */
        INTERNET,

        /**
         * Represents the report scene type.
         */
        REPORT,

        /**
         * Represents the web environment scene type.
         */
        WEBE,

        /**
         * Represents the user profile scene type.
         */
        USER_PROFILE,

        /**
         * Represents the setting scene type.
         */
        SETTING,

        /**
         * Represents the contact scene type.
         */
        CONTACT
    }

    /**
     * Switches the scene.
     * @param event The action event
     * @param sceneType The scene type
     */
    public void switchScene(ActionEvent event, SceneType sceneType) {
        String fxmlFile = "";
        String title = "Explorer";
        // Stop the countdown timer if it is running
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
            case CONTACT: // Default to Contact.fxml, but for Developer, switch to DeveloperPage.fxml
                fxmlFile = "/com/cab302/wellbeing/Contact.fxml";
                if ("Developer".equals(accType)) {
                    fxmlFile = "/com/cab302/wellbeing/DeveloperPage.fxml";
                }
                break;
            default:
                System.err.println("Unsupported scene type: " + sceneType); // Unsupported scene type
                return;
        }
        // Load the scene
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();

            // Fetch current color settings
            Color backgroundColor = (Color) paneMenu.getBackground().getFills().get(0).getFill();
            Color textColor = (Color) lblName.getTextFill();
            Color buttonColor = (Color) btnExplorer.getBackground().getFills().get(0).getFill();
            int hours = limitValue / 3600;
            int minutes = (limitValue % 3600) / 60;
            int seconds = limitValue % 60;

            switch (sceneType) {
                case INTERNET:
                    InternetExplorerController internetController = fxmlLoader.getController();
                    internetController.setUserId(userId);
                    internetController.setFirstName(firstName);
                    internetController.applyColors(backgroundColor, textColor, buttonColor);
                    internetController.applyModeColors();

                    break;

                case REPORT:
                    ReportController reportController = fxmlLoader.getController();
                    reportController.setUserId(userId);
                    reportController.setFirstName(firstName);
                    reportController.displayLineChart();
                    reportController.displayBarChart();
                    reportController.applyColors(backgroundColor, textColor, buttonColor);
                    reportController.applyModeColors();
                    break;

                case WEBE:
                    WellBeingTipsController webeController = fxmlLoader.getController();
                    webeController.setUserId(userId);
                    webeController.setFirstName(firstName);
                    webeController.setUserType(accType);
                    webeController.applyColors(backgroundColor, textColor, buttonColor);
                    webeController.applyModeColors();
                    break;

                case USER_PROFILE:
                    UserProfileController userProfileController = fxmlLoader.getController();
                    userProfileController.setUserId(userId);
                    userProfileController.loadQuestions();
                    userProfileController.displayUserProfile();
                    userProfileController.applyColors(backgroundColor, textColor, buttonColor);
                    userProfileController.applyModeColors();
                    userProfileController.setUserType(accType);
                    break;

                case SETTING:
                    SettingController settingController = fxmlLoader.getController();
                    settingController.setUserId(userId);
                    settingController.setFirstName(firstName);
                    settingController.setAccType(accType);
                    settingController.setTimeLimits(hours, minutes, seconds, active, limitType);
                    settingController.applyColors(backgroundColor, textColor, buttonColor);
                    settingController.applyModeColors();
                    break;

                case CONTACT:
                    if ("Developer".equals(accType)) {
                        DeveloperController developerController = fxmlLoader.getController();
                        developerController.displayTable();
                        developerController.applyColors(backgroundColor, textColor, buttonColor);
                        developerController.applyModeColors();
                    } else {
                        ContactController contactController = fxmlLoader.getController();
                        contactController.setUserId(userId);
                        contactController.applyColors(backgroundColor, textColor, buttonColor);
                        contactController.applyModeColors();
                    }
                    break;
            }
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
        // Pause the countdown timer if the Setting scene is opened
        if (sceneType == SceneType.SETTING) {
            if (sceneType == SceneType.SETTING) {
                pauseCountdownTimer();
                Stage stage = (Stage) btnLogOut.getScene().getWindow();
                stage.close();
            } else {
                resumeCountdownTimer();
            }
        }
    }

    /**
     * Creates an alert.
     * @param alertType The alert type
     * @param title The title
     * @param header The header
     * @param content The content
     * @return The alert
     */
    protected Alert createAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType); // Create an alert
        alert.setTitle(title); // Set the title
        alert.setHeaderText(header); // Set the header
        alert.setContentText(content); // Set the content
        return alert;
    }

    /**
     * Handles the Log Out button action.
     * Logs out the user and displays the login page.
     * @param e The action event
     */
    public void btnLogOutOnAction(ActionEvent e) {
        stopCountdownTimer(); // Stop the countdown timer
        Alert alert = createAlert(Alert.AlertType.CONFIRMATION, "Logout Confirmation", "Logging out", "Are you sure you want to log out?"); // Create a confirmation alert
        Optional<ButtonType> result = alert.showAndWait(); // Show the alert and wait for the user's response
        // If the user confirms the log out
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

    /**
     * Applies the color themes.
     * @param backgroundColor The background color
     * @param textColor The text color
     * @param buttonColor The button color
     */
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);  // Convert the background color to a hex color string
        String textHex = getHexColor(textColor); // Convert the text color to a hex color string
        String buttonHex = getHexColor(buttonColor); // Convert the button color to a hex color string

        if (paneMenu != null) {
            paneMenu.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
        if (lblName != null) {
            lblName.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (btnLogOut != null) {
            btnLogOut.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnExplorer != null) {
            btnExplorer.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnReport != null) {
            btnReport.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnWebe != null) {
            btnWebe.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnUser != null) {
            btnUser.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnSetting != null) {
            btnSetting.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnContact != null) {
            btnContact.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
    }

    /**
     * Converts a Color object to a hex color string.
     * @param color The Color object
     * @return The hex color string
     */
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255)); // Return the hex color string
    }

    /**
     * Loads the saved colors from the database.
     */
    private void loadSavedColors() {
        // Check if the database connection is null
        if (dbConnection == null) {
            System.err.println("Database connection is null.");
            return;
        }
        String query = "SELECT BackgroundColor, TextColor, ButtonColor, ButtonTextColor FROM ColorSettings WHERE UserID = ?"; // SQL query to select the colors
        // Try to connect to the database and execute the query
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String backgroundColorHex = resultSet.getString("BackgroundColor");
                String textColorHex = resultSet.getString("TextColor");
                String buttonColorHex = resultSet.getString("ButtonColor");
                String buttonTextColorHex = resultSet.getString("ButtonTextColor");

                Color backgroundColor = Color.web(backgroundColorHex);
                Color textColor = Color.web(textColorHex);
                Color buttonColor = Color.web(buttonColorHex);

                applyColors(backgroundColor, textColor, buttonColor);
            } else {
                applyColors(DEFAULT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_COLOR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Applies the color theme based on the current mode.
     */
    public void applyModeColors() {
        // Check if the background label is null
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode(); // Get the current mode
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others

        updateLabelBackgroundColor(opacity);
    }

    /**
     * Updates the background color of the label.
     * @param opacity The opacity of the color
     */
    public void updateLabelBackgroundColor(double opacity) {
        // Check if the background label is null
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity); // Get the current mode color with the specified opacity
        lblBkGrd.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";"); // Set the background color of the label
    }

    /**
     * Converts a Color object to an RGBA color string.
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
}