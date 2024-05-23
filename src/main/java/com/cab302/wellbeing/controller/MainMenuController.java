package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.*;
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


public class MainMenuController {
    @FXML
    public Button btnLogOut, btnExplorer, btnReport, btnWebe, btnUser, btnSetting, btnContact;
    @FXML
    public Label lblName;
    @FXML
    public Pane paneMenu;

    @FXML
    private Label lblBkGrd;
    private int userId;
    private int remainingTime;
    private String firstName;
    private Timeline countdown;
    private Timeline notificationTimeline;
    private int totalSeconds;
    private boolean notifySelected;
    private boolean askSelected;
    private boolean exitSelected;
    private String limitType;
    private int limitValue;
    private boolean active;

    private DataBaseConnection dbConnection = new DataBaseConnection();
    private String accType;
    private static final Color DEFAULT_COLOR = Color.web("#009ee0");
    private static final Color DEFAULT_TEXT_COLOR = Color.web("#ffffff");
    private static MainMenuController instance;
    public static MainMenuController getInstance() {
        if (instance == null) {
            instance = new MainMenuController();
        }
        return instance;
    }
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
    private void resumeCountdownTimer() {
        startCountdownTimer(remainingTime, active);
    }

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

    private void startNotificationTimer() {
        if (notificationTimeline != null) {
            notificationTimeline.stop();
        }
        notificationTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> showNotification()));
        notificationTimeline.setCycleCount(1); // Only trigger once
        notificationTimeline.play();
    }

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
                    startCountdownTimer(10, true); // Delay for 5 minutes (300 seconds)
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
        // Load the time limits from the database
        if ("login".equals(source) || "saveSetTime".equals(source)) {
            loadTimeLimits();
        } else {
            loadRemainingTime();
        }
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void displayName(String firstName) {
        lblName.setText(firstName + ", wish you are having a bright day!");
    }

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

    public void setAccType(String accType) {
        this.accType = accType;
    }
    public void setAccTypeToMain(String accType) {
        this.accType = accType;
    }

    public enum SceneType {
        INTERNET, REPORT, WEBE, USER_PROFILE, SETTING, CONTACT
    }

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
            case CONTACT:
                fxmlFile = "/com/cab302/wellbeing/Contact.fxml";
                if ("Developer".equals(accType)) {
                    fxmlFile = "/com/cab302/wellbeing/DeveloperPage.fxml";
                }
                break;
            default:
                System.err.println("Unsupported scene type: " + sceneType);
                return;
        }

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

    protected Alert createAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }

    public void btnLogOutOnAction(ActionEvent e) {
        stopCountdownTimer();
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

    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

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

    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    private void loadSavedColors() {
        if (dbConnection == null) {
            System.err.println("Database connection is null.");
            return;
        }
        String query = "SELECT BackgroundColor, TextColor, ButtonColor, ButtonTextColor FROM ColorSettings WHERE UserID = ?";
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

    private void closeCurrentWindow() {
        Stage stage = (Stage) btnLogOut.getScene().getWindow();
        stage.close();
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