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
public class InternetExplorerController implements Initializable {
    @FXML
    public WebView webView;
    @FXML
    public TextField txtAddr;
    @FXML
    Button btnRfrsh, btnZmIn, btnZmOut, btnHstry, btnBack, btnFwd, btnLoad, btnEnd;
    @FXML
    public Pane paneInternet;
    public Label lblBkGrd;
    public double webZoom;
    private WebHistory history;
    public static WebEngine engine;
    private String homePage;
    int userId;
    String firstName;
    private long startTime, endTime;
    private DataBaseConnection dbConnection = new DataBaseConnection();

    public void setEngine(WebEngine engine) {

        this.engine = engine;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
    private void setupListeners() {
        engine.locationProperty().addListener(new ChangeListener<String>() {
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
    public void LoadPage() {
        if (engine == null) {
            System.err.println("WebEngine is not initialized.");
            return;
        }
        String url = "http://" + txtAddr.getText();
        // Introduce a small delay before loading the URL
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5)); // Adjust the delay as needed
        delay.setOnFinished(e -> {
            startTime = System.currentTimeMillis();  // Record the start time when loading the page
            System.out.println("Page load started at: " + startTime);

            engine.load(url);

            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
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

    public void loadUrl(String url) {
        if (engine == null) {
            System.err.println("WebEngine is not initialized.");
            return;
        }
        txtAddr.setText(url);
        LoadPage();
    }
    public void setUserId(int userId) {
        this.userId = userId;  // Now you can use this userId to store browsing data linked to the user
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    void storeBrowsingData(String url, Timestamp start, Timestamp end, Date sessionDate) {
        String insertQuery = "INSERT INTO BrowsingData (UserID, URL, StartTime, EndTime, SessionDate) VALUES (?, ?, ?, ?, ?)";
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
    public void endSession() {
        engine.load("about:blank"); // Load a blank page to stop all activities
        endTime = System.currentTimeMillis();
        String currentUrl = engine.getLocation();
        storeBrowsingData(currentUrl, new Timestamp(startTime), new Timestamp(endTime), Date.valueOf(LocalDate.now()));

        Stage stage = (Stage) webView.getScene().getWindow();
        stage.close(); // Close the current window.
    }

    public void refreshPage(){
        engine.reload();
    }
    public void zoomIn(){
        if (webZoom <= 2){
            webZoom += 0.25;
            webView.setZoom(webZoom);
        } else {
            webZoom = 5;
        }
    }
    public void zoomOut(){
        if (webZoom >= 0.25){
        webZoom -= 0.25;
        webView.setZoom(webZoom);
        } else {
            webZoom = 0;
        }
    }
    public void switchToHistoryScene(ActionEvent event) {
        Color backgroundColor = (Color) paneInternet.getBackground().getFills().get(0).getFill();
        Color textColor = (Color) btnLoad.getTextFill();
        Color buttonColor = (Color) btnLoad.getBackground().getFills().get(0).getFill();
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
    public void back(){
        if (engine.getHistory().getCurrentIndex() > 0) {  // Ensure there is a history to go back to
            endTime = System.currentTimeMillis();
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
    public void forward() {
        WebHistory history = engine.getHistory();
        if (history != null && history.getCurrentIndex() < history.getEntries().size() - 1) {
            endTime = System.currentTimeMillis();
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

    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
    public void setDbConnection(DataBaseConnection mockDataBaseConnection) {
        this.dbConnection = mockDataBaseConnection;
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
