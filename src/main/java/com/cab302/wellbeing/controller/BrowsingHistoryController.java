package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import com.cab302.wellbeing.DataBaseConnection;

public class BrowsingHistoryController {
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML
    TextArea historyDisplayArea;
    @FXML private TextField txtUrl;
    @FXML private Label lblGreeting;
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

            if (rs.next()) {
                String firstName = rs.getString("firstName");
                lblGreeting.setText(firstName + ", here is your browsing history:");
                rs.beforeFirst(); // Now this will work as rs is scrollable
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
