package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.DataBaseConnection;
import com.cab302.wellbeing.model.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
public class BrowsingHistoryController {
    @FXML
    public DatePicker startDatePicker, endDatePicker;
    @FXML
    public TextArea historyDisplayArea;
    @FXML
    public Button btnSearch, btnClear;
    @FXML
    public Pane paneHistory;
    @FXML
    public TextField txtUrl;
    @FXML
    public Label lblGreeting, lblStart, lblEnd, lblWeb, lblBkGrd;
    private String firstName;
    private int currentUserId;
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setUserId(int userId) {
        this.currentUserId = userId;
        System.out.println("currentUserId: " + currentUserId);
        System.out.println("firstName: " + firstName);
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

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String searchURL = txtUrl.getText();
        int currentUserId = UserSession.getInstance().getCurrentUserId();  // Retrieve current user ID
        String firstName = UserSession.getInstance().getFirstName();  // Retrieve first name

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
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

        if (lblGreeting != null) {
            lblGreeting.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblStart != null) {
            lblStart.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblEnd != null) {
            lblEnd.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblWeb != null) {
            lblWeb.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (btnSearch != null) {
            btnSearch.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnClear != null) {
            btnClear.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (paneHistory != null) {
            paneHistory.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
    }

    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
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
