package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * This class is responsible for controlling the Internet Explorer.
 * It provides functionalities such as loading a page, refreshing the page,
 * zooming in and out, navigating back and forward, and storing browsing data.
 */
public class InternetExplorerController implements Initializable {
    /**
     * This method is used to set the WebView.
     */
    @FXML
    public WebView webView; // WebView for displaying web content
    /**
     * This method is used to set the TextField.
     */
    @FXML
    public TextField txtAddr; // Text field for entering the URL
    /**
     * This method is used to set the Button.
     */
    @FXML
    public Button btnRfrsh, btnZmIn, btnZmOut, btnHstry, btnBack, btnFwd, btnLoad, btnEnd; // Buttons for refreshing, zooming, viewing history, and navigating
    /**
     * This method is used to set the Pane.
     */
    @FXML
    public Pane paneInternet; // Pane for the Internet Explorer
    /**
     * This method is used to set the Label.
     */
    @FXML
    public Label lblBkGrd; // Label for the background color
    /**
     * This method is used to set the Label.
     */
    public double webZoom; // Zoom level for the WebView
    private WebHistory history; // WebHistory for navigating back and forward
    /**
     * This method is used to set the Label.
     */
    public static WebEngine engine; // WebEngine for loading web content
    private String homePage; // Default home page
    int userId; // ID of the current user
    String firstName; // First name of the user
    private long startTime, endTime; // Start and end times for loading a page
    private DataBaseConnection dbConnection = new DataBaseConnection(); // Database connection

    /**
     * Initializes the controller.
     * Sets up the WebEngine for the WebView, sets the home page, and loads the page.
     *
     * @param location The location of the FXML file
     * @param resources The resources for the controller
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
         // Initialize the WebEngine
        if (webView != null) {
            engine = webView.getEngine();
            setupListeners();
            homePage = "www.google.com"; // Ensure URL is fully qualified
            txtAddr.setText(homePage);
            webZoom = 1;
            LoadPage();
        } else {
            System.err.println("WebView is not initialized!");
        }
        // Set up the close request handler
        Platform.runLater(() -> {
            if (webView.getScene() != null) {
                Stage stage = (Stage) webView.getScene().getWindow();
                stage.setOnCloseRequest(event -> {
                    event.consume();
                    endSession();
                });
            }
        });
    }

    /**
     * Sets up the listeners for the WebEngine.
     * Listens for changes in the location property to store browsing data.
     */
    private void setupListeners() {
        // Listen for changes in the location property
        engine.locationProperty().addListener(new ChangeListener<String>() {
            // Handle the change in the location property
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (oldValue != null) {
                    endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;  // Duration in milliseconds
                    storeBrowsingData(oldValue, new Timestamp(startTime), new Timestamp(endTime), Date.valueOf(LocalDate.now()));
                }
                startTime = System.currentTimeMillis();  // Reset start time for the new page
            }
        });
    }

    /**
     * Loads the page specified by the URL in the txtAddr TextField.
     * Introduces a small delay before loading the URL to record the start time.
     */
    public void LoadPage() {
        // Check if the WebEngine is initialized
        if (engine == null) {
            System.err.println("WebEngine is not initialized.");
            return;
        }
        String url = "http://" + txtAddr.getText(); // Get the URL from the text field
        // Introduce a small delay before loading the URL
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5)); // Adjust the delay as needed
        delay.setOnFinished(e -> {
            startTime = System.currentTimeMillis();  // Record the start time when loading the page
            System.out.println("Page load started at: " + startTime);

            engine.load(url);
            // Listen for changes in the state of the WebEngine
            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                // Handle the state change when the page has loaded
                if (newState == Worker.State.SUCCEEDED) {
                    endTime = System.currentTimeMillis();  // Record the end time when the page has loaded
                    System.out.println("Page loaded at: " + endTime);

                    long duration = endTime - startTime;
                    System.out.println("Duration: " + duration + " ms");

                    java.util.Date sessionDateUtil = new java.util.Date(); // Capture the current date and time
                    Date sessionDateSql = new Date(sessionDateUtil.getTime()); // Convert it to SQL date format

                    storeBrowsingData(url, new Timestamp(startTime), new Timestamp(endTime), sessionDateSql);
                }
            });
        });
        delay.play();
    }

    /**
     * Loads the specified URL in the WebView.
     *
     * @param url The URL to load
     */
    public void loadUrl(String url) {
        if (engine == null) {
            System.err.println("WebEngine is not initialized.");
            return;
        }
        txtAddr.setText(url);
        LoadPage();
    }

    /**
     * Sets the user ID.
     *
     * @param userId The user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;  // Parse this userId to store browsing data linked to the user
    }

    /**
     * Sets the first name of the user.
     *
     * @param firstName The first name of the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName; // Parse this firstName to display a greeting message
    }

    /**
     * Stores the browsing data in the database.
     *
     * @param url The URL of the page
     * @param start The start time of the session
     * @param end The end time of the session
     * @param sessionDate The date of the session
     */
    void storeBrowsingData(String url, Timestamp start, Timestamp end, Date sessionDate) {
        String insertQuery = "INSERT INTO BrowsingData (UserID, URL, StartTime, EndTime, SessionDate) VALUES (?, ?, ?, ?, ?)"; // SQL query to insert browsing data into the database
        // Insert the browsing data into the database
        try (Connection conn = dbConnection.getConnection(); // Get a fresh connection
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, url);
            pstmt.setTimestamp(3, start);
            pstmt.setTimestamp(4, end);
            pstmt.setDate(5, new Date(sessionDate.getTime()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ends the current browsing session.
     * Loads a blank page to stop all activities and closes the current window.
     */
    public void endSession() {
        engine.load("about:blank"); // Load a blank page to stop all activities
        endTime = System.currentTimeMillis(); // Record the end time of the session
        String currentUrl = engine.getLocation(); // Get the current URL before ending the session
        storeBrowsingData(currentUrl, new Timestamp(startTime), new Timestamp(endTime), Date.valueOf(LocalDate.now())); // Store the browsing data

        Stage stage = (Stage) webView.getScene().getWindow();
        stage.close(); // Close the current window.
    }

    /**
     * Refreshes the current page in the WebView.
     */
    public void refreshPage(){
        engine.reload();    // Reload the current page
    }

    /**
     * Zooms in on the current page in the WebView.
     */
    public void zoomIn(){
        if (webZoom <= 2){
            webZoom += 0.25;   // Increase the zoom level
            webView.setZoom(webZoom);
        } else {
            webZoom = 5;   // Set the maximum zoom level
        }
    }

    /**
     * Zooms out on the current page in the WebView.
     */
    public void zoomOut(){
        if (webZoom >= 0.25){
        webZoom -= 0.25; // Decrease the zoom level
        webView.setZoom(webZoom);
        } else {
            webZoom = 1;  // Set the minimum zoom level
        }
    }

    /**
     * Navigates back to the previous page in the WebView.
     * @param event - the event that triggers the method
     */
    public void switchToHistoryScene(ActionEvent event) {
        Color backgroundColor = (Color) paneInternet.getBackground().getFills().get(0).getFill(); // Get the background color
        Color textColor = (Color) btnLoad.getTextFill(); // Get the text color
        Color buttonColor = (Color) btnLoad.getBackground().getFills().get(0).getFill(); // Get the button color
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/BrowsingHistory.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();

            BrowsingHistoryController controller = fxmlLoader.getController();
            controller.setFirstName(firstName);  // Pass the user ID to the InternetExplorer controller
            controller.applyColors(backgroundColor, textColor, buttonColor);
            controller.applyModeColors();
            stage.setTitle("Explorer");
            stage.setScene(new Scene(root1));
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading InternetExplorer.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigates back to the previous page in the WebView.
     */
    public void back(){
        if (engine.getHistory().getCurrentIndex() > 0) {  // Ensure there is a history to go back to
            endTime = System.currentTimeMillis(); // Record the end time of the session
            long duration = endTime - startTime;  // Calculate the duration
            String currentUrl = engine.getLocation();  // Get the current URL before going back

            // Store data
            storeBrowsingData(currentUrl, new Timestamp(startTime), new Timestamp(endTime), Date.valueOf(LocalDate.now()));

            // Navigate back
            history = engine.getHistory();
            history.go(-1);
            txtAddr.setText(history.getEntries().get(history.getCurrentIndex()).getUrl());
            startTime = System.currentTimeMillis();  // Reset start time
        }
    }

    /**
     * Navigates forward to the next page in the WebView.
     */
    public void forward() {
        WebHistory history = engine.getHistory(); // Get the WebHistory
        if (history != null && history.getCurrentIndex() < history.getEntries().size() - 1) {
            endTime = System.currentTimeMillis(); // Record the end time of the session
            long duration = endTime - startTime;  // Calculate the duration
            String currentUrl = engine.getLocation();  // Get the current URL before going forward
            // Store data
            storeBrowsingData(currentUrl, new Timestamp(startTime), new Timestamp(endTime), Date.valueOf(LocalDate.now()));
            // Navigate forward
            history.go(1);
            txtAddr.setText(history.getEntries().get(history.getCurrentIndex()).getUrl());
            startTime = System.currentTimeMillis();  // Reset start time
        }
    }

    /**
     * Applies the color theme to the Internet Explorer.
     *
     * @param backgroundColor The background color
     * @param textColor The text color
     * @param buttonColor The button color
     */
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

        if (btnFwd != null) {
            btnFwd.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnRfrsh != null) {
            btnRfrsh.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnZmIn != null) {
            btnZmIn.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnZmOut != null) {
            btnZmOut.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnBack != null) {
            btnBack.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnHstry != null) {
            btnHstry.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnLoad != null) {
            btnLoad.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnEnd != null) {
            btnEnd.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (paneInternet != null) {
            paneInternet.setStyle("-fx-background-color: " + backgroundHex + ";");
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
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255)); // Convert the color to hex format
    }

    /**
     * Applies the color theme based on the current mode.
     * @param mockDataBaseConnection - the mockDataBaseConnection that triggers the method
     */
    public void setDbConnection(DataBaseConnection mockDataBaseConnection) {
        this.dbConnection = mockDataBaseConnection;
    }

    /**
     * Applies the color theme based on the current mode.
     */
    public void applyModeColors() {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        String currentMode = AppSettings.getCurrentMode(); // Get the current mode
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others
        updateLabelBackgroundColor(opacity); // Update the label background color
    }

    /**
     * Updates the background color of the label.
     *
     * @param opacity The opacity of the color
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
}
