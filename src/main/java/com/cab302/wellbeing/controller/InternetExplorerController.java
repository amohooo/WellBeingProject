package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.DataBaseConnection;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class InternetExplorerController implements Initializable {
    @FXML
    WebView webView;
    @FXML
    TextField txtAddr;
    @FXML
    Button btnRfrsh;
    @FXML
    Button btnZmIn;
    @FXML
    Button btnZmOut;
    double webZoom;
    private WebHistory history;
    private WebEngine engine;
    private String homePage;
    int userId;
    String firstName;
    private long startTime, endTime;
    private DataBaseConnection dbConnection = new DataBaseConnection();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        engine = webView.getEngine();
        homePage = "www.google.com";
        txtAddr.setText(homePage);
        webZoom = 1;
        setupListeners();
        LoadPage();

        // Correctly handle the closing of the window, set up once the stage is visible.
        Platform.runLater(() -> {
            Stage stage = (Stage) webView.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                endSession();
                event.consume();
            });
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
        startTime = System.currentTimeMillis();  // Record the start time when loading the page
        System.out.println("Page load started at: " + startTime);

        String url = "http://" + txtAddr.getText();
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
            pstmt.setDate(5, new java.sql.Date(sessionDate.getTime()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void endSession() {
        endTime = System.currentTimeMillis();
        String currentUrl = engine.getLocation();
        storeBrowsingData(currentUrl, new Timestamp(startTime), new Timestamp(endTime), Date.valueOf(LocalDate.now()));
        Stage stage = (Stage) webView.getScene().getWindow();
        stage.close();
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
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/BrowsingHistory.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();

            BrowsingHistoryController controller = fxmlLoader.getController();
            controller.setFirstName(firstName);  // Pass the user ID to the InternetExplorer controller

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
    public void forward(){
        if (history.getCurrentIndex() < history.getEntries().size() - 1) {  // Ensure there is a history to go forward to
            endTime = System.currentTimeMillis();
            long duration = endTime - startTime;  // Calculate the duration
            String currentUrl = engine.getLocation();  // Get the current URL before going forward

            // Store data
            storeBrowsingData(currentUrl, new Timestamp(startTime), new Timestamp(endTime), Date.valueOf(LocalDate.now()));

            // Navigate forward
            history = engine.getHistory();
            history.go(1);
            txtAddr.setText(history.getEntries().get(history.getCurrentIndex()).getUrl());
            startTime = System.currentTimeMillis();  // Reset start time
        }
    }
}
