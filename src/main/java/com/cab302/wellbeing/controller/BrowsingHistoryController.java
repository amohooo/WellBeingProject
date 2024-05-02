package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.DataBaseConnection;
import com.cab302.wellbeing.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class BrowsingHistoryController {
    @FXML
    public DatePicker startDatePicker;
    @FXML
    public DatePicker endDatePicker;
    @FXML
    public TextArea historyDisplayArea;
    @FXML
    public Button btnLoadHistory;
    @FXML
    public TextField txtUrl;
    @FXML
    public Label lblGreeting;
    String firstName;
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void initialize() {
        // Set up the event handler for the Enter key in the txtUrl TextField
        txtUrl.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loadHistory();  // Call your method to load or search the history
            }
        });
    }
    public void loadHistory() {
        historyDisplayArea.clear();

        boolean isFirstNameSet = false;

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String searchURL = txtUrl.getText();

        int currentUserId = UserSession.getInstance().getCurrentUserId();  // Retrieve current user ID

        String query = "SELECT u.firstName, b.URL, b.StartTime, b.SessionDate, b.Duration " +
                "FROM BrowsingData b " +
                "JOIN useraccount u ON b.UserID = u.userId " +
                "WHERE b.UserID = ? ";

        if (searchURL != null && !searchURL.isEmpty()) {
            query += "AND b.URL LIKE ? ";
        }

        if (startDate != null && endDate != null) {
            query += "AND b.SessionDate BETWEEN ? AND ? ";
        } else if (searchURL == null || searchURL.isEmpty()) {
            query += "AND b.SessionDate = CURRENT_DATE() ";
        }

        query += "ORDER BY b.SessionDate DESC, b.StartTime DESC;";

        try (Connection conn = new DataBaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            int paramIndex = 1;
            stmt.setInt(paramIndex++, currentUserId); // Set the user ID

            if (searchURL != null && !searchURL.isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchURL + "%");
            }
            if (startDate != null && endDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
            }

            ResultSet rs = stmt.executeQuery();
            //String firstName = rs.getString("firstName");
            if (rs.next()) {

                lblGreeting.setText(firstName + ", here is your browsing history:");
                rs.beforeFirst(); // Now this will work as rs is scrollable
                //String firstName = rs.getString("firstName");

                //return;
            } else {
                lblGreeting.setText("Sorry, " + firstName + ", No browsing history found");
                historyDisplayArea.setText("Failed to load browsing history: No results found.");
                return;
            }

            while (rs.next()) {

                String url = rs.getString("URL");
                String startTime = rs.getString("StartTime").toString();
                String sessionDate = rs.getString("SessionDate").toString();
                int duration = rs.getInt("Duration");

                String displayText = String.format("URL: %s, Start: %s, Date: %s, Duration: %d seconds\n",
                        url, startTime, sessionDate, duration);
                historyDisplayArea.appendText(displayText);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            historyDisplayArea.setText("Failed to load browsing history: " + e.getMessage());
        }

    }
    @FXML
    public void clearHistoryDisplay() {
        historyDisplayArea.clear();  // Clears the text area
        lblGreeting.setText("Welcome, want to see your browsing history?"); // Reset greeting
    }
}
